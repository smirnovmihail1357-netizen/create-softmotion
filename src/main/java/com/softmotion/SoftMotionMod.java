package com.softmotion;

import com.softmotion.config.SoftMotionConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(SoftMotionMod.MODID)
public class SoftMotionMod {
    public static final String MODID = "softmotion";

    public SoftMotionMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, SoftMotionConfig.SPEC, "softmotion-client.toml");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            registerNativeConfigScreen(modContainer);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerNativeConfigScreen(ModContainer modContainer) {
        try {
            Class<?> factoryClass = Class.forName("net.neoforged.neoforge.client.gui.IConfigScreenFactory");
            Class<?> screenClass = Class.forName("net.neoforged.neoforge.client.gui.ConfigurationScreen");
            Object factory = Proxy.newProxyInstance(
                    SoftMotionMod.class.getClassLoader(),
                    new Class<?>[]{factoryClass},
                    (proxy, method, args) -> {
                        if ("createScreen".equals(method.getName())) {
                            Constructor<?> constructor = screenClass.getConstructor(method.getParameterTypes());
                            return constructor.newInstance(args);
                        }
                        return null;
                    });
            modContainer.registerExtensionPoint((Class) factoryClass, (java.util.function.Supplier) () -> factory);
        } catch (Throwable ignored) {
            // Configuration screen is unavailable; the mod still works with the TOML config file.
        }
    }
}
