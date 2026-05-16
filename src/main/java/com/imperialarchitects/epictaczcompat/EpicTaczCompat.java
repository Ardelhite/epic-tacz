package com.imperialarchitects.epictaczcompat;

import com.imperialarchitects.epictaczcompat.client.CompatClientEvents;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(EpicTaczCompat.MODID)
public final class EpicTaczCompat {
    public static final String MODID = "epictaczcompat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EpicTaczCompat(IEventBus modBus, ModContainer container) {
        LOGGER.info("[{}] loading - bridges TacZ first-person rendering with EpicFight battle mode", MODID);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CompatClientEvents.register();
        }
    }
}
