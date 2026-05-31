package com.creditor;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.logging.Level;

public class Main extends JavaPlugin {
    public Main(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        Creditor.start(this);
    }

    @Override
    protected void setup() {
        Creditor.setup(this);
    }
}
