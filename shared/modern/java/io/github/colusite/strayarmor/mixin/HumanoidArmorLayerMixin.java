package io.github.colusite.strayarmor.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.colusite.strayarmor.StrayArmorClient;
import io.github.colusite.strayarmor.TintContext;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    private static final String SUBMIT =
            "submit(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V";

    @Inject(method = SUBMIT, at = @At("HEAD"))
    private void strayarmor$beginArmor(PoseStack poseStack, SubmitNodeCollector collector, int light,
                                       HumanoidRenderState state, float yRot, float xRot, CallbackInfo ci) {
        int tint = state instanceof AvatarRenderState avatar
                ? StrayArmorClient.tintForState(avatar.id, null)
                : 0;
        TintContext.push(tint);
    }

    @Inject(method = SUBMIT, at = @At("RETURN"))
    private void strayarmor$endArmor(PoseStack poseStack, SubmitNodeCollector collector, int light,
                                     HumanoidRenderState state, float yRot, float xRot, CallbackInfo ci) {
        TintContext.pop();
    }
}
