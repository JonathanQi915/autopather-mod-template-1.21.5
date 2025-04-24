package neu.authopathermod.util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import neu.authopathermod.Node;

import java.util.ArrayList;
import java.util.List;

public class PathControl {
    private static List<BlockPos> path = new ArrayList<>();
    private static int currentIndex = 0;
    private static final double REACH_DISTANCE = 1.2;
    private static final double JUMP_HEIGHT_THRESHOLD = 0.8;
    private static int particleTickCounter = 0;

    public static void startPathing(World world, Vec3d start, Vec3d dest) {
        BlockPos startBlock = new BlockPos((int) start.x, (int) start.y, (int) start.z);
        BlockPos goalBlock = new BlockPos((int) dest.x, (int) dest.y, (int) dest.z);

        path = Node.findPath(world, startBlock, goalBlock, 1000); // You can tweak maxNodes
        currentIndex = 0;

        if (path.isEmpty()) {
            System.out.println("No path found.");
        }

        showPath(path);
    }

    public static void tick(MinecraftClient client) {
        if (path == null || path.isEmpty() || client.player == null) return;

        ClientPlayerEntity player = client.player;
        Vec3d pos = player.getPos();

        if (currentIndex >= path.size()) {
            stopWalking(client);
            player.sendMessage(Text.literal("Arrived!"), false);
            path = null;
            return;
        }

        BlockPos targetBlock = path.get(currentIndex);
        Vec3d targetVec = Vec3d.ofCenter(targetBlock);
        player.sendMessage(Text.literal(targetVec.x + " " + targetVec.y + " " + targetVec.z),false);
        Vec3d direction = targetVec.subtract(pos);
        double distance = direction.length();

        if (distance < REACH_DISTANCE) {
            currentIndex++;
            return;
        }

        if (particleTickCounter % 5 == 0) {
            showPath(path);
        }
        particleTickCounter = particleTickCounter + 1;

        // Turn player to face direction
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        player.setYaw(yaw);

        // Walk forward
        client.options.forwardKey.setPressed(true);

        // Jump if needed
        double yDiff = direction.y;
        if (yDiff > JUMP_HEIGHT_THRESHOLD && player.isOnGround()) {
            client.options.jumpKey.setPressed(true);
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
            currentIndex = 0;
            path = new ArrayList<>();
            client.player.sendMessage(Text.literal("Pathing stopped."), false);

        }
    }

    public static void showPath(List<BlockPos> path) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        if (world == null) return;

        for (BlockPos pos : path) {
            world.addParticleClient(
                    ParticleTypes.HAPPY_VILLAGER,  // or CRIT, FLAME, etc.
                    pos.getX() + 0.5,  // center of block
                    pos.getY() + 0.1,
                    pos.getZ() + 0.5,
                    0, 0.01, 0  // small vertical motion
            );
        }
    }
}