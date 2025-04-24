package neu.authopathermod.command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import neu.authopathermod.util.PathControl;

public class PathXYZ {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("pathXYZ")
                .then(CommandManager.argument("x", DoubleArgumentType.doubleArg())
                        .then(CommandManager.argument("y", DoubleArgumentType.doubleArg())
                                .then(CommandManager.argument("z", DoubleArgumentType.doubleArg())
                                        .executes(PathXYZ::run))))
                .then(CommandManager.literal("stop")
                        .executes(PathXYZ::stop))
        );
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        double x = DoubleArgumentType.getDouble(context, "x");
        double y = DoubleArgumentType.getDouble(context, "y");
        double z = DoubleArgumentType.getDouble(context, "z");

        // Client-side only: grab client player and world
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.world != null) {
            Vec3d destination = new Vec3d(x, y, z);
            Vec3d current = client.player.getPos();

            PathControl.startPathing(client.world, current, destination);
        } else {
            context.getSource().sendFeedback(() -> Text.literal("Client not ready"), false);
            return 0;
        }

        context.getSource().sendFeedback(() -> Text.literal("Walking to " + x + ", " + y + ", " + z), false);
        return 1;
    }

    private static int stop(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftClient client = MinecraftClient.getInstance();
        PathControl.stopPathing();
        context.getSource().sendFeedback(() -> Text.literal("Stopping"), false);
        return 1;
    }
}
