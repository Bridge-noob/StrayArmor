package io.github.bridge_noob.strayarmor.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.bridge_noob.strayarmor.StrayArmorClient;
import io.github.bridge_noob.strayarmor.StrayArmorConfigManager;
import io.github.bridge_noob.strayarmor.TintContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {

    private static final String RENDER =
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V";

    private static final String RENDER_MODEL =
            "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "ILnet/minecraft/client/model/HumanoidModel;"
                    + "ILnet/minecraft/resources/ResourceLocation;)V";

    private static final String ARMOR_CUTOUT =
            "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull("
                    + "Lnet/minecraft/resources/ResourceLocation;)"
                    + "Lnet/minecraft/client/renderer/RenderType;";

    @Inject(method = RENDER, at = @At("HEAD"))
    private void strayarmor$beginArmor(PoseStack poseStack, MultiBufferSource buffers, int light,
                                       LivingEntity entity, float f, float g, float h,
                                       float j, float k, float l, CallbackInfo ci) {
        int tint = entity instanceof Player player ? StrayArmorClient.tintFor(player) : 0;
        TintContext.push(tint);
    }

    @Inject(method = RENDER, at = @At("RETURN"))
    private void strayarmor$endArmor(PoseStack poseStack, MultiBufferSource buffers, int light,
                                     LivingEntity entity, float f, float g, float h,
                                     float j, float k, float l, CallbackInfo ci) {
        TintContext.pop();
    }

    /**
     * In solid mode the armor is drawn from a flat white texture, so the vertex
     * color becomes the final color instead of being multiplied by the armor art.
     */
    @Redirect(method = RENDER_MODEL, at = @At(value = "INVOKE", target = ARMOR_CUTOUT))
    private RenderType strayarmor$solidTexture(ResourceLocation texture) {
        if (TintContext.current() != 0 && StrayArmorConfigManager.isSolidMode()) {
            return RenderType.armorCutoutNoCull(
                    ResourceLocation.fromNamespaceAndPath("strayarmor", "textures/white.png"));
        }
        return RenderType.armorCutoutNoCull(texture);
    }
}
