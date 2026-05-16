package com.imperialarchitects.epictaczcompat.client;

import com.tacz.guns.api.item.IGun;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

/// EpicFightのバトルモードがONになるとTacZの一人称銃モデルが描画されない。
/// また三人称ではTacZのHumanoidModel Mixinが効かずバニラ姿勢になる。
/// 銃を持っている間、毎tickバトルモードをvanillaに戻すことで両視点でTacZの挙動を保つ。
///
/// 注意: EpicFightの `toVanillaMode` は `autoPerspectiveSwithing` 設定下で
/// 内部的にカメラを FIRST_PERSON へ強制する副作用を持つため、呼び出し前後で
/// カメラタイプを save/restore する。これにより三人称中も視点が維持される。
public final class CompatClientEvents {

    private CompatClientEvents() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(CompatClientEvents.class);
    }

    @SubscribeEvent
    public static void onClientTickPost(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (!isHoldingGun(player)) return;

        LocalPlayerPatch patch = EpicFightCapabilities.getLocalPlayerPatch(player);
        if (patch == null) return;

        if (!patch.isEpicFightMode()) return;

        CameraType preCamera = mc.options.getCameraType();
        // dispatch=false: サーバへのモード変更パケットを毎tick送らない
        patch.toVanillaMode(false);
        if (mc.options.getCameraType() != preCamera) {
            mc.options.setCameraType(preCamera);
        }
    }

    private static boolean isHoldingGun(LocalPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof IGun) return true;
        ItemStack off = player.getOffhandItem();
        return off.getItem() instanceof IGun;
    }
}
