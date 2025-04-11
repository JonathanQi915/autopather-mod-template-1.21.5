package neu.authopathermod.util;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import neu.authopathermod.command.PathXYZ;

public class ModRegistries {
    public static void registerModStuffs() {
        registerCommands();
    }

    private static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(PathXYZ::register);
    }
}