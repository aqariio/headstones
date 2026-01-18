package aqario.gravegoods.client.render;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.Nullable;

public class GraveRenderState extends EntityRenderState {
    @Nullable
    public LocalPlayer owner;
    public float bobOffset;
}
