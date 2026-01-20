package aqario.headstones.common;

import aqario.headstones.common.config.HeadstonesConfig;
import aqario.headstones.common.entity.HeadstonesEntityTypes;
import aqario.headstones.common.network.HeadstonesEntityDataSerializers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Headstones implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Headstones");
    public static final String ID = "headstones";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ID, path);
    }

    public static boolean isTrinketsLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Loading Headstones");
        HeadstonesConfig.init(ID, HeadstonesConfig.class);
        HeadstonesEntityDataSerializers.init();
        HeadstonesEntityTypes.init();
    }
}
