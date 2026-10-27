package io.github.bridge_noob.strayarmor.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.bridge_noob.strayarmor.StrayArmorClient;
import io.github.bridge_noob.strayarmor.TintContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    private static final String RENDER =
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V";

    @Inject(method = RENDER, at = @At("HEAD"))
    private void strayarmor$beginArmor(PoseStack poseStack, MultiBufferSource buffers, int light,
                                       HumanoidRenderState state, float f, float g, CallbackInfo ci) {
        int tint = state instanceof PlayerRenderState playerState
                ? StrayArmorClient.tintForState(playerState.id, playerState.name)
                : 0;
        TintContext.push(tint);
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void strayarmor$endArmor(PoseStack poseStack, MultiBufferSource buffers, int light,
                                     HumanoidRenderState state, float f, float g, CallbackInfo ci) {
        TintContext.pop();
    }
}
