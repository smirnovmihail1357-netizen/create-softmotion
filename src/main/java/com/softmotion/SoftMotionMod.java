package com.softmotion;

import com.softmotion.config.SoftMotionConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(SoftMotionMod.MODID)
public class SoftMotionMod {
    public static final String MODID = "softmotion";

    public SoftMotionMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SoftMotionConfig.SPEC, "softmotion-client.toml");
    }
}
