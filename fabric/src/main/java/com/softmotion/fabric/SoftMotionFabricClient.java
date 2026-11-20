package com.softmotion.fabric;

import com.softmotion.config.SoftMotionConfig;
import com.softmotion.config.TomlConfigFile;
import java.nio.file.Path;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Fabric 1.20.1 entrypoint. Config lives in .minecraft/config/softmotion-client.toml;
 * it is written with defaults on first launch and re-read on the next startup.
 */
public class SoftMotionFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Path config = FabricLoader.getInstance().getConfigDir().resolve("softmotion-client.toml");
        SoftMotionConfig.setProvider(TomlConfigFile.load(config));
    }
}
