package com.creditor;

import com.creditor.asset.CreditAsset;
import com.creditor.command.CreditsCommand;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.asseteditor.AssetEditorPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import lombok.Getter;

public class Creditor {

    private Creditor() {}

    public static void setup(JavaPlugin host) {
        if(AssetEditorPlugin.get().getAssetTypeRegistry().getAssetTypeHandler("CreditAsset")==null) {
            AssetRegistry.register(CreditAsset.createAssetStore());
            host.getCommandRegistry().registerCommand(new CreditsCommand());
//          host.getLogger().atInfo().log("Creditor registered");
        }
//        else{
//            host.getLogger().atInfo().log("Creditor has been set up by another plugin skipping registration");
//        }
    }

    public static void start(JavaPlugin host) {}
}
