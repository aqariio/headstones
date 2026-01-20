package aqario.headstones.common.network;

import aqario.headstones.common.Headstones;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public class HeadstonesEntityDataSerializers {
    public static final EntityDataSerializer<Optional<EntityReference<Player>>> OPTIONAL_PLAYER_REFERENCE = register(
        "optional_player_reference",
        EntityReference.<Player>streamCodec().apply(ByteBufCodecs::optional)
    );

    private static <T> EntityDataSerializer<T> register(String id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        EntityDataSerializer<T> serializer = EntityDataSerializer.forValueType(codec);
        FabricTrackedDataRegistry.register(Headstones.id(id), serializer);
        return serializer;
    }

    public static void init() {
    }
}
