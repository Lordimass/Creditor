package com.creditor;

import com.creditor.asset.CreditAsset;
import com.creditor.command.CreditsCommand;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.AssetPackRegisterEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.world.events.AllWorldsLoadedEvent;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Creditor {
    private static JavaPlugin host;
    private static Boolean greatestVersion = null;
    /** Manifest of this version of the plugin if it is the latest version */
    private static PluginManifest pluginManifest;
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    /** Depending on version and context different names for Creditor may appear */
    private static final List<String> CREDITOR_NAMES = new ArrayList<>(List.of(new String[]{
        "com.creditor:Creditor", // Latest
        "com.creditor:creditor", // Pre v1.0.0 (bd763fbc6946d20cb2f4754404035fe6b57b9817)
        "Creditor", // Symlinked Development Environment Pack
        "creditor", // Symlinked Development Environment Pack Pre v1.0.0 (bd763fbc6946d20cb2f4754404035fe6b57b9817)
    }));

    public static void setup(JavaPlugin host) {
        Creditor.host = host;
        if (!isGreatestVersion(host)) return;

        AssetStore<?, ?, ?> creditAssetStore = AssetRegistry.getAssetStore(CreditAsset.class);
        if (creditAssetStore != null) AssetRegistry.unregister(AssetRegistry.getAssetStore(CreditAsset.class));
        host.getAssetRegistry().register(CreditAsset.createAssetStore());

        host.getCommandRegistry().registerCommand(new CreditsCommand());
        host.getEventRegistry().registerGlobal(AssetPackRegisterEvent.class, Creditor::handleAssetPackRegisterEvent);
        host.getEventRegistry().registerGlobal(AllWorldsLoadedEvent.class, Creditor::handleAllWorldsLoadedEvent);
    }

    public static void start(JavaPlugin host) {
        if (!isGreatestVersion(host)) return;

        // Late registering the asset pack to save on reloads from constantly unregistering and
        // registering asset packs as higher version are found. This way, for all >v.1.1.0 Creditor
        // instances, ever reaches registration.
        PluginManifest manifest = host.getManifest();
        if (AssetModule.get().getAssetPack(manifest.getName()) != null) {
            AssetModule.get().unregisterPack(manifest.getName());
        }

        AssetModule.get().registerPack(
            manifest.getName(),
            host.getFile(),
            host.getManifest(),
            AssetPack.PackSource.CLASSPATH
        );
    }

    /**
     * Once all the worlds are loaded (i.e. the server is fully set up and ready) we need to set up
     * the mod again if it's the latest version. This accounts for any pre-v.1.1.0 Creditor
     * instances which may have erroneously overridden the setup due to the old version management.
     */
    private static void handleAllWorldsLoadedEvent(AllWorldsLoadedEvent e) {
        if (isGreatestVersion(host) &&
            !Objects.equals(System.getProperty("CREDITOR_HANDLED_ALL_WORLDS_LOADED"), "true")
        ) {
            // We're the greatest version and the reset hasn't yet been done by another equivalent versioned Creditor instance.
            System.setProperty("CREDITOR_HANDLED_ALL_WORLDS_LOADED", "true");
            setup(host);
        }
    }

    /**
     * Whenever a new pack is registered, if it's not a Creditor instance, or if it's a
     * lower-version Creditor instance, re-register this pack to keep it at the end of the overwrite
     * priority.
     * <br>
     * We have to re-register even for non-Creditor asset packs since shadowing Creditor results in
     * its assets being merged into the resultant mod, so it's not detectable as Creditor.
     * <br>
     * This also prevents other packs from overwriting Creditor assets, ensuring Creditor keeps
     * priority.
     */
    private static void handleAssetPackRegisterEvent(AssetPackRegisterEvent e) {
        AssetPack newAssetPack = e.getAssetPack();
        PluginManifest newPluginManifest = newAssetPack.getManifest();

        // Check if this is a Creditor instance, check version and act accordingly
        if (CREDITOR_NAMES.contains(newPluginManifest.getName())) {
            Semver newSemver = newPluginManifest.getVersion();
            Semver greatestSemver = getGreatestSemver();
            // If version is greater or equal to the greatest version, all is well and we can move on.
            if (greatestSemver != null && newSemver.compareTo(greatestSemver) >= 0) return;
            AssetModule.get().unregisterPack(newAssetPack.getName());
        }

        // Reregister greatest version of Creditor.
        String creditorAssetPackPathString = System.getProperty("CREDITOR_ASSET_PACK_PATH");
        if (creditorAssetPackPathString == null || Creditor.pluginManifest == null) return;
        Path creditorAssetPackPath = Paths.get(creditorAssetPackPathString);
        if (AssetModule.get().getAssetPack("com.creditor:Creditor") != null) {
            AssetModule.get().unregisterPack("com.creditor:Creditor");
        }
        AssetModule.get().registerPack(
            "com.creditor:Creditor",
            creditorAssetPackPath,
            Creditor.pluginManifest,
            AssetPack.PackSource.CLASSPATH
        );
    }

    private static boolean isGreatestVersion(JavaPlugin host) {
        if (greatestVersion != null) return greatestVersion;
        Semver greatestSemver = getGreatestSemver();
        Semver thisSemver = host.getManifest().getVersion();
        if (greatestSemver != null && thisSemver.compareTo(greatestSemver) <= 0) {
            greatestVersion = false;
            return greatestVersion;
        }
        // Set system property for global tracking of the latest found version of Creditor so far.
        System.setProperty("CREDITOR_VERSION", thisSemver.toString());
        // Set system property so that the asset pack can be re-registered if a pre v1.1.0 version
        // of Creditor overwrites the assets due to its outdated version conflict management.
        System.setProperty("CREDITOR_ASSET_PACK_PATH", host.getFile().toString());
        Creditor.pluginManifest = host.getManifest();

        greatestVersion = true;
        return greatestVersion;
    }

    @Nullable
    private static Semver getGreatestSemver() {
        String greatestVerString = System.getProperty("CREDITOR_VERSION");
        return greatestVerString != null
            ? Semver.fromString(greatestVerString)
            : null;
    }
}
