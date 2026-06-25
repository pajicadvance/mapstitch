package me.pajic.mapstitch.config;

//? fabric {

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.pajic.mapstitch.util.CompatFlags;

@SuppressWarnings("unused")
public class ModMenuIntegration implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return CompatFlags.YACL_LOADED ? ModYACLConfig::makeScreen : ModMenuApi.super.getModConfigScreenFactory();
	}
}
//?}
