package kota.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class PublishLocalReleaseTask : DefaultTask() {

    @get:Input
    abstract val artifactType: Property<PublishArtifactType>

    @get:Input
    abstract val track: Property<String>

    @get:Input
    abstract val userFraction: Property<Double>

    @get:Input
    abstract val releaseNotes: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputArtifact: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun publish() {
        val sourceFile = inputArtifact.get().asFile
        val targetDir = outputDirectory.get().asFile

        targetDir.mkdirs()

        val targetFile = targetDir.resolve(sourceFile.name)
        sourceFile.copyTo(targetFile, overwrite = true)

        val metadataFile = targetDir.resolve("publish-metadata.txt")
        metadataFile.writeText(
            buildString {
                appendLine("artifactType=${artifactType.get()}")
                appendLine("track=${track.get()}")
                appendLine("userFraction=${userFraction.get()}")
                appendLine("releaseNotes=${releaseNotes.get()}")
                appendLine("artifact=${targetFile.absolutePath}")
            }
        )

        println("Published ${artifactType.get()} to ${targetFile.absolutePath}")
    }
}