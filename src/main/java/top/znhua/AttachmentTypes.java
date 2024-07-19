package top.znhua;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import org.joml.Vector3f;

import static net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry.builder;

public class AttachmentTypes {
    private static final AttachmentRegistry.Builder<Vector3f> vectorBuilder = builder();
    private static final AttachmentRegistry.Builder<String> stringBuilder = builder();
    private static final AttachmentRegistry.Builder<Integer> integerBuilder = builder();

    public static final AttachmentType<Vector3f> attachmentTypePrevPos = vectorBuilder
            .persistent(Codecs.VECTOR_3F)
            .copyOnDeath()
            .buildAndRegister(Identifier.of(PrivateZone.PRIZ_NAMESPACE, "prev_overworld_pos"));
    // attachmentTypePrevPos: attach on players

    public static final AttachmentType<String> attachmentTypeOwnedDim = stringBuilder
            .persistent(Codecs.NON_EMPTY_STRING)
            .copyOnDeath()
            .buildAndRegister(Identifier.of(PrivateZone.PRIZ_NAMESPACE, "owned_dim"));
    // attachmentTypeOwnedDim: attach on players. Contains "homedim" prefix

    private static final AttachmentRegistry.Builder<String> attachmentTypeDimOwnerBuilder = builder();
    public static final AttachmentType<String> attachmentTypeDimOwner = attachmentTypeDimOwnerBuilder
            .persistent(Codecs.NON_EMPTY_STRING)
            .buildAndRegister(Identifier.of(PrivateZone.PRIZ_NAMESPACE, "dim_owner"));
    // attachmentTypeDimOwner: attach on server-worlds, which stores UUID of owner.

    public static final AttachmentType<Integer> attachmentTypeDimInitialized = integerBuilder
            .persistent(Codecs.NONNEGATIVE_INT)
            .buildAndRegister(Identifier.of(PrivateZone.PRIZ_NAMESPACE, "dim_initialized"));
    // attachmentTypeDimInitialized: attach on server-worlds, which stores 1 if initialized, 0 if not.

    private static final AttachmentRegistry.Builder<Vector3f> attachmentTypeDimSpawnPointBuilder = builder();
    public static final AttachmentType<Vector3f> attachmentTypeDimSpawnPoint = attachmentTypeDimSpawnPointBuilder
            .persistent(Codecs.VECTOR_3F)
            .buildAndRegister(Identifier.of(PrivateZone.PRIZ_NAMESPACE, "dim_spawn_point"));
    // attachmentTypeDimSpawnPoint: attach on server-worlds, which stores spawn point.

    public static void init() {
        PrivateZone.LOGGER.info(String.valueOf(attachmentTypeDimSpawnPoint));
    }
}
