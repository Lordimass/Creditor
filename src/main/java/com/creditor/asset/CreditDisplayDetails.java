package com.creditor.asset;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record CreditDisplayDetails(
    @Nonnull Message name,
    @Nonnull String versionAndUrl,
    @Nonnull Message description,
    @Nonnull Message license,
    boolean hasDescription
) {
    @Nonnull
    public static CreditDisplayDetails from(@Nonnull PluginManifest manifest, @Nullable CreditAsset creditAsset) {
        Message name = creditAsset != null && creditAsset.getName() != null
            ? creditAsset.getName()
            : Message.raw(nonNullString(manifest.getName()));

        String version = creditAsset != null && creditAsset.getVersion() != null
            ? creditAsset.getVersion()
            : manifest.getVersion() != null ? manifest.getVersion().toString() : "";
        String website = creditAsset != null && creditAsset.getWebsite() != null
            ? creditAsset.getWebsite()
            : manifest.getWebsite();

        Message description;
        boolean hasDescription;
        if (creditAsset != null && creditAsset.getDescription() != null) {
            description = creditAsset.getDescription();
            hasDescription = true;
        } else if (manifest.getDescription() != null && !manifest.getDescription().isEmpty()) {
            description = Message.raw(manifest.getDescription());
            hasDescription = true;
        } else {
            description = Message.empty();
            hasDescription = false;
        }

        Message license = creditAsset != null && creditAsset.getLicense() != null
            ? creditAsset.getLicense()
            : Message.empty();

        return new CreditDisplayDetails(name, joinVersionAndUrl(version, website), description, license, hasDescription);
    }

    @Nonnull
    public String descriptionText() {
        return toDisplayText(this.description);
    }

    @Nonnull
    public String licenseText() {
        return toDisplayText(this.license);
    }

    @Nonnull
    private static String joinVersionAndUrl(@Nullable String version, @Nullable String website) {
        boolean hasVersion = version != null && !version.isEmpty();
        boolean hasWebsite = website != null && !website.isEmpty();
        if (hasVersion && hasWebsite) {
            return version + " • " + website;
        }
        if (hasVersion) {
            return version;
        }
        if (hasWebsite) {
            return website;
        }
        return "";
    }

    @Nonnull
    private static String nonNullString(@Nullable String value) {
        return value != null ? value : "";
    }

    @Nonnull
    private static String toDisplayText(@Nonnull Message message) {
        if (message.getRawText() != null && !message.getRawText().isEmpty()) {
            return message.getRawText();
        }

        String messageId = message.getMessageId();
        if (messageId != null && !messageId.isEmpty()) {
            I18nModule i18nModule = I18nModule.get();
            if (i18nModule != null) {
                String translated = i18nModule.getMessage(I18nModule.DEFAULT_LANGUAGE, messageId);
                if (translated != null && !translated.isEmpty()) {
                    return translated;
                }
            }
            return messageId;
        }

        return "";
    }
}
