/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.config.LibraryConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Library
import java.io.File

/**
 * This generates the sequence of libraries. To get different libraries
 * the seed is adjusted by the index of the library.
 */
data class LibraryGenerator(
    override val configuration: LibraryConfiguration,
    val configDir: File
) : Generator<LibraryConfiguration, Library> {
    override val objects: Sequence<Library>
        get() = (1..configuration.elementCount).map {
            Library(it, configuration.initialSeed, configuration, configDir)
            // Add custom ComdagenSharedLibrary
        }.plus(
            Library(
                0,
                configuration.initialSeed,
                LibraryConfiguration(
                    configuration.initialSeed,
                    "ComdagenSharedLibrary",
                    configuration.contentAssetCount,
                    emptyList(),
                    emptyList(),
                    configuration.defaultContentAssetConfig,
                    1,
                    "ComdagenSharedLibrary.xml",
                    "libraries",
                    configuration.templateName
                ),
                configDir
            )
        ).asSequence()

    override val creatorFunc = { idx: Int, seed: Long -> Library(idx, seed, configuration, configDir) }

    //TODO: Implement metadata
    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}