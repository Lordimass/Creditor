plugins {
    idea
    java
    id("com.azuredoom.hytale-tools") version "1.+"
    id("com.azuredoom.hytalepublisher") version "1.1.12"
}


tasks.withType<Javadoc>().configureEach {
    (options as org.gradle.external.javadoc.StandardJavadocDocletOptions).addStringOption("Xdoclint:-missing", "-quiet")
}

group = project.property("group").toString()

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(property("java_version").toString().toInt()))
}

val lombokVersion = "1.18.40"
dependencies {
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("org.jspecify:jspecify:1.0.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

hytaleTools {
    javaVersion = property("java_version").toString().toInt()
    hytaleVersion = property("hytale_version").toString()
    manifestServerVersion = property("manifestServerVersion").toString()
    manifestGroup = property("manifest_group").toString()
    modId = property("mod_id").toString()
    modDescription = property("mod_description").toString()
    modUrl = property("mod_url").toString()
    mainClass = property("main_class").toString()
    modCredits = property("mod_author").toString()
    manifestDependencies = property("manifest_dependencies").toString()
    manifestOptionalDependencies = property("manifest_opt_dependencies").toString()
    curseforgeId = property("curseforgeID").toString()
    disabledByDefault = property("disabled_by_default").toString().toBoolean()
    includesPack = property("includes_pack").toString().toBoolean()
    patchline = property("patchline").toString()
}

val generateBuildInfo by tasks.registering(Copy::class) {
    description = "Generate a class with a version constant to allow accessing it at runtime (manifest.json is not available when shadowed)"
    from("src/main/templates")
    into(layout.buildDirectory.dir("generated/sources/buildInfo"))

    expand(
        mapOf(
            "version" to project.version
        )
    )
}

sourceSets.main {
    java.srcDir(layout.buildDirectory.dir("generated/sources/buildInfo"))
}

tasks.compileJava {
    dependsOn(generateBuildInfo)
}

repositories {
    mavenCentral()
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
        generatedSourceDirs.add(
            layout.buildDirectory.dir("generated/sources/buildInfo").get().asFile
        )
    }
}

hytalePublisher {
    curseforge {
        enabled = true
        projectId = property("curseforge_project_id").toString()
    }

    modifold {
        enabled = true
        projectId = property("modifold_project_id").toString()
    }
}
