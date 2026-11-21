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

	// libs/ 下的 jar 由 scripts/fetch_deps.py 或本地手工提供；不存在的文件会被忽略
	val libJars = file("libs").listFiles { f -> f.name.endsWith(".jar") } ?: emptyArray()
	compileOnly(files(libJars))
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
