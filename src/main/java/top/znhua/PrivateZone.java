package top.znhua;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static net.minecraft.server.command.CommandManager.literal;

public class PrivateZone implements ModInitializer {
	public static final String PRIZ_NAMESPACE = "priz";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(PRIZ_NAMESPACE);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fucking Fabric world!");

		initCommands();
	}

	public void initCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerPrivateZone(dispatcher));
	}

	public static LiteralCommandNode registerPrivateZone(CommandDispatcher<ServerCommandSource> dispatcher) {
		return dispatcher.register( literal("homez").executes(PrivateZone::commandHomez) );
	}

	public static int commandHomez(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		final ServerCommandSource source = context.getSource();
		final PlayerEntity player = source.getPlayer();
		String targetDimensionName = "homedim1";
		Identifier targetDimensionId = Identifier.of(PRIZ_NAMESPACE, targetDimensionName);
		RegistryKey<World> targetDimensionKey = RegistryKey.of(RegistryKeys.WORLD, targetDimensionId);
		ServerWorld targetWorld = source.getWorld().getServer().getWorld(targetDimensionKey);
		if (targetWorld != null && player != null) {
			// Found target world. Will teleport player.
			LOGGER.info("teleport player to "+targetDimensionName+" .");
            player.teleport(targetWorld, 0, 70, 0, Set.of(), 0f, 0f);
        } else {
			// Target world not found.
			LOGGER.info("target world or player not found.");
		}
		return 0;
    }
}