package me.pajic.mapstitch.config;

import me.pajic.mapstitch.MapStitch;
import me.pajic.mapstitch.minimap.MinimapBackground;
import me.pajic.mapstitch.minimap.MinimapDisplayCondition;
import me.pajic.mapstitch.minimap.MinimapPosition;
import me.pajic.mapstitch.worldmap.TextHighlightColor;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.option.Range;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
//? neoforge {
/*import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;

@ConfigEntryPointForge(MapStitch.MOD_ID)
*///?}
@SuppressWarnings("unused")
public class ModSodiumConfig implements ConfigEntryPoint {
	private static final Minecraft MC = Minecraft.getInstance();

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setName("MapStitch")
                .setColorTheme(builder.createColorTheme().setBaseThemeRGB(0xb388eb))
                .setNonTintedIcon(Identifier.parse("mapstitch:textures/config_icon.png"))
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("config.mapstitch.minimap"))
						.addOption(builder.createEnumOption(MapStitch.id("minimap_display_condition"), MinimapDisplayCondition.class)
								.setName(Component.translatable("config.mapstitch.minimap.display_condition"))
								.setTooltip(Component.translatable("config.mapstitch.minimap.display_condition.desc"))
								.setDefaultValue(MinimapDisplayCondition.HOTBAR)
								.setElementNameProvider(MinimapDisplayCondition::getName)
								.setBinding(e -> ModConfigHolder.options().minimapDisplayCondition = e, () -> ModConfigHolder.options().minimapDisplayCondition)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createEnumOption(MapStitch.id("minimap_position"), MinimapPosition.class)
								.setName(Component.translatable("config.mapstitch.minimap.position"))
								.setTooltip(Component.translatable("config.mapstitch.minimap.position.desc"))
								.setDefaultValue(MinimapPosition.TOP_RIGHT)
								.setElementNameProvider(MinimapPosition::getName)
								.setBinding(e -> ModConfigHolder.options().minimapPosition = e, () -> ModConfigHolder.options().minimapPosition)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createIntegerOption(MapStitch.id("minimap_size"))
								.setName(Component.translatable("config.mapstitch.minimap.size"))
								.setTooltip(Component.translatable("config.mapstitch.minimap.size.desc"))
								.setDefaultValue(19)
								.setValueFormatter(value -> Component.literal(String.format("%.2fx", 0.05 + (value * 0.05))))
								.setRange(0, 39, 1)
								.setBinding(i -> ModConfigHolder.options().minimapSize = i, () -> ModConfigHolder.options().minimapSize)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createEnumOption(MapStitch.id("minimap_background"), MinimapBackground.class)
								.setName(Component.translatable("config.mapstitch.minimap.background"))
								.setTooltip(Component.translatable("config.mapstitch.minimap.background.desc"))
								.setDefaultValue(MinimapBackground.CLEAR)
								.setElementNameProvider(MinimapBackground::getName)
								.setBinding(e -> ModConfigHolder.options().minimapBackground = e, () -> ModConfigHolder.options().minimapBackground)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createIntegerOption(MapStitch.id("minimap_background_opacity"))
								.setName(Component.translatable("config.mapstitch.minimap.background_opacity"))
								.setTooltip(Component.translatable("config.mapstitch.minimap.background_opacity.desc"))
								.setDefaultValue(50)
								.setRange(0, 100, 1)
								.setValueFormatter(value -> Component.literal(value + "%"))
								.setBinding(i -> ModConfigHolder.options().minimapBackgroundOpacity = i, () -> ModConfigHolder.options().minimapBackgroundOpacity)
								.setEnabledProvider(state -> state.readEnumOption(MapStitch.id("minimap_background"), MinimapBackground.class) == MinimapBackground.CLEAR, MapStitch.id("minimap_background"))
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createBooleanOption(MapStitch.id("prevent_effect_overlap"))
								.setName(Component.translatable("config.mapstitch.minimap.prevent_effect_overlap"))
								.setTooltip(Component.translatable("config.mapstitch.minimap.prevent_effect_overlap.desc"))
								.setDefaultValue(true)
								.setBinding(bl -> ModConfigHolder.options().minimapPreventEffectOverlap = bl, () -> ModConfigHolder.options().minimapPreventEffectOverlap)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createIntegerOption(MapStitch.id("minimap_x_offset"))
								.setName(Component.translatable("config.mapstitch.minimap.x_offset"))
								.setTooltip(Component.translatable("config.mapstitch.minimap.x_offset.desc"))
								.setDefaultValue(0)
								.setValueFormatter(value -> Component.literal(String.valueOf(value)))
								.setRangeProvider(_ -> new Range(-MC.getWindow().getGuiScaledWidth(), MC.getWindow().getGuiScaledWidth(), 1), ConfigState.UPDATE_ON_REBUILD)
								.setBinding(i -> ModConfigHolder.options().minimapXOffset = i, () -> ModConfigHolder.options().minimapXOffset)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createIntegerOption(MapStitch.id("minimap_y_offset"))
								.setName(Component.translatable("config.mapstitch.minimap.y_offset"))
								.setTooltip(Component.translatable("config.mapstitch.minimap.y_offset.desc"))
								.setDefaultValue(0)
								.setValueFormatter(value -> Component.literal(String.valueOf(value)))
								.setRangeProvider(_ -> new Range(-MC.getWindow().getGuiScaledHeight(), MC.getWindow().getGuiScaledHeight(), 1), ConfigState.UPDATE_ON_REBUILD)
								.setBinding(i -> ModConfigHolder.options().minimapYOffset = i, () -> ModConfigHolder.options().minimapYOffset)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges())))
				.addPage(builder.createOptionPage()
						.setName(Component.translatable("config.mapstitch.worldmap"))
						.addOption(builder.createEnumOption(MapStitch.id("worldmap_text_highlight_color"), TextHighlightColor.class)
								.setName(Component.translatable("config.mapstitch.worldmap.text_highlight_color"))
								.setTooltip(Component.translatable("config.mapstitch.worldmap.text_highlight_color.desc"))
								.setDefaultValue(TextHighlightColor.YELLOW)
								.setElementNameProvider(TextHighlightColor::getName)
								.setBinding(e -> ModConfigHolder.options().worldMapTextHighlightColor = e, () -> ModConfigHolder.options().worldMapTextHighlightColor)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createIntegerOption(MapStitch.id("worldmap_text_background_opacity"))
								.setName(Component.translatable("config.mapstitch.worldmap.text_background_opacity"))
								.setTooltip(Component.translatable("config.mapstitch.worldmap.text_background_opacity.desc"))
								.setDefaultValue(50)
								.setRange(0, 100, 1)
								.setValueFormatter(value -> Component.literal(value + "%"))
								.setBinding(i -> ModConfigHolder.options().worldMapTextBackgroundOpacity = i, () -> ModConfigHolder.options().worldMapTextBackgroundOpacity)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createBooleanOption(MapStitch.id("show_buttons"))
								.setName(Component.translatable("config.mapstitch.show_buttons"))
								.setTooltip(Component.translatable("config.mapstitch.show_buttons.desc"))
								.setDefaultValue(true)
								.setBinding(bl -> ModConfigHolder.options().worldMapButtons = bl, () -> ModConfigHolder.options().worldMapButtons)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges()))
						.addOption(builder.createBooleanOption(MapStitch.id("show_help"))
								.setName(Component.translatable("config.mapstitch.show_help"))
								.setTooltip(Component.translatable("config.mapstitch.show_help.desc"))
								.setDefaultValue(true)
								.setBinding(bl -> ModConfigHolder.options().worldMapHelp = bl, () -> ModConfigHolder.options().worldMapHelp)
								.setStorageHandler(() -> ModConfigHolder.options().writeChanges())));
    }
}
