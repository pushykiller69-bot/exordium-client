package com.bred.crystaloptimizer;

import com.bred.crystaloptimizer.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class CrystalOptimizer implements ModInitializer {

    public static final String MOD_ID = "crystaloptimizer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Set<String> SPAWNER_BLOCKS = new HashSet<>();

    public static boolean isLoggerValid() {
        return true;
    }

    @Override
    public void onInitialize() {
        ModConfig.load();
        LOGGER.info("Exordium Client initialized.");
    }
}
