package top.znhua;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.Set;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static top.znhua.PrivateZone.*;

public class HomezCommand {
    public static void initCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerPrivateZone(dispatcher));
    }

    public static LiteralCommandNode registerPrivateZone(CommandDispatcher<ServerCommandSource> dispatcher) {
        return dispatcher.register(literal("homez")
                .then(literal("tp").executes(HomezCommand::commandHomezTp))
                .then(literal("back").executes(HomezCommand::commandHomezBack))
                .then(literal("create").executes(HomezCommand::commandHomezCreate))
                .then(literal("give")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(argument("dimIndex", integer())
                                .executes(HomezCommand::commandHomezGive)))
                .then(literal("init")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(argument("dimIndex", integer())
                                .executes(HomezCommand::commandHomezInit)))
                .then(literal("worldspawn").executes(HomezCommand::commandHomezWorldspawn))
                .then(literal("setspawn").executes(HomezCommand::commandHomezSetspawn))
        );
    }

    private static int commandHomezSetspawn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
        ServerCommandSource source = context.getSource();
        PlayerEntity player = source.getPlayerOrThrow();
        Vec3d playerPos = player.getPos();
        ServerWorld world = source.getWorld();

        // Check if player is in its own private zone
        if (world.getRegistryKey().getValue().getPath()
                .equals(player.getAttachedOrElse(AttachmentTypes.attachmentTypeOwnedDim, ""))) {
            // Attach playerPos(as spawn point) to server world
            world.setAttached(AttachmentTypes.attachmentTypeDimSpawnPoint, playerPos.toVector3f());
            source.sendFeedback(() -> Text.translatable("text.homez_command.setspawn.success"), false);
            // Reset obsidian platform
            InitializePrivateZone.resetObsidianPlatform(world, BlockPos.ofFloored(playerPos));
            return 1;
        } else {
            source.sendFeedback(() -> Text.translatable("text.homez_command.setspawn.not_in_private_zone"), false);
            return 0;
        }
    }

    private static int commandHomezWorldspawn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
        // Get world spawn point (in the overworld)
        ServerCommandSource source = context.getSource();
        ServerWorld overworld = source.getServer().getOverworld();
        BlockPos worldSpawn =overworld.getSpawnPos();
        PlayerEntity player = source.getPlayerOrThrow();
        player.teleport(overworld, worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ(), Set.of(), 0, 0);
        return 1;
    }

    private static int commandHomezInit(CommandContext<ServerCommandSource> context) {
        int dimIndex = getInteger(context, "dimIndex");
        String dimId = HOME_DIM_PREFIX + dimIndex;
        ServerCommandSource source = context.getSource();
        ServerWorld target = getServerWorldOfDimension(Identifier.of(PRIZ_NAMESPACE, dimId), source.getWorld());

        if (target != null) {
            if (InitializePrivateZone.isInitRequired(target)) {
                source.sendFeedback(() -> Text.translatable("text.homez_command.init.init_required"), false);
            } else {
                source.sendFeedback(() -> Text.translatable("text.homez_command.init.already_init"), false);
            }
            InitializePrivateZone.initPrivateZone(target);
            return 1;
        }
        else {
            source.sendFeedback(() -> Text.translatable("text.homez_command.init.not_found"), false);
        }
        return 0;
    }

    public static int commandHomezTp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int destX = 0;
        int destY = 70;
        int destZ = 0;

        final ServerCommandSource source = context.getSource();
        final PlayerEntity player = source.getPlayerOrThrow();
        String targetDimensionName = player.getAttached(AttachmentTypes.attachmentTypeOwnedDim);
        if (targetDimensionName == null) {  // Check if a home dimension is already created.
            source.sendFeedback(() -> Text.translatable("text.homez_command.tp.not_created"), false);
            return 0;
        }
        Identifier targetDimensionId = Identifier.of(PRIZ_NAMESPACE, targetDimensionName);
        ServerWorld targetWorld = getServerWorldOfDimension(targetDimensionId, source.getWorld());

        // Get target pos(spawn point)
        Vector3f spawnPointVector = (targetWorld.getAttached(AttachmentTypes.attachmentTypeDimSpawnPoint));
        if (spawnPointVector != null) {
            Vec3d spawnPointPos = new Vec3d(spawnPointVector);
            destX = (int) spawnPointPos.getX();
            destY = (int) spawnPointPos.getY();
            destZ = (int) spawnPointPos.getZ();
        }

        if (source.getWorld() != source.getServer().getOverworld()) {
            source.sendFeedback(() -> Text.translatable("text.homez_command.tp.not_overworld"), false);
            return 0;
        } else if (targetWorld != null) {
            // Found target world. Will teleport player.
            LOGGER.info("teleport player to " + targetDimensionName + " .");

            // Record previous player position.

            Vector3f prevPos = player.getPos().toVector3f();

            // Reset obsidian platform.
            InitializePrivateZone.resetObsidianPlatform(targetWorld, new BlockPos(destX, destY, destZ));

            // Teleport player onto previous obsidian platform.
            player.teleport(targetWorld, destX, destY, destZ, Set.of(), 0f, 0f);

            // Attach previous position to player.
            player.setAttached(AttachmentTypes.attachmentTypePrevPos, prevPos);
        } else {
            // Target world not found.
            LOGGER.info("target world or player not found.");
        }
        return 1;
    }

    public static int commandHomezBack(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        PlayerEntity player = source.getPlayerOrThrow();
        ServerWorld homeWorld = source.getWorld();

        // Get previous position in player attachment
        Vector3f prevPosTemp = player.getAttached(AttachmentTypes.attachmentTypePrevPos);
        if (prevPosTemp != null) {
            Vec3d prevPos = new Vec3d(prevPosTemp);
            // Get overworld
            ServerWorld overworld = homeWorld.getServer().getOverworld();
            // Teleport to previous position in overworld
            player.teleport(overworld, prevPos.x, prevPos.y, prevPos.z, Set.of(), 0f, 0f);
            return 1;
        } else {
            source.sendFeedback(() -> Text.translatable("text.homez_command.back.prev_not_found"), false);
            return 0;
        }
    }

    public static int commandHomezCreate(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // Distribute an "empty" dimension to player
        ServerCommandSource source = context.getSource();
        PlayerEntity player = source.getPlayerOrThrow();

        if (player.getAttached(AttachmentTypes.attachmentTypeOwnedDim) == null) {
            // For each custom dimension(priz:homedim$n), n is ranging from 1 to PrivateZone.HOME_DIM_LIMIT
            boolean existEmptyDim = false;
            for (int dimIndex = 1; dimIndex <= PrivateZone.HOME_DIM_LIMIT; dimIndex++) {
                // Get server-world of the dimension
                ServerWorld element = getServerWorldOfDimension(Identifier.of(PRIZ_NAMESPACE, HOME_DIM_PREFIX + dimIndex), source.getWorld());

                // Check if it is usable.
                if (element.getAttached(AttachmentTypes.attachmentTypeDimOwner) == null) {
                    // Distribute this dimension
                    existEmptyDim = true;

                    element.setAttached(AttachmentTypes.attachmentTypeDimOwner, player.getUuidAsString());
                    player.setAttached(AttachmentTypes.attachmentTypeOwnedDim, HOME_DIM_PREFIX + dimIndex);
                    // Initialize
                    InitializePrivateZone.initPrivateZone(element);
                    source.sendFeedback(() -> Text.translatable("text.homez_command.create.success"), false);
                    return 1;
                }
            }
            if (!existEmptyDim) {
                source.sendFeedback(() -> Text.translatable("text.homez_command.create.none_left"), false);
                return 0;
            }
        }
        source.sendFeedback(() -> Text.translatable("text.homez_command.create.already_exist"), false);
        return 0;
    }

    public static int commandHomezGive(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        PlayerEntity player = source.getPlayerOrThrow();
        int dimIndex = getInteger(context, "dimIndex");
        ServerWorld targetWorld = getServerWorldOfDimension(Identifier.of(PRIZ_NAMESPACE, HOME_DIM_PREFIX + dimIndex), source.getWorld());
        if (targetWorld != null && targetWorld.getAttached(AttachmentTypes.attachmentTypeDimOwner) == null) {
            // de-distribute original dimension of player
            String originalDimId = player.getAttached(AttachmentTypes.attachmentTypeOwnedDim);

            if (originalDimId != null) {
                ServerWorld originalWorld = getServerWorldOfDimension(Identifier.of(PRIZ_NAMESPACE, originalDimId), source.getWorld());
                originalWorld.removeAttached(AttachmentTypes.attachmentTypeDimOwner);
            }

            // distribute new dimension to player
            targetWorld.setAttached(AttachmentTypes.attachmentTypeDimOwner, player.getUuidAsString());
            player.setAttached(AttachmentTypes.attachmentTypeOwnedDim, HOME_DIM_PREFIX + dimIndex);

            InitializePrivateZone.initPrivateZone(targetWorld);

            source.sendFeedback(() -> Text.translatable("text.homez_command.give.success", player.getName(), dimIndex), false);
            return 1;
        } else {
            source.sendFeedback(() -> Text.translatable("text.homez_command.give.not_found_or_owned"), false);
            return 0;
        }
    }

    public static ServerWorld getServerWorldOfDimension(Identifier dimId, ServerWorld world) {
        RegistryKey<World> targetDimensionKey = RegistryKey.of(RegistryKeys.WORLD, dimId);
        ServerWorld targetWorld = world.getServer().getWorld(targetDimensionKey);
        return targetWorld;
    }
}
