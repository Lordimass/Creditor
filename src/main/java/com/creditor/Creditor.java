package com.creditor;

import com.creditor.command.CreditsCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

public class Creditor {

    private Creditor() {}

    // Used to prevent mod from being initialised multiple times if more than one mod is installed which uses Creditor as a library.
    private static boolean isSetupDone = false;
    private static boolean isStartDone = false;

    public static void setup(JavaPlugin host) {
        if (isSetupDone) return;

        host.getCommandRegistry().registerCommand(new CreditsCommand());

        isSetupDone = true;
    }

    public static void start(JavaPlugin host) {
        if (isStartDone) return;



        isStartDone = true;
    }
}
