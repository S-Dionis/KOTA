package kota.buildlogic

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

abstract class GooglePlayPublishingExtension(
    objects: ObjectFactory,
    layout: ProjectLayout
) {
    val artifactType: Property<PublishArtifactType> =
        objects.property(PublishArtifactType::class.java).convention(PublishArtifactType.BUNDLE)

    val track: Property<String> =
        objects.property(String::class.java).convention("internal")

    val userFraction: Property<Double> =
        objects.property(Double::class.java).convention(1.0)

    val releaseNotes: Property<String> =
        objects.property(String::class.java).convention("")

    val outputDirectory: DirectoryProperty =
        objects.directoryProperty().convention(layout.buildDirectory.dir("local-publish"))
}
