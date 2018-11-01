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
    val defaultFolderConfigs: FolderConfiguration,
    val folderCount: Int = 6,

    val libraries: List<LibraryConfiguration> = listOf(),

    val content: List<ContentConfiguration> = listOf(),
    @JsonProperty("contentAssetDefaults")
    val defaultContentAssetConfig: ContentConfiguration,

    override val elementCount: Int = 1,
    override val outputFilePattern: String = "comdagensharedlibrary.xml",
    override val outputDir: String = "libraries",
    override val templateName: String = "library.ftlx"
) : RenderConfig