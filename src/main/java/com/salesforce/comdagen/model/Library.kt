/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.config.LibraryConfiguration
import java.lang.Integer.max
import java.util.*
import kotlin.streams.toList

/**
 * Abstract class of a library that contains content assets and folders.
 */
abstract class AbstractLibrary(
    open val seed: Long,
    private val config: LibraryConfiguration
) {
    // Use the predefined libraryId or the internal id for the libraryId
    open val libraryId: String get() = config.libraryId ?: "unnamed_library"

    open val contentAssets: List<AbstractContent>
        get() {
            val rng = Random(seed + "contentassets".hashCode())
            return (1..config.contentAssetCount).map {
                RandomContent(
                    rng.nextLong(),
                    config.defaultContentAssetConfig,
                    SupportedZone.values().asList()
                )
            }
        }

    open val folders: List<AbstractFolder>
        get() {
            val rng = Random(seed + "randomFolders".hashCode())
            /**
             * Generate all defined folders from the libraries config plus the default folders:
             * "root", "ComdagenContent" and "ComdagenContentAssets". Fill up with random folders
             * up to folderCount folders.
             */
            return config.folders.asSequence().map { it ->
                RandomFolder(rng.nextLong(), it)
            }.plus(
                rng.longs(
                    max(
                        0,
                        config.folderCount - config.folders.size - 3
                    ).toLong()
                ).toList().map { it -> RandomFolder(it, config.defaultFolderConfigs) }
            ).toList()
        }
}

// TODO: Add support for different libraries allowing custom libraries and custom seeds!

/**
 * This is the model of a library which has its own attributes, contents and folders.
 */
class Library(
    /**
     * This internId is necessary to set a unique libraryId when
     * none is defined in the configuration file. The internId
     * will be related to the order in which the libraries are
     * generated. internId = 0 is reserved for the ComdagenSharedLibrary
     */
    private val internId: Int,
    override val seed: Long,
    private val config: LibraryConfiguration
) : AbstractLibrary(seed, config) {
    // Use the predefined libraryId or the internal id for the libraryId
    override val libraryId: String get() = config.libraryId ?: "Library_"+internId.toString()

    // Check if this library is the "ComdagenSharedLibrary". This is called from the freemarker template
    val comdagenShared: Boolean get() = internId == 0

    override val contentAssets: List<AbstractContent>
        get() {
            val rng = Random(seed + "contentassets".hashCode())
            return rng.longs(config.contentAssetCount.toLong()).toList().mapIndexed { index, it ->
                IndexedRandomContent(
                    index + 1,
                    it,
                    config.defaultContentAssetConfig,
                    SupportedZone.values().asList()
                )
            }
        }

    override val folders: List<AbstractFolder>
        get() {
            val rng = Random(seed + "abstractFolders".hashCode())
            /**
             * Generate all defined abstractFolders from the libraries config plus the default folders:
             * "root", "ComdagenContent" and "ComdagenContentAssets". Fill up with random, indexed folders
             * up to folderCount folders.
             */
            return config.folders.asSequence().map { it ->
                RandomFolder(rng.nextLong(), it)
            }.plus(
                rng.longs(
                    max(
                        0,
                        config.folderCount - config.folders.size - 3
                    ).toLong()
                    // FolderId starts with 1 thus "Folder_1"
                ).toList().mapIndexed { index, it -> IndexedRandomFolder(index + 1, it, config.defaultFolderConfigs) }
            ).toList()
        }
}