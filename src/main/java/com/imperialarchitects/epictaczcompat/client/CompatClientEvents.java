package com.imperialarchitects.epictaczcompat.client;

import com.imperialarchitects.epictaczcompat.EpicTaczCompat;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.player.TickPlayerEpicFightModeEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/// EpicFightのバトルモードがONになるとTacZの一人称銃モデルが描画されない。
/// また三人称ではTacZのHumanoidModel Mixinが効かずバニラ姿勢になる。
/// そこで銃を持っている間だけバトルモードを抑制する。
///
/// 抑制には EpicFight 公式の `TickPlayerEpicFightModeEvent` を使う。これを cancel すると
/// `PlayerPatch.preTick` 側で `toVanillaMode(false)` + `battleModeRestricted = true` が立ち、
/// cancel をやめた時点で EpicFight 自身が `toEpicFightMode(false)` で復帰させてくれる。
/// 直接 `toVanillaMode` を呼ぶ旧実装では復帰処理が無く、一度銃を持つと剣に持ち替えても
/// バトルモードUIが戻らなかった(0.5.1 までの不具合)。
///
/// 注意: EpicFightのモード切替は `autoPerspectiveSwithing` 設定下で内部的にカメラを
/// FIRST_PERSON / THIRD_PERSON_BACK へ強制する副作用を持つため、モードが実際に
/// 変化したtickに限り切替前のカメラタイプへ戻す。
public final class CompatClientEvents {

    /// モード変化を監視中の LocalPlayerPatch。監視待ちが無ければ null。
    private static LocalPlayerPatch pendingPatch;
    /// 監視開始時点のバトルモード状態。
    private static boolean epicFightModeBefore;
    /// 監視開始時点のカメラタイプ。
    private static CameraType cameraBefore;

    private CompatClientEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.register(CompatClientEvents.class);
        // EpicFight 21.x のイベントは NeoForge のバスではなく独自の EventHook 経由。
        EpicFightEventHooks.Player.TICK_EPICFIGHT_MODE.registerEvent(
                CompatClientEvents::onTickEpicFightMode, EpicTaczCompat.MODID);
    }

    /// EpicFightが「バトルモードを維持してよいか」を毎tick問い合わせてくるイベント。
    /// 銃保持中だけcancelして抑制する。
    private static void onTickEpicFightMode(TickPlayerEpicFightModeEvent event) {
        if (!(event.getPlayerPatch() instanceof LocalPlayerPatch patch)) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || patch.getOriginal() != player) return;

        pendingPatch = patch;
        epicFightModeBefore = patch.isEpicFightMode();
        cameraBefore = mc.options.getCameraType();

        if (isHoldingGun(player) && !isPlayingEmote(patch)) {
            event.cancel();
        }
    }

    /// EpicFightのエモートは EpicFight 自前の armature に対して再生されるので、
    /// バニラモードのままだと再生されても一切描画されない(エモートホイールは
    /// 開くのに何も起きないように見える)。エモート中だけ抑制を解除する。
    private static boolean isPlayingEmote(LocalPlayerPatch patch) {
        Class<?> emoteAnimation = EmoteAnimationClass.VALUE;
        if (emoteAnimation == null) return false;
        return emoteAnimation.isInstance(patch.getClientAnimator().getPlayerFor(null).getAnimation().get());
    }

    /// `EmoteAnimation` は EpicFight のエモート実装が入ったビルドにしか存在しない。
    /// 本MODは EpicFight `[21.0.0,)` を許容しているので、直接参照すると古いビルドで
    /// NoClassDefFoundError になる。解決できなければエモート無しとして扱う。
    private static final class EmoteAnimationClass {
        static final Class<?> VALUE = resolve();

        private static Class<?> resolve() {
            try {
                return Class.forName("yesman.epicfight.api.animation.types.EmoteAnimation");
            } catch (ClassNotFoundException e) {
                EpicTaczCompat.LOGGER.info("[{}] Epic Fight has no emote support on this build - emote handling disabled",
                        EpicTaczCompat.MODID);
                return null;
            }
        }
    }

    /// モードが切り替わったtickだけカメラを戻す。無条件に戻すと通常のF5操作まで
    /// 打ち消してしまうため、切替が起きた場合に限定している。
    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        LocalPlayerPatch patch = pendingPatch;
        if (patch == null) return;
        CameraType saved = cameraBefore;
        boolean before = epicFightModeBefore;
        pendingPatch = null;
        cameraBefore = null;

        if (saved == null || patch.isEpicFightMode() == before) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() != saved) {
            mc.options.setCameraType(saved);
        }
    }

    /// TacZ の銃 (`IGun`) に加えて、TacZ のアニメーションシステムに乗る外部アドオン装備
    /// (`IAnimationItem`: LesRaisins Tactical Equipements の近接武器・投擲物など) も対象にする。
    /// これらもバトルモード中は EpicFight の攻撃アニメに一人称ビューモデルを潰されるため。
    private static boolean isHoldingGun(LocalPlayer player) {
        return isTaczAnimatedItem(player.getMainHandItem())
                || isTaczAnimatedItem(player.getOffhandItem());
    }

    private static boolean isTaczAnimatedItem(ItemStack stack) {
        return stack.getItem() instanceof IGun || stack.getItem() instanceof IAnimationItem;
    }
}
