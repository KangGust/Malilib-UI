plugins {
	id("net.fabricmc.fabric-loom")
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

base {
	archivesName.set("malilib-ui")
}

repositories {
	flatDir { dirs("${projectDir}/libs") }
}

dependencies {
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

	compileOnly(files(
		"${projectDir}/libs/malilib-fabric-26.3-0.30.2.jar",
		"${projectDir}/libs/litematica-fabric-26.3-0.29.1.jar",
		"${projectDir}/libs/minihud-fabric-26.3-0.41.2.jar",
		"${projectDir}/libs/tweakeroo-fabric-26.3-0.30.1.jar",
		"${projectDir}/libs/modmenu-21.0.0.jar",
		"${projectDir}/libs/Searchables-fabric-26.3-1.0.3.jar",
	))
}

tasks.processResources {
	val version = version
	inputs.property("version", version)
	filesMatching("fabric.mod.json") { expand("version" to version) }
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
	options.encoding = "UTF-8"
}

java {
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.matching { it.name.startsWith("run") }.configureEach { enabled = false }

tasks.jar {
	from("LICENSE") { rename { "${it}_malilib-ui" } }
}
