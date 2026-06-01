package com.creditor;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import lombok.Getter;

public class Main extends JavaPlugin {
    @Getter
    private static boolean supporter = false;

    public Main(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        supporter = true;
        Creditor.start(this);
    }

    @Override
    protected void setup() {
        Creditor.setup(this);
    }
}
