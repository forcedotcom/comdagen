package com.salesforce.comdagen.generator

import com.salesforce.comdagen.config.LibraryConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Library
import java.io.File

data class LibraryGenerator(
    override val configuration: LibraryConfiguration,

    val configDir: File
) :
        Generator<LibraryConfiguration, Library> {
    override val objects: Sequence<Library>
        get() = (1..configuration.elementCount).map {
            Library(it, configuration.initialSeed, configuration, configDir)
        }.asSequence()

    override val creatorFunc = { idx: Int, seed: Long -> Library(idx, seed, configuration, configDir) }


    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}