package aqario.gravegoods.client;

import aqario.gravegoods.client.model.GraveModel;
import aqario.gravegoods.client.render.GraveRenderer;
import aqario.gravegoods.common.entity.GraveGoodsEntityType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class GraveGoodsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRenderers.register(GraveGoodsEntityType.GRAVE, GraveRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(GraveModel.DEFAULT_LAYER, GraveModel::createDefaultLayer);
        EntityModelLayerRegistry.registerModelLayer(GraveModel.PLAYER_LAYER, GraveModel::createPlayerLayer);
    }
}
