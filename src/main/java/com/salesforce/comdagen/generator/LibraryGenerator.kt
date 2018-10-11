package com.salesforce.comdagen.generator

import com.salesforce.comdagen.config.LibraryConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Library
import java.io.File

data class LibraryGenerator(override val configuration: LibraryConfiguration, val configDir: File) :
        Generator<LibraryConfiguration, Library> {

    //TODO: Implement generation of libraries with index
    override val objects: Sequence<Library>
        get() {
            // Test implementation
            var libraries: Sequence<Library> = emptySequence()
            libraries = libraries.plus(sequenceOf(Library(configuration.initialSeed, configuration, configDir)))
            return libraries
        }

    //TODO: Implement creatorFunc


    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}