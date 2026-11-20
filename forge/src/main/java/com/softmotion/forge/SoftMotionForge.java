package com.softmotion.forge;

import com.softmotion.config.SoftMotionConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Forge 1.20.1 entrypoint. The produced jar also runs on NeoForge 20.1, which is
 * binary-compatible with this Forge line (Create itself ships the same jar for
 * both loaders on 1.20.1).
 */
@Mod(SoftMotionForge.MODID)
public class SoftMotionForge {
    public static final String MODID = "softmotion";

    public SoftMotionForge() {
        // Forge 1.20.1 has no generic config screen API, so the TOML file is the
        // documented way to change settings on this platform.
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SoftMotionForgeConfig.SPEC, "softmotion-client.toml");
        SoftMotionConfig.setProvider(new SoftMotionForgeConfig());
    }
}
