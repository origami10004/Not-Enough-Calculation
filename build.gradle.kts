import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    idea
    java
    alias { libs.plugins.idea.ext }
    alias { libs.plugins.retrofuturagradle }
    id("com.gradleup.shadow") version "8.3.0"
}

val modId: String by project
val modName: String by project
val modLicense: String by project
val modVersion: String = System.getenv("VERSION") ?: "0.2.1-indev"
val modGroupId: String by project
val modAuthors: String by project
val modDescription: String by project

val modIssueTracker: String by project

version = modVersion
group = modGroupId

java.toolchain.languageVersion = JavaLanguageVersion.of(8)

base.archivesName.set("$modName-${libs.versions.minecraft.get()}")

println(
    "Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${
        System.getProperty(
            "os.arch"
        )
    }"
)

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val modReplacementProperties = mapOf(
        "modId" to modId,
        "modName" to modName,
        "modVersion" to modVersion,
        "mcVersion" to "1.12.2",
        "modAuthors" to "${modAuthors.split(", ").map { "\"$it\"" }}",
        "modDescription" to modDescription
    )
    inputs.properties(modReplacementProperties)
    expand(modReplacementProperties)
    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}

sourceSets.main {
    resources {
        srcDir(generateModMetadata)
    }
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven {
                name = "BlameJared Maven"
                url = uri("https://maven.blamejared.com/")
            }
        }
        filter {
            includeGroup("mezz.jei")
        }
    }
    exclusiveContent {
        forRepository {
            maven {
                name = "Curse maven"
                url = uri("https://www.cursemaven.com")
            }
        }
        filter {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    val deobf = fun(depSpec: Provider<MinimalExternalModuleDependency>): Provider<MinimalExternalModuleDependency> =
        depSpec.apply {
            rfg.deobf(get().let {
                "${it.group}:${it.name}:${it.version}"
            })
        }

    compileOnly(libs.jetbrains.annotations)

    compileOnly(libs.jei) { artifact { classifier = "api" } }
    runtimeOnly(deobf(libs.jei))
    runtimeOnly("curse.maven:nuclearcraft-mod-226254:5340979")

    compileOnly("curse.maven:mekanism-ce-399904:7660563")
    compileOnly("curse.maven:thaumcraft-223628:2629023")
    // runtimeOnly(deobf("curse.maven:mekanism-ce-399904:7660563"))
    // runtimeOnly(deobf("curse.maven:thaumcraft-223628:2629023"))

    shadow("org.hipparchus:hipparchus-optim:3.1")
    implementation("org.hipparchus:hipparchus-optim:3.1")
}

mcpTasks {
    taskRunClient.get().workingDir = file("run/client")
    taskRunServer.get().workingDir = file("run/server")
}

minecraft {
    mcVersion.set("1.12.2")

    // Username for client run configurations
    username.set("Dev")

    // Enable assertions in the mod's package when running the client or server
    extraRunJvmArguments.add("-ea:${project.group}")

    // Exclude some Maven dependency groups from being automatically included in the reobfuscated runs
    groupsToExcludeFromAutoReobfMapping.addAll("com.diffplug", "com.diffplug.durian", "net.industrial-craft")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true

        // Add directory exclusions
        val excludes = arrayOf(
            "run", ".gradle", "build", ".idea"
        ).map { file(it) }
        excludeDirs.addAll(excludes)
    }

    project {
        settings {
            runConfigurations {
                add(Gradle("1. Run Client").apply {
                    taskNames = listOf("runClient")
                })
                add(Gradle("2. Run Server").apply {
                    taskNames = listOf("runServer")
                })
            }
        }
    }
}

tasks {
    processIdeaSettings {
        dependsOn(generateModMetadata)
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        archiveClassifier.set("shaded") 
        mergeServiceFiles()

        relocate("org.hipparchus", "com.origami10004.necalc.shaded.hipparchus")
    }

    reobfJar {
        dependsOn(shadowJar)
        
        inputJar.set(shadowJar.flatMap { it.archiveFile }) 
    }

    build {
        dependsOn(reobfJar)
    }
}