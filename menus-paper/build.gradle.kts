plugins {
    alias(libs.plugins.shadow)
}

dependencies {
    compileOnly(libs.paper.api)

    implementation(project(":menus-api"))
    implementation(libs.platform.api)
    implementation(libs.platform.common)
    implementation(libs.platform.config)
    implementation(libs.platform.paper)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.paper.api)
    testRuntimeOnly(libs.slf4j.simple)
    testRuntimeOnly(libs.junit.platform.launcher)
}

configurations.runtimeClasspath {
    // Paper provides these; a second copy inside the plugin jar shadows the server's own.
    exclude(group = "com.google.code.gson")
    exclude(group = "org.slf4j", module = "slf4j-api")
    exclude(group = "net.kyori")
}

tasks.shadowJar {
    archiveFileName = "landmc-menus.jar"

    val shaded = "pl.landmc.menus.paper.libs"
    listOf(
        "eu.okaeri",
        "dev.rollczi.litecommands",
        "com.eternalcode.multification",
        "org.yaml.snakeyaml",
    ).forEach { relocate(it, "$shaded.$it") }

    // menus-api is deliberately not relocated. The same classes are compiled into the
    // proxy-side plugins, and while nothing passes objects between the two - the wire is
    // bytes - a relocated package name here would make the two copies impossible to compare
    // when reading a stack trace from either side.

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/versions/**/module-info.class")
    exclude("org/jetbrains/annotations/**", "org/intellij/lang/**")

    mergeServiceFiles()
}

tasks.processResources {
    val properties = mapOf("version" to project.version)
    inputs.properties(properties)
    filesMatching("paper-plugin.yml") {
        expand(properties)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
