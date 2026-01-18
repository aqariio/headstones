package aqario.gravegoods.client.render;

import aqario.gravegoods.client.model.GraveModel;
import aqario.gravegoods.common.entity.GraveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class GraveRenderer extends EntityRenderer<GraveEntity, GraveRenderState> {
    private static final Identifier DEFAULT_TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png");
    private final Map<Boolean, GraveModel> models;
    private GraveModel model;

    public GraveRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.models = bakeModels(context);
        this.model = this.models.get(false);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    private static Map<Boolean, GraveModel> bakeModels(EntityRendererProvider.Context context) {
        return Map.of(
            false,
            new GraveModel(context.bakeLayer(GraveModel.DEFAULT_LAYER)),
            true,
            new GraveModel(context.bakeLayer(GraveModel.PLAYER_LAYER))
        );
    }

    @Override
    public void submit(GraveRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        this.model = this.models.get(state.owner != null);
        poseStack.pushPose();
        poseStack.translate(0.0, 1, 0.0);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        float bob = Mth.sin(state.ageInTicks / 10.0F + state.bobOffset) * 0.1F + 0.1F;
        poseStack.translate(0.0F, bob + 0.25F, 0.0F);
        float h = ItemEntity.getSpin(state.ageInTicks, state.bobOffset);
        poseStack.mulPose(Axis.YP.rotation(h));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        submitNodeCollector.submitModel(
            this.model,
            state,
            poseStack,
            this.model.renderType(this.getTextureLocation(state)),
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            state.outlineColor,
            null
        );
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    protected boolean shouldShowName(GraveEntity entity, double distance) {
        return entity.hasCustomName();
    }

    @NotNull
    public Identifier getTextureLocation(GraveRenderState state) {
        LocalPlayer player = state.owner;
        if(player != null) {
            return player.getSkin().body().texturePath();
        }
        return DEFAULT_TEXTURE;
    }

    @Override
    public GraveRenderState createRenderState() {
        return new GraveRenderState();
    }

    @Override
    public void extractRenderState(GraveEntity grave, GraveRenderState state, float f) {
        super.extractRenderState(grave, state, f);
        state.owner = (LocalPlayer) Optional.ofNullable(grave.getOwnerReference())
            .map(reference -> reference.getEntity(grave.level(), LivingEntity.class))
            .filter(e -> e instanceof LocalPlayer)
            .orElse(null);
        state.bobOffset = grave.bobOffs;
    }
}
