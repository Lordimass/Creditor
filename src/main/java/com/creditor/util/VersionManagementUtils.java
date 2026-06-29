package com.creditor.util;

import com.creditor.BuildInfo;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

public class VersionManagementUtils {
    private static Boolean greatestVersion = null;
    /** Manifest of this version of the host plugin if it contains the latest Creditor version */
    @Getter private static PluginManifest pluginManifest;
    private VersionManagementUtils() {}

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static boolean containsGreatestCreditorVersion(JavaPlugin host) {
        if (greatestVersion != null) return greatestVersion;
        Semver greatestSemver = getGreatestSemver();
        Semver thisSemver = Semver.fromString(BuildInfo.VERSION);
        if (greatestSemver != null && thisSemver.compareTo(greatestSemver) <= 0) {
            greatestVersion = false;
            return greatestVersion;
        }
        // Set system property for global tracking of the latest found version of Creditor so far.
        System.setProperty("CREDITOR_VERSION", thisSemver.toString());
        // Set system property so that the asset pack can be re-registered if a mod containing a
        // pre v1.1.0 version of Creditor overwrites the assets due to its outdated version conflict
        // management.
        System.setProperty("CREDITOR_ASSET_PACK_PATH", host.getFile().toString());
        pluginManifest = host.getManifest();

        greatestVersion = true;
        return greatestVersion;
    }

    @Nullable
    public static Semver getGreatestSemver() {
        String greatestVerString = System.getProperty("CREDITOR_VERSION");
        return greatestVerString != null
            ? Semver.fromString(greatestVerString)
            : null;
    }
}
