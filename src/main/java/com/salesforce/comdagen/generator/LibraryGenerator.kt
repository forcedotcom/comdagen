/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.config.LibraryConfiguration
import com.salesforce.comdagen.model.AbstractLibrary
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Library
import java.io.File
import java.util.*
import kotlin.streams.toList

/**
 * This generates the sequence of libraries. To get different libraries
 * the seed is adjusted by the index of the library.
 */
data class LibraryGenerator(
    override val configuration: LibraryConfiguration,
    val configDir: File
) : Generator<LibraryConfiguration, AbstractLibrary> {

    private val rng: Random
        get() = Random(configuration.initialSeed)


    override val objects: Sequence<AbstractLibrary>
        get() {
            // The default generated library comdagenSharedLibrary
            val comdagenSharedLibrary = Library(
                0,
                configuration.initialSeed,
                LibraryConfiguration(
                    configuration.initialSeed,
                    "ComdagenSharedLibrary",
                    configuration.contentAssetCount,
                    emptyList(),
                    configuration.defaultFolderConfigs,
                    configuration.folderCount,
                    emptyList(),
                    emptyList(),
                    configuration.defaultContentAssetConfig,
                    1,
                    "ComdagenSharedLibrary.xml",
                    "libraries",
                    configuration.templateName
                )
            )
            return (1..configuration.libraries.size).map { index ->
                creatorFunc(index, configuration.libraries[index - 1].initialSeed)
            }.plus(comdagenSharedLibrary) // Adding ComdagenSharedLibrary
                .plus(
                    rng.longs(
                        Math.max(
                            // Fill up to elementCount libraries
                            configuration.elementCount - configuration.libraries.size - 1,
                            0
                        ).toLong()
                    ).toList().mapIndexed { index, randomLong ->
                        creatorFunc(
                            configuration.libraries.size + 1 + index,
                            randomLong
                        )
                    }).asSequence()
        }

    override val creatorFunc = { idx: Int, seed: Long -> Library(idx, seed, configuration) }

    // TODO: Implement metadata
    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}