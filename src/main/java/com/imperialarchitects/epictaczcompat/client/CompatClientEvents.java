package com.imperialarchitects.epictaczcompat.client;

import com.tacz.guns.api.item.IGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

/// EpicFightのバトルモードがONになるとTacZの一人称銃モデルが描画されない。
/// 銃をメインハンドに持っている間、毎tickバトルモードをvanilla(通常)に強制する。
public final class CompatClientEvents {

    private CompatClientEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(CompatClientEvents.class);
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (!isHoldingGun(player)) return;

        LocalPlayerPatch patch = EpicFightCapabilities.getCachedLocalPlayerPatch();
        if (patch == null) return;

        if (patch.isEpicFightMode()) {
            patch.toVanillaMode(true);
        }
    }

    private static boolean isHoldingGun(LocalPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof IGun) return true;
        ItemStack off = player.getOffhandItem();
        return off.getItem() instanceof IGun;
    }
}
