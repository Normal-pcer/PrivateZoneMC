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

    public static final AttachmentType<String> attachmentTypeDimOwner = stringBuilder
            .persistent(Codecs.NON_EMPTY_STRING)
            .buildAndRegister(Identifier.of(PrivateZone.PRIZ_NAMESPACE, "dim_owner"));
    // attachmentTypeDimOwner: attach on server-worlds, which stores UUID of owner.
}
