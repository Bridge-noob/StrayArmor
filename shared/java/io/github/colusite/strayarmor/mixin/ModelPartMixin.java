package io.github.colusite.strayarmor.mixin;

import io.github.colusite.strayarmor.TintContext;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Tints model parts while an armor layer is rendering. The context is only
 * active inside {@code HumanoidArmorLayer}, so normal player/skin rendering is
 * untouched.
 */
@Mixin(ModelPart.class)
public abstract class ModelPartMixin {

    @ModifyVariable(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
            at = @At("HEAD"),
            argsOnly = true,
            index = 5)
    private int strayarmor$tintModelPart(int color) {
        return TintContext.apply(color);
    }
}
