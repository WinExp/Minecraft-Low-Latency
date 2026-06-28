val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

plugins {
    java
    id("multiloader-common")
    id("me.modmuss50.mod-publish-plugin")
}

val curseProject = property("curse_project") as String
val modrinthProject = property("modrinth_project") as String
val minecraftVersion = libs.findLibrary("minecraft").get().get().version

val commonJava = configurations.create("commonJava") {
    isCanBeResolved = true
}
val commonResources = configurations.create("commonResources") {
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

tasks.compileJava {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.processResources {
    dependsOn(commonResources)
    from(commonResources)
}

tasks.javadoc {
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

    type.set(ALPHA)
    file(project(":${project.name}"))
    changelog = rootProject.file("CHANGELOG.md").readText()
    modLoaders.add(project.name)
    version = "${project.version}+${project.name}"
    displayName = "[${loaderName} ${minecraftVersion}] ${project.version}"

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = curseProject
        client = true

        minecraftVersionList("26.1, 26.1.1, 26.1.2")
        if (project.name == "fabric") {
            requires("fabric-api")
        }
        optional("sodium")
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = modrinthProject
        projectDescription = rootProject.file("README.md").readText()

        minecraftVersionList("26.1, 26.1.1, 26.1.2")
        if (project.name == "fabric") {
            requires("fabric-api")
        }
        optional("sodium")
    }
}
