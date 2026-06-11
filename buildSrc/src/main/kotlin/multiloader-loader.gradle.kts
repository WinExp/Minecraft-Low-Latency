import org.gradle.api.artifacts.VersionCatalogsExtension

val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

plugins {
    java
    id("me.modmuss50.mod-publish-plugin")
}

pluginManager.apply("multiloader-common")

val curse_project: String by project
val modrinth_project: String by project
val minecraft_version = libs.findLibrary("minecraft").get().get().version

val commonJava by configurations.creating {
    isCanBeResolved = true
}
val commonResources by configurations.creating {
    isCanBeResolved = true
}

dependencies {
    compileOnly(project(":common")) {
        val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
        attributes {
            attribute(loaderAttribute, "common")
        }
    }
    commonJava(project(path = ":common", configuration = "commonJava"))
    commonResources(project(path = ":common", configuration = "commonResources"))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(commonResources)
    from(commonResources)
}

tasks.named<Javadoc>("javadoc") {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(commonJava)
    from(commonJava)
    dependsOn(commonResources)
    from(commonResources)
}

publishMods {
    val loaderName = when (project.name) {
        "fabric" -> "Fabric"
        "neoforge" -> "NeoForge"
        else -> ""
    }

    type.set(STABLE)
    file(project(":${project.name}"))
    changelog = rootProject.file("CHANGELOG.md").readText()
    modLoaders.add(project.name)
    version = "${project.version}+${project.name}"
    displayName = "[${loaderName} ${minecraft_version}] ${project.version}"

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = curse_project
        client = true

        minecraftVersionList("26.1, 26.1.1, 26.1.2")
        if (project.name == "fabric") {
            requires("fabric-api")
        }
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = modrinth_project
        projectDescription = rootProject.file("README.md").readText()

        minecraftVersionList("26.1, 26.1.1, 26.1.2")
        if (project.name == "fabric") {
            requires("fabric-api")
        }
    }
}
