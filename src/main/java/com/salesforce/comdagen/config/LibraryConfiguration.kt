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
    val generatedContentAssetCount: Int = 3,
    val folders: List<FolderConfiguration> = listOf(),
    @JsonProperty("folderDefaults")
    val defaultFolderConfigs: FolderConfiguration = FolderConfiguration(initialSeed, null, null, null, parent = null),
    val generatedFolderCount: Int = 6,
    @JsonProperty("generateSummaryContentAsset")
    val renderComdagenSummaryContentAsset: Boolean = true,
    @JsonProperty("contentAssetDefaults")
    val defaultContentAssetConfig: ContentConfiguration = ContentConfiguration(initialSeed, null, null),

    override val elementCount: Int = 1,
    override val outputFilePattern: String = "",
    override val outputDir: String = "libraries",
    override val templateName: String = "library.ftlx"
) : RenderConfig