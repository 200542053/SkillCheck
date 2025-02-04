package io.github.cottonmc.skillcheck.events;

import io.github.cottonmc.skillcheck.SkillCheck;
import io.github.cottonmc.skillcheck.api.classes.ClassManager;
import io.github.cottonmc.skillcheck.api.dice.Dice;
import io.github.cottonmc.skillcheck.api.dice.RollResult;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

public class PlayerAttackEvent {

	public static AttackEntityCallback onPlayerAttack = (player, world, hand, entity, hitResult) -> {
		if (player.getStackInHand(hand).isEmpty()) {
			if (ClassManager.hasClass(player, SkillCheck.BRAWLER)) {
				for (ItemStack stack : entity.getArmorItems()) {
					if (stack.getItem() instanceof ArmorItem) return ActionResult.PASS;
				}
				if (entity instanceof LivingEntity) {
					LivingEntity mob = (LivingEntity) entity;
					if (ClassManager.hasLevel(player, SkillCheck.BRAWLER, 2) && !hasWeakness(mob)) {
						RollResult roll = Dice.roll("1d20+"+ClassManager.getLevel(player, SkillCheck.BRAWLER));
						if (SkillCheck.config.showDiceRolls) {
							if (roll.isCritFail()) player.addChatMessage(new TranslatableText("msg.skillcheck.roll.fail", roll.getFormattedNaturals()), false);
							else player.addChatMessage(new TranslatableText("msg.skillcheck.roll.result", roll.getTotal(), roll.getFormattedNaturals()), false);
						}
						if (roll.isCritFail()) {
							player.addPotionEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200));
							player.playSound(SoundEvents.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
						} else if (roll.getTotal() >= SkillCheck.config.weakenEnemyRoll) {
							mob.addPotionEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200));
							player.playSound(SoundEvents.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
						}
					}
				}
				entity.damage(DamageSource.player(player), ClassManager.getLevel(player, SkillCheck.BRAWLER)*2);
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.PASS;
	};

	private static boolean hasWeakness(LivingEntity entity) {
		if (entity.getStatusEffects().isEmpty()) return false;
		for (StatusEffectInstance status : entity.getStatusEffects()) {
			if (status.getEffectType() == StatusEffects.WEAKNESS) return true;
		}
		return false;
	}
}
