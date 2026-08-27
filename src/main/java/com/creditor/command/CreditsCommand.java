package com.creditor.command;

import com.creditor.ui.CreditsPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class CreditsCommand extends AbstractPlayerCommand {
    public CreditsCommand() {
        super("credits", "View full modlist and credits for modders.");
        addAliases("credit", "mod", "mods", "modlist", "assetpacks");

        requireNoPermission();
    }

    @Override
    protected void execute(
        @Nonnull CommandContext commandContext,
        @Nonnull Store<EntityStore> store,
        @Nonnull Ref<EntityStore> ref,
        @Nonnull PlayerRef playerRef,
        @Nonnull World world
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        PageManager pageManager = player.getPageManager();
        CustomUIPage currPage = pageManager.getCustomPage();
        if (currPage != null) return;

        CreditsPage page = new CreditsPage(playerRef);
        pageManager.openCustomPage(ref, store, page);
    }
}
