buildscript {
    dependencies.constraints {
        "classpath"("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0-titan-217!!")
    }
}

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.kotlinMultiplatform) apply false
}