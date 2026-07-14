package me.pajic.mapstitch.gamerule;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;

public class ModGameRules {
	public static GameRule<Boolean> REQUIRE_COMPASS_FOR_POS = booleanGameRule(true);
	public static GameRule<Integer> MAX_ATLAS_ITEMS = integerGameRule(64, 16384, 16384);

	private static GameRule<Integer> integerGameRule(int min, int max, int defaultValue) {
		return new GameRule<>(
				GameRuleCategory.MISC,
				GameRuleType.INT,
				IntegerArgumentType.integer(min, max),
				GameRuleTypeVisitor::visitInteger,
				Codec.INT,
				i -> i,
				defaultValue,
				FeatureFlagSet.of()
		);
	}

	private static GameRule<Boolean> booleanGameRule(boolean defaultValue) {
		return new GameRule<>(
				GameRuleCategory.MISC,
				GameRuleType.BOOL,
				BoolArgumentType.bool(),
				GameRuleTypeVisitor::visitBoolean,
				Codec.BOOL,
				bl -> bl ? 1 : 0,
				defaultValue,
				FeatureFlagSet.of()
		);
	}

	public static void init() {}
}
