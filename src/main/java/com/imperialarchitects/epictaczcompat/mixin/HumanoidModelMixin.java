package com.imperialarchitects.epictaczcompat.mixin;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.third.InnerThirdPersonManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// EpicFight が同梱されているとき、TacZ の三人称銃ポーズが
/// `HumanoidModel.setupAnim` の TAIL inject 競合で消える問題を回避する。
/// TacZ 自身の TAIL の後に走る priority=1500 で、
/// `InnerThirdPersonManager.setRotationAnglesHead` を再実行する。
@Mixin(value = HumanoidModel.class, priority = 1500)
public abstract class HumanoidModelMixin {

    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightArm;

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At("TAIL")
    )
    private void epictaczcompat$reapplyGunPose(LivingEntity entity, float limbSwing, float limbSwingAmount,
                                                float ageInTicks, float netHeadYaw, float headPitch,
                                                CallbackInfo ci) {
        if (ageInTicks == 0f) return;
        // 対象は三人称のプレイヤーモデルのみ。EpicFight の WearableItemLayer は
        // 防具モデル(素の HumanoidModel)に対しても setupAnim を呼ぶため、絞らないと
        // 防具スロットの数だけ PlayerAnimator のアニメ再生が毎フレーム走ってしまう。
        // Mob(ゾンビ等)のモデルも同じ理由で除外する。
        if (!((Object) this instanceof PlayerModel<?>)) return;
        if (!(entity instanceof Player)) return;
        if (!(entity.getMainHandItem().getItem() instanceof IGun)) return;

        // 最後の引数は TacZ 本家の TAIL inject と同じく limbSwingAmount。
        // ageInTicks を渡すと playLowerAnimation の歩行判定 (> 0.05) が常に真になり、
        // 静止中でも歩行アニメが再生されてしまう。
        InnerThirdPersonManager.setRotationAnglesHead(entity, this.rightArm, this.leftArm, this.body, this.head, limbSwingAmount);
    }
}
