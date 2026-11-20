package com.softmotion.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal TOML reader/writer for the Fabric build, where no config library is
 * bundled by choice (see README). It supports exactly what the mod needs:
 * a {@code [client]} table with three scalar keys. The file is read once at
 * client startup; edit it and restart the game to apply changes.
 */
public final class TomlConfigFile implements SoftMotionConfig.Provider {
    private static final Logger LOGGER = LoggerFactory.getLogger("softmotion");

    private double visualSpeedMultiplier = 0.25D;
    private String visualSpeedFormula = "";
    private boolean slowBearingContraptions = true;

    private TomlConfigFile() {
    }

    public static TomlConfigFile load(Path file) {
        TomlConfigFile config = new TomlConfigFile();
        if (Files.isRegularFile(file)) {
            try {
                config.parse(file);
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Failed to read {}, using defaults: {}", file, e.toString());
            }
        } else {
            writeDefaultTemplate(file);
        }
        return config;
    }

    private void parse(Path file) throws IOException {
        String section = "";
        for (String rawLine : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String line = stripComment(rawLine.trim());
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("[") && line.endsWith("]")) {
                section = line.substring(1, line.length() - 1).trim();
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue; // tolerate junk lines instead of breaking the mod
            }
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            if (!"client".equals(section)) {
                continue;
            }
            switch (key) {
                case "visualSpeedMultiplier" -> visualSpeedMultiplier = clampDouble(parseDouble(value, 0.25D));
                case "visualSpeedFormula" -> visualSpeedFormula = parseString(value);
                case "slowBearingContraptions" -> slowBearingContraptions = parseBoolean(value, true);
                default -> {
                }
            }
        }
    }

    private static String stripComment(String line) {
        boolean inString = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inString = !inString;
            } else if ((c == '#' || c == ';') && !inString) {
                return line.substring(0, i).trim();
            }
        }
        return line;
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double clampDouble(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static boolean parseBoolean(String value, boolean fallback) {
        String v = value.toLowerCase(java.util.Locale.ROOT);
        if ("true".equals(v)) {
            return true;
        }
        if ("false".equals(v)) {
            return false;
        }
        return fallback;
    }

    private static String parseString(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static void writeDefaultTemplate(Path file) {
        String template = String.join(System.lineSeparator(),
                "# Create: SoftMotion client configuration (client-side only).",
                "# Visual slowdown of Create machinery. Does NOT change gameplay, stress, RPM, or kinetic networks.",
                "# Edit while the game is closed; restart the client to apply.",
                "",
                "[client]",
                "    # Visual rotation multiplier, clamped to 0.0..1.0.",
                "    # 0.0 = visually frozen, 0.25 = rotation divided by 4, 1.0 = vanilla Create visuals.",
                "    visualSpeedMultiplier = 0.25",
                "",
                "    # Optional formula for the visual multiplier, evaluated per element.",
                "    # Empty = use visualSpeedMultiplier.",
                "    # Variables: rpm = real absolute speed (Create units), speed = rpm * 6 (Flywheel units).",
                "    # Result is clamped to 0.0..1.0.",
                "    # Operators: + - * / % ^, comparisons, &&, ||, !, ? :",
                "    # Functions: abs, min, max, clamp, sqrt, sign, floor, ceil, round, pow, log, exp, sin, cos, tan, if.",
                "    # Examples:",
                "    #   \"min(1, 8 / (rpm + 0.001))\"",
                "    #   \"clamp(0.25 + rpm / 200, 0.1, 1)\"",
                "    visualSpeedFormula = \"\"",
                "",
                "    # If true, contraption entities attached to Mechanical Bearings are slowed visually too.",
                "    slowBearingContraptions = true",
                "");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, template, StandardCharsets.UTF_8);
            LOGGER.info("Wrote default config {}", file);
        } catch (IOException e) {
            LOGGER.warn("Could not write default config {}: {}", file, e.toString());
        }
    }

    @Override
    public double visualSpeedMultiplier() {
        return visualSpeedMultiplier;
    }

    @Override
    public String visualSpeedFormula() {
        return visualSpeedFormula;
    }

    @Override
    public boolean slowBearingContraptions() {
        return slowBearingContraptions;
    }
}
