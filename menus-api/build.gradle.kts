// The wire format a menu's contents travel in.
//
// Plain Java and nothing else: no Bukkit, no Velocity, not even the platform. This module is
// compiled into the proxy-side plugins that own the data and into the backend plugin that draws
// it, so a type from either server platform here would make it unusable on the other side -
// which is the whole reason it is its own module.
dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}
