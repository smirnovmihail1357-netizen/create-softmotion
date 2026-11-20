package com.softmotion.util;

import java.util.Locale;

/**
 * Small self-contained expression evaluator used by the visual speed formula.
 *
 * <p>Supported: numbers, variables {@code rpm}, {@code speed}, constants {@code pi}, {@code e},
 * operators {@code + - * / % ^}, comparisons, {@code &&}, {@code ||}, {@code !}, ternary
 * {@code ? :}, and functions abs/min/max/clamp/sqrt/sign/floor/ceil/round/pow/log/exp/
 * sin/cos/tan/if. Booleans are represented as 1.0 / 0.0. A compiled formula is immutable
 * and can be evaluated from several render threads at once.</p>
 */
public final class Formula {
    private final Node root;

    private Formula(Node root) {
        this.root = root;
    }

    public static Formula compile(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Formula is empty");
        }
        if (source.length() > 512) {
            throw new IllegalArgumentException("Formula is too long (max 512 characters)");
        }
        return new Formula(new Parser(source).parse());
    }

    public double evaluate(double rpm, double speed) {
        return root.eval(rpm, speed);
    }

    private interface Node {
        double eval(double rpm, double speed);
    }

    private record Const(double value) implements Node {
        @Override
        public double eval(double rpm, double speed) {
            return value;
        }
    }

    private record Var(int index) implements Node {
        @Override
        public double eval(double rpm, double speed) {
            return index == 0 ? rpm : speed;
        }
    }

    private record Neg(Node operand) implements Node {
        @Override
        public double eval(double rpm, double speed) {
            return -operand.eval(rpm, speed);
        }
    }

    private record Not(Node operand) implements Node {
        @Override
        public double eval(double rpm, double speed) {
            return operand.eval(rpm, speed) == 0.0 ? 1.0 : 0.0;
        }
    }

    private record Binary(char op, Node left, Node right) implements Node {
        @Override
        public double eval(double rpm, double speed) {
            double a = left.eval(rpm, speed);
            if (op == '&') {
                return a != 0.0 && right.eval(rpm, speed) != 0.0 ? 1.0 : 0.0;
            }
            if (op == '|') {
                return a != 0.0 || right.eval(rpm, speed) != 0.0 ? 1.0 : 0.0;
            }
            double b = right.eval(rpm, speed);
            return switch (op) {
                case '+' -> a + b;
                case '-' -> a - b;
                case '*' -> a * b;
                case '/' -> a / b;
                case '%' -> a % b;
                case '^' -> Math.pow(a, b);
                case '<' -> a < b ? 1.0 : 0.0;
                case '>' -> a > b ? 1.0 : 0.0;
                case 'l' -> a <= b ? 1.0 : 0.0;
                case 'g' -> a >= b ? 1.0 : 0.0;
                case '=' -> a == b ? 1.0 : 0.0;
                case 'n' -> a != b ? 1.0 : 0.0;
                default -> throw new IllegalStateException("Unknown operator: " + op);
            };
        }
    }

    private record Ternary(Node condition, Node whenTrue, Node whenFalse) implements Node {
        @Override
        public double eval(double rpm, double speed) {
            return condition.eval(rpm, speed) != 0.0 ? whenTrue.eval(rpm, speed) : whenFalse.eval(rpm, speed);
        }
    }

    private record Call(String name, Node[] args) implements Node {
        @Override
        public double eval(double rpm, double speed) {
            // Evaluated once per element per frame, so avoid allocating per call.
            double a = args[0].eval(rpm, speed);
            return switch (name) {
                case "abs" -> Math.abs(a);
                case "min" -> min(a, rpm, speed);
                case "max" -> max(a, rpm, speed);
                case "clamp" -> Math.max(args[1].eval(rpm, speed), Math.min(args[2].eval(rpm, speed), a));
                case "sqrt" -> Math.sqrt(a);
                case "sign" -> Math.signum(a);
                case "floor" -> Math.floor(a);
                case "ceil" -> Math.ceil(a);
                case "round" -> Math.round(a);
                case "pow" -> Math.pow(a, args[1].eval(rpm, speed));
                case "log" -> Math.log(a);
                case "exp" -> Math.exp(a);
                case "sin" -> Math.sin(a);
                case "cos" -> Math.cos(a);
                case "tan" -> Math.tan(a);
                case "if" -> a != 0.0 ? args[1].eval(rpm, speed) : args[2].eval(rpm, speed);
                default -> throw new IllegalStateException("Unknown function: " + name);
            };
        }

        private double min(double first, double rpm, double speed) {
            double result = first;
            for (int i = 1; i < args.length; i++) {
                result = Math.min(result, args[i].eval(rpm, speed));
            }
            return result;
        }

        private double max(double first, double rpm, double speed) {
            double result = first;
            for (int i = 1; i < args.length; i++) {
                result = Math.max(result, args[i].eval(rpm, speed));
            }
            return result;
        }
    }

    /** Recursive-descent parser; all parsing happens once when the config is loaded. */
    private static final class Parser {
        private final String source;
        private int pos;

        Parser(String source) {
            this.source = source;
        }

        Node parse() {
            Node node = ternary();
            skipWhitespace();
            if (pos < source.length()) {
                throw error("Unexpected trailing input");
            }
            return node;
        }

        private Node ternary() {
            Node condition = or();
            if (consume('?')) {
                Node whenTrue = ternary();
                expect(':');
                Node whenFalse = ternary();
                return new Ternary(condition, whenTrue, whenFalse);
            }
            return condition;
        }

        private Node or() {
            Node node = and();
            while (consume("||")) {
                node = new Binary('|', node, and());
            }
            return node;
        }

        private Node and() {
            Node node = equality();
            while (consume("&&")) {
                node = new Binary('&', node, equality());
            }
            return node;
        }

        private Node equality() {
            Node node = comparison();
            while (true) {
                if (peek("==")) {
                    node = new Binary('=', node, comparison());
                } else if (peek("!=")) {
                    node = new Binary('n', node, comparison());
                } else {
                    return node;
                }
            }
        }

        private Node comparison() {
            Node node = term();
            while (true) {
                if (peek("<=")) {
                    node = new Binary('l', node, term());
                } else if (peek(">=")) {
                    node = new Binary('g', node, term());
                } else if (consume('<')) {
                    node = new Binary('<', node, term());
                } else if (consume('>')) {
                    node = new Binary('>', node, term());
                } else {
                    return node;
                }
            }
        }

        private Node term() {
            Node node = factor();
            while (true) {
                if (consume('+')) {
                    node = new Binary('+', node, factor());
                } else if (consume('-')) {
                    node = new Binary('-', node, factor());
                } else {
                    return node;
                }
            }
        }

        private Node factor() {
            Node node = unary();
            while (true) {
                if (consume('*')) {
                    node = new Binary('*', node, unary());
                } else if (consume('/')) {
                    node = new Binary('/', node, unary());
                } else if (consume('%')) {
                    node = new Binary('%', node, unary());
                } else {
                    return node;
                }
            }
        }

        private Node unary() {
            if (consume('-')) {
                return new Neg(unary());
            }
            if (consume('+')) {
                return unary();
            }
            if (consume('!')) {
                return new Not(unary());
            }
            Node node = primary();
            if (consume('^')) {
                return new Binary('^', node, unary()); // right associative
            }
            return node;
        }

        private Node primary() {
            skipWhitespace();
            if (pos >= source.length()) {
                throw error("Expected a number, variable or '('");
            }
            char c = source.charAt(pos);
            if (c == '(') {
                pos++;
                Node node = ternary();
                expect(')');
                return node;
            }
            if (Character.isDigit(c) || c == '.') {
                return number();
            }
            if (Character.isLetter(c) || c == '_') {
                return identifier();
            }
            throw error("Unexpected character '" + c + "'");
        }

        private Node number() {
            int start = pos;
            boolean dot = false;
            while (pos < source.length()) {
                char c = source.charAt(pos);
                if (Character.isDigit(c)) {
                    pos++;
                } else if (c == '.' && !dot) {
                    dot = true;
                    pos++;
                } else {
                    break;
                }
            }
            if (pos < source.length() && (source.charAt(pos) == 'e' || source.charAt(pos) == 'E')) {
                int exponentStart = pos++;
                if (pos < source.length() && (source.charAt(pos) == '+' || source.charAt(pos) == '-')) {
                    pos++;
                }
                int digits = 0;
                while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
                    pos++;
                    digits++;
                }
                if (digits == 0) {
                    pos = exponentStart;
                }
            }
            try {
                return new Const(Double.parseDouble(source.substring(start, pos)));
            } catch (NumberFormatException e) {
                throw error("Invalid number '" + source.substring(start, pos) + "'");
            }
        }

        private Node identifier() {
            int start = pos;
            while (pos < source.length() && (Character.isLetterOrDigit(source.charAt(pos)) || source.charAt(pos) == '_')) {
                pos++;
            }
            String name = source.substring(start, pos).toLowerCase(Locale.ROOT);
            skipWhitespace();
            if (consume('(')) {
                Node[] args = new Node[0];
                if (!consume(')')) {
                    java.util.List<Node> list = new java.util.ArrayList<>();
                    do {
                        list.add(ternary());
                    } while (consume(','));
                    expect(')');
                    args = list.toArray(Node[]::new);
                }
                validateArity(name, args.length);
                return new Call(name, args);
            }
            return switch (name) {
                case "rpm" -> new Var(0);
                case "speed" -> new Var(1);
                case "pi" -> new Const(Math.PI);
                case "e" -> new Const(Math.E);
                default -> throw error("Unknown variable '" + name + "'");
            };
        }

        private static void validateArity(String name, int count) {
            int expected = switch (name) {
                case "abs", "sqrt", "sign", "floor", "ceil", "round", "log", "exp", "sin", "cos", "tan" -> 1;
                case "pow" -> 2;
                case "clamp", "if" -> 3;
                case "min", "max" -> count >= 2 ? count : -1;
                default -> -1;
            };
            if (expected == -1) {
                throw new IllegalArgumentException("Unknown function '" + name + "' or wrong number of arguments");
            }
            if (count != expected) {
                throw new IllegalArgumentException("Function '" + name + "' expects " + expected + " argument(s), got " + count);
            }
        }

        private void skipWhitespace() {
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
                pos++;
            }
        }

        private boolean consume(char c) {
            skipWhitespace();
            if (pos < source.length() && source.charAt(pos) == c) {
                pos++;
                return true;
            }
            return false;
        }

        private boolean peek(String text) {
            skipWhitespace();
            return source.startsWith(text, pos);
        }

        private boolean consume(String text) {
            skipWhitespace();
            if (source.startsWith(text, pos)) {
                pos += text.length();
                return true;
            }
            return false;
        }

        private void expect(char c) {
            if (!consume(c)) {
                throw error("Expected '" + c + "'");
            }
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at position " + pos + " in '" + source + "'");
        }
    }

    private Formula() {
        throw new AssertionError();
    }
}
