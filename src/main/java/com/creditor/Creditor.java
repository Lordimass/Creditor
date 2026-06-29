package com.creditor;

import com.creditor.asset.CreditAsset;
import com.creditor.command.CreditsCommand;
import com.creditor.util.VersionManagementUtils;
import com.hypixel.hytale.assetstore.AssetMap;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static com.creditor.util.VersionManagementUtils.*;

public class Creditor {
    private static JavaPlugin host;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static void setup(JavaPlugin host) {
        Creditor.host = host;
        if (!containsGreatestCreditorVersion(host)) return;
        LOGGER.atInfo().log("Registering Creditor v" + host.getManifest().getVersion());

        // Check handleAllWorldsLoaded event when adding extra registrations here
        host.getAssetRegistry().register(CreditAsset.createAssetStore());
        host.getCommandRegistry().registerCommand(new CreditsCommand());
        host.getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, Creditor::handleAllWorldsLoadedEvent);
    }

    public static void start(JavaPlugin host) {
        if (!containsGreatestCreditorVersion(host)) return;
    }

    /**
     * Once all the worlds are loaded (i.e. the server is fully set up and ready) we need to set up
     * the mod again if it's the latest version. This accounts for any pre-v.1.1.0 Creditor
     * instances which may have erroneously overridden the setup due to the old version management.
     */
    private static void handleAllWorldsLoadedEvent(AllWorldsLoadedEvent e) {
        if (containsGreatestCreditorVersion(host) &&
            !Objects.equals(System.getProperty("CREDITOR_HANDLED_ALL_WORLDS_LOADED"), "true")
        ) {
            // We're the greatest version and the reset hasn't yet been done by another equivalent versioned Creditor instance.
            System.setProperty("CREDITOR_HANDLED_ALL_WORLDS_LOADED", "true");

            // Force the asset map to use the class from this Creditor-instance by changing the key to point to our version of the class.
            AssetStore<?, ?, ?> creditAssetStore = AssetRegistry.getAssetStore(CreditAsset.class);
            if (creditAssetStore != null) {
                Map<Class<? extends JsonAssetWithMap<?,?>>, AssetStore<?, ?, ?>> assetMap = null;
                try {
                    Field assetMapField = AssetRegistry.class.getDeclaredField("storeMap");
                    assetMapField.setAccessible(true);
                    assetMap = (Map<Class<? extends JsonAssetWithMap<?,?>>, AssetStore<?, ?, ?>>) assetMapField.get(AssetMap.class);
                } catch (NoSuchFieldException | IllegalAccessException error) {
                    LOGGER.atSevere().withCause(error).log("Failed to update assetMap for Creditor. You may experience version mismatch problems.");
                }
                if (assetMap != null) {
                    assetMap.put(CreditAsset.class, CreditAsset.getAssetStore());
                }
            }

            host.getCommandRegistry().registerCommand(new CreditsCommand());
            reregisterCreditorContainingAssetPack();
        }
    }

    private static void reregisterCreditorContainingAssetPack() {
        PluginManifest manifest = VersionManagementUtils.getPluginManifest();
        String creditorAssetPackPathString = System.getProperty("CREDITOR_ASSET_PACK_PATH");
        if (creditorAssetPackPathString == null || manifest == null) return;
        Path creditorAssetPackPath = Paths.get(creditorAssetPackPathString);
        if (AssetModule.get().getAssetPack(manifest.getName()) != null) {
            AssetModule.get().unregisterPack(manifest.getName());
        }
        AssetModule.get().registerPack(
            manifest.getName(),
            creditorAssetPackPath,
            manifest,
            AssetPack.PackSource.CLASSPATH
        );
    }
}
