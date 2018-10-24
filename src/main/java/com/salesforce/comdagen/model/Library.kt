/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.config.LibraryConfiguration
import java.io.File
import java.lang.Integer.max
import java.util.*
import kotlin.streams.toList


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
            val rng = Random(seed + "abstractFolders".hashCode())
            /**
             * Generate all defined abstractFolders from the libraries config plus the default abstractFolders:
             * "root", "ComdagenContent" and "ComdagenContentAssets". Fill up with random, indexed Folders
             * up to folderCount abstractFolders.
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

/**
 * This is the model of a library which has its own attributes, contents and abstractFolders.
 */
class Library(
    /**
     * This internId is necessary to set a unique libraryId when
     * none is defined in the configuration file. The internId
     * will be related to the order in which the libraries are
     * generated. internId = 0 is reserved for the ComdagenSharedLibrary
     */
    val internId: Int,
    override val seed: Long,
    private val config: LibraryConfiguration,
    private val configDir: File
) : AbstractLibrary(seed, config) {
    // Use the predefined libraryId or the internal id for the libraryId
    override val libraryId: String get() = config.libraryId ?: "Library_"+internId.toString()

    // Check for the ComdagenSharedLibrary used in freemarker template
    val comdagenShared: Boolean get() = internId == 0
}