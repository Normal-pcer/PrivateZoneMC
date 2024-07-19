package top.znhua;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class InitializePrivateZone {
    public static boolean isInitRequired(ServerWorld world) {
        Integer initializeStatusCode = world.getAttached(AttachmentTypes.attachmentTypeDimInitialized);
        return initializeStatusCode == null || initializeStatusCode == 0;
    }

    public static void initPrivateZone(ServerWorld world) {
        if (!isInitRequired(world)) return;
        // Fill 6*6 chunks' land
        resetMainland(world);

        // Set fence around the land
        resetMainlandFences(world);
        world.setAttached(AttachmentTypes.attachmentTypeDimInitialized, 1);
    }

    public static void resetMainland(ServerWorld world) {
        fill36Chunks(world, -128, -128, Blocks.BEDROCK);
        fill36Chunks(world, -127, -1, Blocks.DEEPSLATE);
        fill36Chunks(world, 0, 64, Blocks.STONE);
        fill36Chunks(world, 65, 67, Blocks.DIRT);
        fill36Chunks(world, 68, 68, Blocks.GRASS_BLOCK);
    }

    public static void resetMainlandFences(ServerWorld world) {
        for (int i = -48; i <= 47; i++) {
            world.setBlockState(new BlockPos(i, 69, 47), Blocks.OAK_FENCE.getDefaultState());
            world.setBlockState(new BlockPos(i, 69, -48), Blocks.OAK_FENCE.getDefaultState());
            world.setBlockState(new BlockPos(47, 69, i), Blocks.OAK_FENCE.getDefaultState());
            world.setBlockState(new BlockPos(-48, 69, i), Blocks.OAK_FENCE.getDefaultState());
        }
    }

    public static void resetObsidianPlatform(ServerWorld world, BlockPos pos) {
        int destX = pos.getX();
        int destY = pos.getY();
        int destZ = pos.getZ();
        // Pos: center of obsidian.
        fillBlockStates(world, new BlockPos(destX - 1, destY - 1, destZ - 1), new BlockPos(destX + 1, destY - 1, destZ + 1), Blocks.OBSIDIAN.getDefaultState());
        fillBlockStates(world, new BlockPos(destX - 1, destY, destZ - 1), new BlockPos(destX + 1, destY + 2, destZ + 1), Blocks.AIR.getDefaultState());
    }

    private static void fill36Chunks(ServerWorld world, int floor, int ceil, Block targetBlock) {
        fillBlockStates(world, new BlockPos(-48, floor, -48), new BlockPos(47, ceil, 47), targetBlock.getDefaultState());
    }

    private static void fillBlockStates(ServerWorld world, BlockPos corner1, BlockPos corner2, BlockState targetBlock) {
        for (int x = corner1.getX(); x <= corner2.getX(); x++) {
            for (int y = corner1.getY(); y <= corner2.getY(); y++) {
                for (int z = corner1.getZ(); z <= corner2.getZ(); z++) {
                    world.setBlockState(new BlockPos(x, y, z), targetBlock);
                }
            }
        }
    }
}
