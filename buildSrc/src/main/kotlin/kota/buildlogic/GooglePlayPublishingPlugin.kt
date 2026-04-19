package kota.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register

class GooglePlayPublishingPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val extension = extensions.create(
            "googlePlayPublishing",
            GooglePlayPublishingExtension::class.java,
            objects,
            layout
        )

        tasks.register<Copy>("publishLocalReleaseApk") {
            group = "publishing"
            dependsOn("assembleRelease")

            from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
            into(extension.outputDirectory)
        }

        tasks.register<Copy>("publishLocalReleaseBundle") {
            group = "publishing"
            dependsOn("bundleRelease")

            from(layout.buildDirectory.file("outputs/bundle/release/app-release.aab"))
            into(extension.outputDirectory)
        }

        tasks.register("publishLocalRelease", PublishLocalReleaseTask::class.java) {
            group = "publishing"

            artifactType.set(extension.artifactType)
            track.set(extension.track)
            userFraction.set(extension.userFraction)
            releaseNotes.set(extension.releaseNotes)
            outputDirectory.set(extension.outputDirectory)

            val type = extension.artifactType.get()
            if (type == PublishArtifactType.APK) {
                dependsOn("assembleRelease")
                inputArtifact.set(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
            } else {
                dependsOn("bundleRelease")
                inputArtifact.set(layout.buildDirectory.file("outputs/bundle/release/app-release.aab"))
            }
        }
    }
}
