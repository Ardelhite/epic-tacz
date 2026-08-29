package com.imperialarchitects.epictaczcompat.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.api.item.IGun;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.forgeevent.BattleModeSustainableEvent;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.SkillSlots;

/// EpicFightのバトルモードがONになるとTacZの一人称銃モデルが描画されない。
/// また三人称ではTacZのHumanoidModel Mixinが効かずバニラ姿勢になる。
/// そこで銃を持っている間だけバトルモードを抑制する。
///
/// 抑制には EpicFight 公式の `BattleModeSustainableEvent` を使う。これを cancel すると
/// `PlayerPatch.tick` 側で `toVanillaMode(false)` + `battleModeRestricted = true` が立ち、
/// cancel をやめた時点で EpicFight 自身が `toEpicFightMode(false)` で復帰させてくれる。
/// 直接 `toVanillaMode` を呼ぶ旧実装では復帰処理が無く、一度銃を持つと剣に持ち替えても
/// バトルモードUIが戻らなかった(0.5.1 までの不具合)。
///
/// 注意: EpicFightのモード切替は `autoSwitchCamera` 設定下で内部的にカメラを
/// FIRST_PERSON / THIRD_PERSON_BACK へ強制する副作用を持つため、モードが実際に
/// 変化したtickに限り切替前のカメラタイプへ戻す。
///
/// また、抑制中(=バニラモード強制中)は EpicFight のドッジ入力処理が動かないため、
/// ドッジキーの押下を本MODが検知してスキル発動を代行する(詳細は
/// `maybeCastDodgeWhileSuppressed` を参照)。ドッジアニメ再生中は抑制を解除し、
/// EpicFight 描画でロールを見せてから自動でバニラモードへ戻す。
public final class CompatClientEvents {

    /// モード変化を監視中の LocalPlayerPatch。監視待ちが無ければ null。
    private static LocalPlayerPatch pendingPatch;
    /// 監視開始時点のバトルモード状態。
    private static boolean epicFightModeBefore;
    /// 監視開始時点のカメラタイプ。
    private static CameraType cameraBefore;

    private CompatClientEvents() {}

    public static void register() {
        MinecraftForge.EVENT_BUS.register(CompatClientEvents.class);
    }

    /// EpicFightが「バトルモードを維持してよいか」を毎tick問い合わせてくるイベント。
    /// 銃保持中だけcancelして抑制する。
    @SubscribeEvent
    public static void onBattleModeSustainable(BattleModeSustainableEvent event) {
        if (!(event.getPlayerPatch() instanceof LocalPlayerPatch patch)) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || patch.getOriginal() != player) return;

        pendingPatch = patch;
        epicFightModeBefore = patch.isEpicFightMode();
        cameraBefore = mc.options.getCameraType();

        if (isHoldingGun(player) && !isPlayingDodge(patch)) {
            event.setCanceled(true);
        }
    }

    /// ドッジ(ロール/ステップ)は EpicFight 自前の armature に対して再生されるので、
    /// 抑制したままだと発動しても見た目が一切動かない。ドッジアニメ再生中だけ
    /// 抑制を解除して EpicFight 描画に切り替える。アニメが終われば銃保持中の
    /// cancel が再開し、自動的に銃の一人称描画へ戻る。
    private static boolean isPlayingDodge(LocalPlayerPatch patch) {
        return patch.getClientAnimator().getPlayerFor(null).getAnimation().get() instanceof DodgeAnimation;
    }

    /// 前tickのドッジキー押下状態。押下エッジ(押した瞬間)の検出に使う。
    private static boolean dodgeKeyWasDown;

    /// 銃保持中はバニラモード強制のため、EpicFight 自身のドッジ入力処理が
    /// `isEpicFightMode()` ゲートで一切動かない。そこでドッジキー押下を検知して
    /// `SkillContainer.sendCastRequest` を代行する。スキルの実行判定
    /// (接地・スタミナ・クールダウン等) とサーバ側の `requestCasting` は
    /// どちらもモード非依存なので、バニラモードのまま発動できる。
    /// EpicFight モード中は EpicFight 自身が処理するので触らない。
    private static void maybeCastDodgeWhileSuppressed() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        boolean down = isDodgeKeyPhysicallyDown(mc);
        boolean pressed = down && !dodgeKeyWasDown;
        dodgeKeyWasDown = down;
        if (!pressed || mc.screen != null || !isHoldingGun(player)) return;

        // ドッジキーがスニークと同じ物理キーにバインドされている構成
        // (EpicFight が公式にサポートするスニーク兼用設定)では、銃を持って
        // しゃがむだけでドッジが暴発してしまうため代行発動しない。
        if (EpicFightKeyMappings.DODGE.getKey().equals(mc.options.keyShift.getKey())) return;

        LocalPlayerPatch patch = ClientEngine.getInstance().getPlayerPatch();
        if (patch == null || patch.getOriginal() != player || patch.isEpicFightMode()) return;

        SkillContainer dodge = patch.getSkill(SkillSlots.DODGE);
        if (dodge.isEmpty()) return;
        dodge.sendCastRequest(patch, ClientEngine.getInstance().controlEngine);
    }

    /// EpicFight の `CombatKeyMapping` は `isActiveAndMatches` をオーバーライドし、
    /// バニラモード中はキー入力を一切マッピングへ伝えない(`isDown` /
    /// `consumeClick` とも常に false)。抑制中こそ押下を拾いたいので、
    /// 現在のバインドの物理キー状態を GLFW から直接読む。
    private static boolean isDodgeKeyPhysicallyDown(Minecraft mc) {
        InputConstants.Key key = EpicFightKeyMappings.DODGE.getKey();
        if (key == InputConstants.UNKNOWN) return false;
        long window = mc.getWindow().getWindow();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
        }
        return InputConstants.isKeyDown(window, key.getValue());
    }

    /// モードが切り替わったtickだけカメラを戻す。無条件に戻すと通常のF5操作まで
    /// 打ち消してしまうため、切替が起きた場合に限定している。
    @SubscribeEvent
    public static void onClientTickPost(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        maybeCastDodgeWhileSuppressed();

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
