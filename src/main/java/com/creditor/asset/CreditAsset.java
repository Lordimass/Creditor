package com.creditor.asset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CreditAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, CreditAsset>> {
    public static final String ASSET_PATH = "Credits";
    public static final AssetBuilderCodec<String, CreditAsset> CODEC = AssetBuilderCodec.builder(
            CreditAsset.class,
            CreditAsset::new,
            Codec.STRING,
            CreditAsset::setId,
            CreditAsset::getId,
            CreditAsset::setData,
            CreditAsset::getData
        )
        .append(new KeyedCodec<>("Plugin", Codec.STRING), CreditAsset::setPlugin, CreditAsset::getPlugin)
        .documentation("Optional plugin identifier to override, formatted as Group:Name. Falls back to the asset id when omitted.")
        .add()
        .append(new KeyedCodec<>("Name", Message.CODEC), CreditAsset::setName, CreditAsset::getName)
        .documentation("Optional rich display name for the credited plugin.")
        .add()
        .append(new KeyedCodec<>("Version", Codec.STRING), CreditAsset::setVersion, CreditAsset::getVersion)
        .documentation("Optional display version override.")
        .add()
        .append(new KeyedCodec<>("Website", Codec.STRING), CreditAsset::setWebsite, CreditAsset::getWebsite)
        .documentation("Optional website override.")
        .add()
        .append(new KeyedCodec<>("Description", Message.CODEC), CreditAsset::setDescription, CreditAsset::getDescription)
        .documentation("Optional rich, localizable description shown on the credits page.")
        .add()
        .append(new KeyedCodec<>("License", Message.CODEC), CreditAsset::setLicense, CreditAsset::getLicense)
        .documentation("Optional rich, localizable license notice shown above the authors table.")
        .add()
        .build();

    @Nullable
    private static AssetStore<String, CreditAsset, DefaultAssetMap<String, CreditAsset>> assetStore;
    @Nullable @Getter @Setter
    private AssetExtraInfo.Data data;
    @Nullable @Getter @Setter
    private String id;
    @Nullable @Getter @Setter
    private String plugin;
    @Nullable @Getter @Setter
    private Message name;
    @Nullable @Getter @Setter
    private String version;
    @Nullable @Getter @Setter
    private String website;
    @Nullable @Getter @Setter
    private Message description;
    @Nullable @Getter @Setter
    private Message license;

    public CreditAsset() {}

    @Nonnull
    public static HytaleAssetStore<String, CreditAsset, DefaultAssetMap<String, CreditAsset>> createAssetStore() {
        return HytaleAssetStore.builder(CreditAsset.class, new DefaultAssetMap<>())
            .setPath(ASSET_PATH)
            .setCodec(CODEC)
            .setKeyFunction(CreditAsset::getId)
            .build();
    }

    @Nullable
    public static CreditAsset findByPlugin(@Nonnull String pluginIdentifier) {
        AssetStore<String, CreditAsset, DefaultAssetMap<String, CreditAsset>> store = getAssetStore();
        if (store == null) return null;
        for (CreditAsset asset : store.getAssetMap().getAssetMap().values()) {
            if (asset.matchesPlugin(pluginIdentifier)) {
                return asset;
            }
        }
        return null;
    }

    public boolean matchesPlugin(@Nonnull String pluginIdentifier) {
        if (this.plugin != null) {
            return this.plugin.equals(pluginIdentifier);
        }
        return pluginIdentifier.equals(this.id);
    }

    @Nullable
    public static AssetStore<String, CreditAsset, DefaultAssetMap<String, CreditAsset>> getAssetStore() {
        if (assetStore == null) {
            assetStore = com.hypixel.hytale.assetstore.AssetRegistry.getAssetStore(CreditAsset.class);
        }
        return assetStore;
    }
}
