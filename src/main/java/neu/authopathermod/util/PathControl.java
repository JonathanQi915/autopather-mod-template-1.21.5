package neu.authopathermod.util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class PathControl {
    private static Vec3d target;
    private static final double REACH_DISTANCE = 1.2;
    private static final double JUMP_HEIGHT_THRESHOLD = 0.8;

    public static void startPathing(Vec3d dest) {
        target = dest;
    }

    public static void tick(MinecraftClient client) {
        if (target == null || client.player == null) return;

        ClientPlayerEntity player = client.player;
        Vec3d pos = player.getPos();
        Vec3d direction = target.subtract(pos);
        double distance = direction.length();

        if (distance < REACH_DISTANCE) {
            stopWalking(client);
            player.sendMessage(Text.literal("Arrived!"), false);
            target = null;
            return;
        }

        // Turn player to face direction
        float yaw = (float)(Math.toDegrees(Math.atan2(-direction.x, direction.z)));
        player.setYaw(yaw);

        // Simulate walking forward
        KeyBinding forwardKey = client.options.forwardKey;
        forwardKey.setPressed(true);

        // Jump if Y difference is steep enough
        double yDiff = direction.y;
        if (yDiff > JUMP_HEIGHT_THRESHOLD && player.isOnGround()) {
            KeyBinding jumpKey = client.options.jumpKey;
            jumpKey.setPressed(true);
        } else {
            client.options.jumpKey.setPressed(false);
        }
    }

    public static void stopWalking(MinecraftClient client) {
        client.options.forwardKey.setPressed(false);
        client.options.jumpKey.setPressed(false);
    }

    public static void stopPathing() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            stopWalking(client);
            client.player.sendMessage(Text.literal("Pathing stopped."), false);
        }
        target = null;
    }
}