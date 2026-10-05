package com.jellyjam.jellyjamrunes;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.logging.Level;

public class Main extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public Main(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        LOGGER.at(Level.INFO).log("Starting JellyJamRunes!");
    }

    @Override
    protected void setup() {

        LOGGER.at(Level.INFO).log("Setting up JellyJamRunes!");
        this.getEntityStoreRegistry().registerSystem(new TinyEffectWatcher());
    }

    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("Shutting down JellyJamRunes!");
    }


}
