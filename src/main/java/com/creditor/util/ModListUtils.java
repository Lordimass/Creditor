package com.creditor.util;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.plugin.PluginBase;

import javax.annotation.Nullable;

public class ModListUtils {
    public static boolean isExcluded(@Nullable PluginBase plugin) {
        if (plugin == null ||
            plugin.isDisabled() ||
            plugin.getState().isInactive()
        ) return true;

        PluginManifest manifest = plugin.getManifest();
        String name = manifest.getName();

        return name.endsWith("_Patchly") || // Patchly Synthetic Asset Packs
            name.endsWith("Hytaylor-Overrides") || // Hytaylor Synthetic Asset Packs
            name.startsWith("_"); // e.g. Perl:_Defensive
    }
}
