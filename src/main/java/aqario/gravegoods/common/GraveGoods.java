package aqario.gravegoods.common;

import aqario.gravegoods.common.config.GraveGoodsConfig;
import aqario.gravegoods.common.entity.GraveGoodsEntityType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraveGoods implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Grave Goods");
    public static final String ID = "gravegoods";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static boolean isTrinketsLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Loading Grave Goods");
        GraveGoodsConfig.init(ID, GraveGoodsConfig.class);
        GraveGoodsEntityType.init();
    }
}
