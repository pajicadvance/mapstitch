package me.pajic.mapstitch.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import me.pajic.mapstitch.minimap.MinimapBackground;
import me.pajic.mapstitch.minimap.MinimapDisplayCondition;
import me.pajic.mapstitch.minimap.MinimapPosition;
import me.pajic.mapstitch.worldmap.TextHighlightColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModYACLConfig {
	private static final Minecraft MC = Minecraft.getInstance();

	public static Screen makeScreen(Screen parentScreen) {
		return YetAnotherConfigLib.createBuilder()
				.title(Component.translatable("config.mapstitch.title"))
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.mapstitch.minimap"))
						.option(Option.<MinimapDisplayCondition>createBuilder()
								.name(Component.translatable("config.mapstitch.minimap.display_condition"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.minimap.display_condition.desc")))
								.binding(ModConfigHolder.options().minimapDisplayCondition,
										() -> ModConfigHolder.options().minimapDisplayCondition,
										newValue -> ModConfigHolder.options().minimapDisplayCondition = newValue)
								.controller(opt -> EnumControllerBuilder.create(opt)
										.enumClass(MinimapDisplayCondition.class)
										.formatValue(MinimapDisplayCondition::getName))
								.build())
						.option(Option.<MinimapPosition>createBuilder()
								.name(Component.translatable("config.mapstitch.minimap.position"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.minimap.position.desc")))
								.binding(ModConfigHolder.options().minimapPosition,
										() -> ModConfigHolder.options().minimapPosition,
										newValue -> ModConfigHolder.options().minimapPosition = newValue)
								.controller(opt -> EnumControllerBuilder.create(opt)
										.enumClass(MinimapPosition.class)
										.formatValue(MinimapPosition::getName))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("config.mapstitch.minimap.size"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.minimap.size.desc")))
								.binding(ModConfigHolder.options().minimapSize,
										() -> ModConfigHolder.options().minimapSize,
										newValue -> ModConfigHolder.options().minimapSize = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										.range(0, 39).step(1)
										.formatValue(i -> Component.literal(String.format("%.2fx", 0.05 + (i * 0.05)))))
								.build())
						.option(Option.<MinimapBackground>createBuilder()
								.name(Component.translatable("config.mapstitch.minimap.background"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.minimap.background.desc")))
								.binding(ModConfigHolder.options().minimapBackground,
										() -> ModConfigHolder.options().minimapBackground,
										newValue -> ModConfigHolder.options().minimapBackground = newValue)
								.controller(opt -> EnumControllerBuilder.create(opt)
										.enumClass(MinimapBackground.class)
										.formatValue(MinimapBackground::getName))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("config.mapstitch.minimap.background_opacity"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.minimap.background_opacity.desc")))
								.binding(ModConfigHolder.options().minimapBackgroundOpacity,
										() -> ModConfigHolder.options().minimapBackgroundOpacity,
										newValue -> ModConfigHolder.options().minimapBackgroundOpacity = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										.range(0, 100).step(1)
										.formatValue(i -> Component.literal(i + "%")))
								.available(ModConfigHolder.options().minimapBackground == MinimapBackground.CLEAR)
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("config.mapstitch.minimap.x_offset"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.minimap.x_offset.desc")))
								.binding(ModConfigHolder.options().minimapXOffset,
										() -> ModConfigHolder.options().minimapXOffset,
										newValue -> ModConfigHolder.options().minimapXOffset = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										.range(-MC.getWindow().getGuiScaledWidth(), MC.getWindow().getGuiScaledWidth()).step(1))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("config.mapstitch.minimap.y_offset"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.minimap.y_offset.desc")))
								.binding(ModConfigHolder.options().minimapYOffset,
										() -> ModConfigHolder.options().minimapYOffset,
										newValue -> ModConfigHolder.options().minimapYOffset = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										.range(-MC.getWindow().getGuiScaledHeight(), MC.getWindow().getGuiScaledHeight()).step(1))
								.build())
						.build())
				.category(ConfigCategory.createBuilder()
						.name(Component.translatable("config.mapstitch.worldmap"))
						.option(Option.<TextHighlightColor>createBuilder()
								.name(Component.translatable("config.mapstitch.worldmap.text_highlight_color"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.worldmap.text_highlight_color.desc")))
								.binding(ModConfigHolder.options().worldMapTextHighlightColor,
										() -> ModConfigHolder.options().worldMapTextHighlightColor,
										newValue -> ModConfigHolder.options().worldMapTextHighlightColor = newValue)
								.controller(opt -> EnumControllerBuilder.create(opt)
										.enumClass(TextHighlightColor.class)
										.formatValue(TextHighlightColor::getName))
								.build())
						.option(Option.<Integer>createBuilder()
								.name(Component.translatable("config.mapstitch.worldmap.text_background_opacity"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.worldmap")))
								.binding(ModConfigHolder.options().worldMapTextBackgroundOpacity,
										() -> ModConfigHolder.options().worldMapTextBackgroundOpacity,
										newValue -> ModConfigHolder.options().worldMapTextBackgroundOpacity = newValue)
								.controller(opt -> IntegerSliderControllerBuilder.create(opt)
										.range(0, 100).step(1)
										.formatValue(i -> Component.literal(i + "%")))
								.build())
						.option(Option.<Boolean>createBuilder()
								.name(Component.translatable("config.mapstitch.show_help"))
								.description(OptionDescription.of(Component.translatable("config.mapstitch.show_help.desc")))
								.binding(ModConfigHolder.options().worldMapHelp,
										() -> ModConfigHolder.options().worldMapHelp,
										newValue -> ModConfigHolder.options().worldMapHelp = newValue)
								.controller(TickBoxControllerBuilder::create)
								.build())
						.build())
				.save(() -> ModConfigHolder.options().writeChanges())
				.build().generateScreen(parentScreen);
	}
}
