import java.util.Locale

plugins {
    id("dev.kikugie.stonecutter")
    kotlin("jvm") apply false
    id("com.google.devtools.ksp") apply false
    id("dev.kikugie.fletching-table.fabric") apply false
    id("me.modmuss50.mod-publish-plugin") apply false
}

stonecutter active "26.2-fabric"

stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)
    val versionFormatted = version.replace(".", "_")
    val loaderFormatted = loader.replaceFirstChar { it.uppercase(Locale.getDefault()) }
    val modId = properties.get<String>("mod.id")
    val modGroup = properties.get<String>("mod.group")

    properties {
        tags(version, loader)
    }

    constants {
        match(loader, "fabric", "neoforge")
    }

    swaps["mod_id"] = "\"${modId}\";"
    swaps["version_util_import"] = "import ${modGroup}.${modId}.platform.version.Util${versionFormatted};"
    swaps["version_util_inst"] = "new Util${versionFormatted}();"
    swaps["loader_util_import"] = "import ${modGroup}.${modId}.platform.${loader}.${loaderFormatted}LoaderUtil;"
    swaps["loader_util_inst"] = "new ${loaderFormatted}LoaderUtil();"
    constants["release"] = properties.get<String>("mod.id") != "template"
    dependencies["fapi"] = properties.getOrNull<String>("deps.fabric_api") ?: "0"

    replacements {
        string(current.parsed < "26.1" && loader == "fabric") {
            replace("classTweaker v2 official", "classTweaker v2 named")
        }
        string(current.parsed >= "1.21.11") {
            filters.exclude("**/*.ct")
            replace("ResourceLocation", "Identifier")
            replace("location()", "identifier()")
            replace("ValidatedIdentifier", "ValidatedIdentifier")
            replace("AllowableIdentifiers", "AllowableIdentifiers")
            replace("GuiGraphicsExtractor.class", "GuiGraphicsExtractor.class")
            replace("import net.minecraft.Util;", "import net.minecraft.util.Util;")
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("graphics.drawString", "graphics.text")
            replace("playS2C()", "clientboundPlay()")
            replace("playC2S()", "serverboundPlay()")
            replace("ItemGroupEvents.modifyEntriesEvent", "CreativeModeTabEvents.modifyOutputEvent")
            replace("import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;", "import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;")
        }
        string(current.parsed > "26.1.2") {
            filters.exclude("**/*.ct")
            replace("MC.options.hideGui", "MC.gui.hud.isHidden()")
            replace("MC.gui.getDebugOverlay().showDebugScreen()", "MC.gui.hud.getDebugOverlay().showDebugScreen()")
            replace("MC.gameRenderer.getGameRenderState()", "MC.gameRenderer.gameRenderState()")
            replace("MC.screen", "MC.gui.screen()")
            replace("client.setScreen", "client.setScreenAndShow")
        }
    }
}
