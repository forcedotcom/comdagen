/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.RenderConfig


/**
 * Configuration file for a library
 */
@JsonRootName("libraryConfig")
@JsonIgnoreProperties(ignoreUnknown = true)
data class LibraryConfiguration(
    override val initialSeed: Long = 1234,
    val libraryId: String?,
    val contentAssetCount: Int = 3,
    val folders: List<FolderConfiguration> = listOf(),
    @JsonProperty("folderDefaults")
    val defaultFolderConfigs: FolderConfiguration =
        FolderConfiguration(initialSeed, null, null, null, parent = null),
    val folderCount: Int = 6,
    /* If set to true generates a library called 'ComdagenSummaryLibrary' and the folders 'root' and the content
    asset 'ComdagenSummary'. The generated library does not need to fulfill specified general requirements such as
    a specific count of content assets or folders.
    '*/
    @JsonProperty("generateSummaryContentAsset")
    val renderComdagenSummaryContentAsset: Boolean = false,

    val libraries: List<LibraryConfiguration> = listOf(),
    @JsonProperty("contentAssetDefaults")
    val defaultContentAssetConfig: ContentConfiguration = ContentConfiguration(initialSeed, null),

    @JsonProperty("libraryCount")
    override val elementCount: Int = 3,
    override val outputFilePattern: String = "",
    override val outputDir: String = "libraries",
    override val templateName: String = "library.ftlx"
) : RenderConfig {
    init {
        require(folderCount >= 1) { "There need to be at least one folder to insert generated content assets." }
    }

    /**
     * Returns the number of content assets that will be generated by this library and its custom libraries.
     */
    fun totalContentAssetCount() =
        /* Number of default generated content assets */ contentAssetCount +
            /* Number of content assets generated by custom libraries */ libraries.sumBy { it.contentAssetCount }

    /**
     * Returns the number of folders that will be rendered in the template.
     */
    fun totalFolderCount(): Int {
        val x =        /* Number of default generated folders */ folderCount +
                /* Number of folders generated by custom libraries */ libraries.sumBy { it.folderCount }
        return if (renderComdagenSummaryContentAsset) x + 1 else x
    }

}