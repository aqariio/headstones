package aqario.gravegoods.common.entity;

import aqario.gravegoods.common.GraveGoods;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class GraveGoodsEntityType {
    public static final EntityType<GraveEntity> GRAVE = register("grave",
        EntityType.Builder.of(GraveEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
    );

    private static ResourceKey<EntityType<?>> entityId(String id) {
        return ResourceKey.create(Registries.ENTITY_TYPE, GraveGoods.id(id));
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> builder) {
        return register(entityId(id), builder);
    }

    private static <T extends Entity> EntityType<T> register(ResourceKey<EntityType<?>> key, EntityType.Builder<T> builder) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void init() {
    }
}
