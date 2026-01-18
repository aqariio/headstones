package aqario.gravegoods.mixin;

import aqario.gravegoods.common.entity.GraveEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {
    @ModifyArg(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"), index = 0)
    private Packet<?> gravegoods$changeGraveMenuName(Packet<?> packet, @Local AbstractContainerMenu screenHandler, @Local(argsOnly = true) @Nullable MenuProvider factory) {
        if(factory instanceof GraveEntity grave) {
            Component containerTitle = grave.getCustomName() == null
                ? Component.translatable("container.gravegoods.grave_unknown")
                : Component.translatable("container.gravegoods.grave", grave.getCustomName());
            return new ClientboundOpenScreenPacket(screenHandler.containerId, screenHandler.getType(), containerTitle);
        }
        return packet;
    }
}
