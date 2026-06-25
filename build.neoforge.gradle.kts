plugins {
	id("mod-platform")
	id("net.neoforged.moddev")
}

platform {
	loader = "neoforge"
	dependencies {
		required("minecraft") {
			forgeVersionRange = "[${prop("deps.minecraft")},)"
		}
		required("neoforge") {
			forgeVersionRange = "[1,)"
		}
	}
}

neoForge {
	version = property("deps.neoforge") as String
	runs {
		register("client") {
			client()
			gameDirectory = file("run/")
			ideName = "NeoForge Client (${stonecutter.active?.version})"
			programArgument("--username=Dev")
		}
		register("server") {
			server()
			gameDirectory = file("run/")
			ideName = "NeoForge Server (${stonecutter.active?.version})"
		}
	}

	mods {
		register(property("mod.id") as String) {
			sourceSet(sourceSets["main"])
		}
	}
}

repositories {
	mavenCentral()
	strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
	strictMaven("https://maven.caffeinemc.net/releases") { name = "CaffeineMC" }
	strictMaven("https://maven.nucleoid.xyz/releases") { name = "Nucleoid" }
}

dependencies {
	compileOnlyApi("net.caffeinemc:sodium-neoforge-api:${prop("deps.sodium")}")
	runtimeOnly("net.caffeinemc:sodium-neoforge:${prop("deps.sodium")}")
	compileOnlyApi("eu.pb4:trinkets:${prop("deps.trinkets")}")
	//runtimeOnly("eu.pb4:trinkets:${prop("deps.trinkets")}")
	compileOnly("io.github.swackyy:ohmega-neoforge:${prop("deps.ohmega")}")
	/*runtimeOnly("io.github.swackyy:ohmega-neoforge:${prop("deps.ohmega")}")
	runtimeOnly("maven.modrinth:forge-config-api-port:26.1.3-neoforge")*/
}

tasks.named("createMinecraftArtifacts") {
	dependsOn(tasks.named("stonecutterGenerate"))
}
