package top.znhua;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrivateZone implements ModInitializer {
    public static final String PRIZ_NAMESPACE = "priz";
    public static final String HOME_DIM_PREFIX = "homedim";
    public static final int HOME_DIM_LIMIT = 24;
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(PRIZ_NAMESPACE);


    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fucking Fabric world!");
        AttachmentTypes.init();
        HomezCommand.initCommands();
    }
}