plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        register("googlePlayPublishing") {
            id = "kota.google-play-publishing"
            implementationClass = "kota.buildlogic.GooglePlayPublishingPlugin"
        }
    }
}
