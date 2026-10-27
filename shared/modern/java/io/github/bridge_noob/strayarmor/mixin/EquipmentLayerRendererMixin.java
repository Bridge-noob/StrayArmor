package io.github.bridge_noob.strayarmor.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.bridge_noob.strayarmor.StrayArmorConfigManager;
import io.github.bridge_noob.strayarmor.TintContext;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * In 1.21.11+ armor draws from submitted model nodes, so the tint has to be
 * baked into the node color instead of applied while the buffer is built.
 */
@Mixin(EquipmentLayerRenderer.class)
public abstract class EquipmentLayerRendererMixin {

    private static final String RENDER_LAYERS =
            "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;"
                    + "Lnet/minecraft/resources/ResourceKey;"
                    + "Lnet/minecraft/client/model/Model;"
                    + "Ljava/lang/Object;"
                    + "Lnet/minecraft/world/item/ItemStack;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "ILnet/minecraft/resources/Identifier;II)V";

    private static final String SUBMIT_MODEL =
            "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;"
                    + "submitModel(Lnet/minecraft/client/model/Model;"
                    + "Ljava/lang/Object;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/rendertype/RenderType;"
                    + "III"
                    + "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
                    + "I"
                    + "Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V";

    private static final String ARMOR_CUTOUT =
            "Lnet/minecraft/client/renderer/rendertype/RenderTypes;armorCutoutNoCull("
                    + "Lnet/minecraft/resources/Identifier;)"
                    + "Lnet/minecraft/client/renderer/rendertype/RenderType;";

    @Redirect(
            method = RENDER_LAYERS,
            at = @At(value = "INVOKE", target = ARMOR_CUTOUT))
    private RenderType strayarmor$solidTexture(Identifier texture) {
        if (TintContext.current() != 0 && StrayArmorConfigManager.isSolidMode()) {
            return RenderTypes.armorCutoutNoCull(
                    Identifier.fromNamespaceAndPath("strayarmor", "textures/white.png"));
        }
        return RenderTypes.armorCutoutNoCull(texture);
    }

    @Redirect(
            method = RENDER_LAYERS,
            at = @At(value = "INVOKE", target = SUBMIT_MODEL))
    private void strayarmor$tintSubmittedModel(
            OrderedSubmitNodeCollector collector,
            Model<?> model,
            Object state,
            PoseStack poseStack,
            RenderType renderType,
            int light,
            int overlay,
            int color,
            TextureAtlasSprite sprite,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        collector.submitModel((Model) model, state, poseStack, renderType, light, overlay,
                TintContext.apply(color), sprite, outlineColor, crumblingOverlay);
    }
}
