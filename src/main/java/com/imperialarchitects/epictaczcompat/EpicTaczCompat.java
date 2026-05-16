package com.imperialarchitects.epictaczcompat;

import com.imperialarchitects.epictaczcompat.client.CompatClientEvents;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(EpicTaczCompat.MODID)
public final class EpicTaczCompat {
    public static final String MODID = "epictaczcompat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EpicTaczCompat() {
        LOGGER.info("[{}] loading - bridges TacZ first-person rendering with EpicFight battle mode", MODID);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            CompatClientEvents.register();
        }
    }
}
