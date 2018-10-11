package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.ExtendableObjectConfig
import com.salesforce.comdagen.RenderConfig

/**
 * Congfiguration file for libraries
 */
@JsonRootName("libraryConfig")
@JsonIgnoreProperties(ignoreUnknown = true)
data class LibraryConfiguration(
        override val initialSeed: Long = 1234,
        val libraryId: String?,
        //TODO: Adjust template and implement this
        val folders: List<FolderConfiguration> = listOf(),
        val content: List<ContentConfiguration> = listOf(),

        override val elementCount: Int = 1,
        override val outputFilePattern: String = "comdagensharedlibrary.xml",
        override val outputDir: String = "libraries",
        override val templateName: String = "library.ftlx"
) : RenderConfig