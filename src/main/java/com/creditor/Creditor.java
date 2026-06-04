package com.creditor;

import com.creditor.asset.CreditAsset;
import com.creditor.command.CreditsCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import lombok.Getter;

public class Creditor {

    private Creditor() {}

    public static void setup(JavaPlugin host) {
        host.getAssetRegistry().register(CreditAsset.createAssetStore());
        host.getCommandRegistry().registerCommand(new CreditsCommand());
    }

    public static void start(JavaPlugin host) {}
}
