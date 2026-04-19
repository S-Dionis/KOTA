package kota.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

class GooglePlayPublishingPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        val extension = extensions.create(
            "googlePlayPublishing",
            GooglePlayPublishingExtension::class.java,
            objects,
            layout
        )

        tasks.register("publishLocalRelease", PublishLocalReleaseTask::class.java) {
            group = "publishing"

            artifactType.set(extension.artifactType)
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

abstract class GooglePlayPublishingExtension(
    objects: org.gradle.api.model.ObjectFactory,
    layout: org.gradle.api.file.ProjectLayout
) {
    val artifactType: Property<PublishArtifactType> =
        objects.property(PublishArtifactType::class.java).convention(PublishArtifactType.BUNDLE)

    val outputDirectory: DirectoryProperty =
        objects.directoryProperty().convention(layout.buildDirectory.dir("local-publish"))
}

enum class PublishArtifactType {
    APK,
    BUNDLE
}

abstract class PublishLocalReleaseTask : DefaultTask() {

    @get:Input
    abstract val artifactType: Property<PublishArtifactType>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputArtifact: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun publish() {
        val sourceFile = inputArtifact.get().asFile
        require(sourceFile.exists()) {
            "Artifact not found: ${sourceFile.absolutePath}"
        }

        val targetDir = outputDirectory.get().asFile
        targetDir.mkdirs()

        val targetFile = targetDir.resolve(sourceFile.name)
        sourceFile.copyTo(targetFile, overwrite = true)

        println("Published ${artifactType.get()} to ${targetFile.absolutePath}")
    }
}
