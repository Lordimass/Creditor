package com.creditor.ui;

import com.creditor.Main;
import com.creditor.asset.CreditAsset;
import com.creditor.asset.CreditDisplayDetails;
import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.builtin.hytalegenerator.assets.AssetManager;
import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginListPageManager;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.plugin.pages.PluginListPage;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CreditsPage extends PluginListPage {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final Value<String> BUTTON_LABEL_STYLE = Value.ref("Pages/CreditsPageButton.ui", "LabelStyle");
    private static final Value<String> BUTTON_LABEL_STYLE_SELECTED = Value.ref("Pages/CreditsPageButton.ui", "SelectedLabelStyle");
    @Nullable
    private CreditsPage.PluginDetails selectedPlugin;
    @Nonnull
    private final ObjectList<CreditsPage.PluginDetails> availablePlugins = new ObjectArrayList<>();
    @Nonnull
    private final ObjectList<CreditsPage.PluginDetails> visiblePlugins = new ObjectArrayList<>();
    @Nullable
    private PluginListPageManager.SessionSettings playerSessionSettings;

    public CreditsPage(@Nonnull PlayerRef playerRef) {
        super(playerRef);
    }

    @Override
    public void build(
        @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
    ) {
        PluginListPageManager pageManager = PluginListPageManager.get();
        pageManager.registerPluginListPage(this);
        this.playerSessionSettings = store.ensureAndGetComponent(ref, PluginListPageManager.SessionSettings.getComponentType());
        commandBuilder.append("Pages/CreditsPage.ui");
        this.buildPluginList(commandBuilder, eventBuilder);
        if (!this.visiblePlugins.isEmpty()) {
            this.selectPlugin(this.visiblePlugins.getFirst().identifier.toString(), commandBuilder);
        }
        if (Main.isSupporter()) buildSupporterBadge(commandBuilder);
    }

    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PluginListPage.PluginListPageEventData data) {
        assert this.playerSessionSettings != null;
        UICommandBuilder commandBuilder = new UICommandBuilder();

        // Fields are private in base PluginListPage Hytale class, use reflection to access them here.
        String plugin;
        try {
            Field dataPlugin = PluginListPageEventData.class.getDeclaredField("plugin");
            dataPlugin.setAccessible(true);
            plugin = (String) dataPlugin.get(data);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOGGER.atSevere().log("Could not access field when handling CreditsPage data event:", e);
            return;
        }

        if (plugin != null) {
            this.selectPlugin(plugin, commandBuilder);
            this.sendUpdate(commandBuilder, null, false);
        }
    }

    private void buildPluginList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
        assert this.playerSessionSettings != null;

        commandBuilder.clear("#PluginList");
        this.visiblePlugins.clear();
        this.availablePlugins.clear();
        PluginManager module = PluginManager.get();
        Map<PluginIdentifier, PluginManifest> loadedPlugins = module.getAvailablePlugins();
        loadedPlugins.forEach((id, manifest) -> {
            // Don't include base game plugins. This is for installed mods.
            if (manifest.getGroup().equals("Hytale")) return;
            this.availablePlugins.add(new CreditsPage.PluginDetails(manifest, id));
        });
        AssetModule.get().getAssetPacks().forEach(assetPack -> {
            PluginIdentifier identifier = new PluginIdentifier(assetPack.getManifest());
            if (assetPack.isCoreMod() || loadedPlugins.containsKey(identifier)) return;
            this.availablePlugins.add(new CreditsPage.PluginDetails(assetPack.getManifest(), identifier));
        });

        int i = 0;
        int count = 0;
        for (int bound = this.availablePlugins.size(); i < bound; i++) {
            PluginDetails pluginDetails = this.availablePlugins.get(i);
            PluginIdentifier identifier = pluginDetails.identifier;
            String id = identifier.toString();
            PluginBase loadedPlugin = module.getPlugin(identifier);
            if (loadedPlugin != null && loadedPlugin.isDisabled()) continue;

            visiblePlugins.add(pluginDetails);
            String selector = "#PluginList[" + count + "]";
            commandBuilder.append("#PluginList", "Pages/CreditsPageButton.ui");
            commandBuilder.set(selector + " #Button.Text", pluginDetails.manifest.getName());
            eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating, selector + " #Button", new EventData().append("Plugin", id).append("Type", "Select"), false
            );
            count++;
        }
    }

    private void selectPlugin(@Nonnull String playerSelectedPlugin, @Nonnull UICommandBuilder commandBuilder) {
        CreditsPage.PluginDetails nextSelectedPlugin = null;

        for (PluginDetails plugin : this.visiblePlugins) {
            if (playerSelectedPlugin.equals(plugin.identifier.toString())) {
                nextSelectedPlugin = plugin;
                break;
            }
        }

        if (nextSelectedPlugin != null) {
            if (this.selectedPlugin != null && this.visiblePlugins.contains(this.selectedPlugin)) {
                commandBuilder.set("#PluginList[" + this.visiblePlugins.indexOf(this.selectedPlugin) + "] #Button.Style", BUTTON_LABEL_STYLE);
            }

            commandBuilder.set("#PluginList[" + this.visiblePlugins.indexOf(nextSelectedPlugin) + "] #Button.Style", BUTTON_LABEL_STYLE_SELECTED);
            CreditDisplayDetails details = CreditDisplayDetails.from(
                nextSelectedPlugin.manifest, CreditAsset.findByPlugin(nextSelectedPlugin.identifier.toString())
            );

            commandBuilder.set("#PluginName.Text", nextSelectedPlugin.manifest.getName());

            commandBuilder.set("#PluginVersion.Text", details.versionAndUrl());
            commandBuilder.set("#PluginDescription.Text", details.descriptionText());
            String licenseText = details.licenseText();
            commandBuilder.set("#PluginLicense.Visible", !licenseText.isEmpty());
            commandBuilder.set("#PluginLicense.Text", licenseText);
            buildAuthorTable(commandBuilder, nextSelectedPlugin);

            this.selectedPlugin = nextSelectedPlugin;
        }
    }

    private void buildAuthorTable(@Nonnull UICommandBuilder commandBuilder, PluginDetails nextSelectedPlugin) {
        commandBuilder.clear("#Authors");
        if (!nextSelectedPlugin.manifest.getAuthors().isEmpty()) {
            commandBuilder.append("#Authors", "Pages/AuthorsTableHeader.ui");
        }
        AtomicInteger i = new AtomicInteger(1);
        nextSelectedPlugin.manifest.getAuthors().forEach((author) -> {
            String selector = "#Authors["+i+"]";
            commandBuilder.append("#Authors", "Pages/AuthorsTableRow.ui");
            if (author.getName() != null) {
                commandBuilder.set(selector + " #Name.Text", author.getName());
                commandBuilder.set(selector + " #Name.TooltipText", author.getName());
            }
            if (author.getEmail() != null) {
                commandBuilder.set(selector + " #Email.Text", author.getEmail());
                commandBuilder.set(selector + " #Email.TooltipText", author.getEmail());
            }
            if (author.getUrl() != null) {
                commandBuilder.set(selector + " #Url.Text", author.getUrl());
                commandBuilder.set(selector + " #Url.TooltipText", author.getUrl());
            }

            i.getAndIncrement();
        });
    }

    public void handlePluginChangeEvent(@Nonnull PluginIdentifier plugin, boolean activeState) {
        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();
        CreditsPage.PluginDetails key = null;
        int i = 0;

        for (int bound = this.visiblePlugins.size(); i < bound; i++) {
            CreditsPage.PluginDetails details = this.visiblePlugins.get(i);
            if (details.identifier.equals(plugin)) {
                key = details;
                break;
            }
        }

        if (key != null) {
            this.sendUpdate(commandBuilder, eventBuilder, false);
        }
    }

    private void buildSupporterBadge(@Nonnull UICommandBuilder commandBuilder) {
        // Inlined to add a layer of difficulty for anyone trying to spoof this.
        // If it were included as a .ui file it would be incredibly easy to just add the badge externally.
        commandBuilder.appendInline("#Title", """
                  AssetImage {
                    AssetPath: "UI/Custom/Common/Checkmark.png";
                    Anchor: (Top: 6, Right: 15, Width: 20, Height: 20);
                    TooltipText: %creditor.credits.supporter.tooltip;
                    TextTooltipStyle: (
                      Background: (TexturePath: "Common/TooltipDefaultBackground.png", Border: 24),
                      MaxWidth: 400,
                      LabelStyle: (Wrap: true, FontSize: 16),
                      Padding: 24
                    );
                  }
            """);
    }

    private record PluginDetails(@Nonnull PluginManifest manifest,
                                 @Nonnull PluginIdentifier identifier) {
    }
}
