plugins {
	id("mod-platform")
	id("net.fabricmc.fabric-loom")
}

platform {
	loader = "fabric"
	dependencies {
		required("minecraft") {
			versionRange = ">=${prop("deps.minecraft")}"
		}
		required("fabric-api") {
			slug("fabric-api")
			versionRange = ">=${prop("deps.fabric-api")}"
		}
		required("fabricloader") {
			versionRange = ">=${libs.fabric.loader.get().version}"
		}
	}
}

loom {
	runs.named("client") {
		client()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "client"
		programArgs("--username=Dev")
		configName = "Fabric Client"
	}
	runs.named("server") {
		server()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "server"
		configName = "Fabric Server"
	}
}

repositories {
	mavenCentral()
	strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
	strictMaven("https://maven.caffeinemc.net/releases") { name = "CaffeineMC" }
	strictMaven("https://maven.nucleoid.xyz/releases") { name = "Nucleoid" }
}

dependencies {
	minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
	implementation(libs.fabric.loader)
	implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
	compileOnlyApi("net.caffeinemc:sodium-fabric-api:${prop("deps.sodium")}")
	runtimeOnly("net.caffeinemc:sodium-fabric:${prop("deps.sodium")}")
	compileOnlyApi("eu.pb4:trinkets:${prop("deps.trinkets")}")
	//runtimeOnly("eu.pb4:trinkets:${prop("deps.trinkets")}")
	compileOnly("io.github.swackyy:ohmega-fabric:${prop("deps.ohmega")}")
	/*runtimeOnly("io.github.swackyy:ohmega-fabric:${prop("deps.ohmega")}")
	runtimeOnly("maven.modrinth:forge-config-api-port:26.2.1-fabric")*/
}
