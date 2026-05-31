package com.creditor.ui;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
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
    }

    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PluginListPage.PluginListPageEventData data) {
        assert this.playerSessionSettings != null;

        UICommandBuilder commandBuilder = new UICommandBuilder();
        UIEventBuilder eventBuilder = new UIEventBuilder();

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

    @Override
    public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
        PluginListPageManager.get().deregisterPluginListPage(this);
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

        int i = 0;
        for (int bound = this.availablePlugins.size(); i < bound; i++) {
            CreditsPage.PluginDetails plugin = this.availablePlugins.get(i);
            String desc = plugin.manifest.getDescription();
            if (!this.playerSessionSettings.descriptiveOnly || desc != null && !desc.isEmpty()) {
                this.visiblePlugins.add(plugin);
            }
        }

        i = 0;
        for (int bound = this.visiblePlugins.size(); i < bound; i++) {
            PluginDetails pluginDetails = this.visiblePlugins.get(i);
            PluginIdentifier identifier = pluginDetails.identifier;
            String id = identifier.toString();
            PluginBase loadedPlugin = module.getPlugin(identifier);
            if (loadedPlugin == null || !loadedPlugin.isEnabled()) {
                continue;
            }

            String selector = "#PluginList[" + i + "]";
            commandBuilder.append("#PluginList", "Pages/CreditsPageButton.ui");
            commandBuilder.set(selector + " #Button.Text", pluginDetails.manifest.getName());
            eventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating, selector + " #Button", new EventData().append("Plugin", id).append("Type", "Select"), false
            );
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
            commandBuilder.set("#PluginName.Text", nextSelectedPlugin.manifest.getName());

            String versionAndUrl = (nextSelectedPlugin.manifest.getVersion() != null ? nextSelectedPlugin.manifest.getVersion().toString() : "")
                + (nextSelectedPlugin.manifest.getWebsite() != null ? " • " + nextSelectedPlugin.manifest.getWebsite() : "");
            commandBuilder.set("#PluginVersion.Text", versionAndUrl);

            if (nextSelectedPlugin.manifest.getDescription() != null) {
                commandBuilder.set("#PluginDescription.Text", nextSelectedPlugin.manifest.getDescription());
            } else {
                commandBuilder.set("#PluginDescription.Text", "");
            }

            AtomicInteger i = new AtomicInteger();
            nextSelectedPlugin.manifest.getAuthors().forEach((author) -> {
                int top = 30*(i.get() + 1);
                String name = author.getName() != null ? author.getName() : "";
                String email = author.getEmail() != null ? author.getEmail() : "";
                String url = author.getUrl() != null ? author.getUrl() : "";
                commandBuilder.appendInline("#Authors",
                    "          Group #Author"+i+" {" +
                        "            Anchor: (Left: 0, Width: 200);" +
                        "            Label #Name {" +
                        "              Anchor: (Left: 0, Width: 200, Top: "+top+");" +
                        "              Padding: (Full: 5);" +
                        "              Text: \""+name+"\";" +
                        "              OutlineSize: 1.5;" +
                        "              OutlineColor: #473e26;" +
                        "              Background: #1b263a;" +
                        "            }" +
                        "            Label #Email {" +
                        "              Anchor: (Left: 200, Width: 200, Top: "+top+");" +
                        "              Padding: (Full: 5);" +
                        "              Text: \""+email+"\";" +
                        "              OutlineSize: 1.5;" +
                        "              OutlineColor: #473e26;" +
                        "              Background: #1b263a;" +
                        "            }" +
                        "            Label #Url {" +
                        "              Anchor: (Left: 400, Width: 200, Top: "+top+");" +
                        "              Padding: (Full: 5);" +
                        "              Text: \""+url+"\";" +
                        "              OutlineSize: 1.5;" +
                        "              OutlineColor: #473e26;" +
                        "              Background: #1b263a;" +
                        "            }" +
                        "          }");
                i.getAndIncrement();
            });

            this.selectedPlugin = nextSelectedPlugin;
        }
    }

    public void handlePluginChangeEvent(@Nonnull PluginIdentifier plugin, boolean activeState) {}

    private record PluginDetails(@Nonnull PluginManifest manifest,
                                 @Nonnull PluginIdentifier identifier) {
    }
}
