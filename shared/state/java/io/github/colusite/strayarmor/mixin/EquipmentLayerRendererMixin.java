package io.github.colusite.strayarmor.mixin;

import io.github.colusite.strayarmor.StrayArmorConfigManager;
import io.github.colusite.strayarmor.TintContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * In solid mode the armor is drawn from a flat white texture, so the vertex
 * color becomes the final color instead of being multiplied by the armor art.
 */
@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentLayerRendererMixin {

    private static final String RENDER_LAYERS =
            "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;"
                    + "Lnet/minecraft/resources/ResourceKey;"
                    + "Lnet/minecraft/client/model/Model;"
                    + "Lnet/minecraft/world/item/ItemStack;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;"
                    + "ILnet/minecraft/resources/ResourceLocation;)V";

    private static final String ARMOR_CUTOUT =
            "Lnet/minecraft/client/renderer/RenderType;armorCutoutNoCull("
                    + "Lnet/minecraft/resources/ResourceLocation;)"
                    + "Lnet/minecraft/client/renderer/RenderType;";

    @Redirect(method = RENDER_LAYERS, at = @At(value = "INVOKE", target = ARMOR_CUTOUT))
    private RenderType strayarmor$solidTexture(ResourceLocation texture) {
        if (TintContext.current() != 0 && StrayArmorConfigManager.isSolidMode()) {
            return RenderType.armorCutoutNoCull(
                    ResourceLocation.fromNamespaceAndPath("strayarmor", "textures/white.png"));
        }
        return RenderType.armorCutoutNoCull(texture);
    }
}
