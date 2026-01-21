package aqario.headstones.client.render;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.jspecify.annotations.Nullable;

public class GraveRenderState extends EntityRenderState {
    @Nullable
    public PlayerInfo owner;
    public float bobOffset;
}
