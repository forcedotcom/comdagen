/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.config.ContentConfiguration
import com.salesforce.comdagen.config.FolderConfiguration
import com.salesforce.comdagen.config.LibraryConfiguration
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
) : Generator<LibraryConfiguration, Library> {

    private val rng: Random
        get() = Random(configuration.initialSeed)

    /**
     * First the library from the main library configuration file gets created. Following are custom libraries and
     * generated libraries up to the defined number of libraries elementCount.
     */
    override val objects: Sequence<Library>
        get() {
            val maxNumberOfLibraries = configuration.elementCount

            if (maxNumberOfLibraries <= 0)
                return emptySequence()
            var x = emptySequence<Library>()

            // Adding one library with the configured libraryId. If libraryId is null the internId will be 0
            x += creatorFunc(
                0,
                configuration.initialSeed
            )
            // Adding custom libraries
            x += (0 until Math.min(maxNumberOfLibraries - x.count(), configuration.libraries.size)).map { index ->
                Library(index + x.count(), configuration.libraries[index].initialSeed, configuration.libraries[index])
            }

            // Adding generated libraries to fill up
            x += rng.longs(
                Math.max(
                    maxNumberOfLibraries - configuration.libraries.size - 1,
                    0
                ).toLong()
            ).toList().mapIndexed { index, randomLong ->
                creatorFunIndexedLibrary(
                    index + x.count(),
                    randomLong
                )
            }.asSequence()

            if (configuration.renderComdagenSummaryContentAsset)
            // Adding the ComdagenSummaryLibrary that contains the comdagen summary content asset
              x = sequenceOf(Library(
                  1, configuration.initialSeed, LibraryConfiguration(
                      configuration.initialSeed,
                      "ComdagenSummaryLibrary",
                      contentAssetCount = 0,
                      folderCount = 1,
                      folders = listOf(
                          FolderConfiguration(
                              configuration.initialSeed,
                              "root",
                              "Root folder",
                              "Root folder containing the comdagen summary content asset.",
                              true,
                              "root",
                              false,
                              1
                          )
                      ), defaultFolderConfigs = FolderConfiguration(
                          configuration.initialSeed,
                          "root",
                          "Root folder",
                          "Root folder containing the comdagen summary content asset.",
                          true,
                          "root",
                          false,
                          1
                      ),
                      defaultContentAssetConfig = ContentConfiguration(configuration.initialSeed, contentId = null, classificationFolder = null)
                  )
              )) + x

            return x
        }


    /**
     * Creates a library according to the configuration of the generator.
     */
    override val creatorFunc = { idx: Int, seed: Long -> Library(idx, seed, configuration) }

    /**
     * Generates a library according to the configuration of the generator but ignores the libraryId
     */
    val creatorFunIndexedLibrary =
        { idx: Int, seed: Long -> Library(idx, seed, configuration.copy(libraryId = null,
            renderComdagenSummaryContentAsset = false)) }

    // TODO: Implement metadata
    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}