/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.config.LibraryConfiguration
import java.util.*
import kotlin.streams.toList

/**
 * This is the model of a library which has its own attributes, contents and folders.
 */
class Library(
    /**
     * This internId is necessary to set a unique libraryId when
     * none is defined in the configuration file. The internId
     * will be related to the order in which the libraries are
     * generated. internId = 0 is reserved for the base library in the
     * configuration file.
     */
    private val internId: Int,
    val seed: Long,
    val config: LibraryConfiguration
) {

    // Use the predefined libraryId or the internal id for the libraryId
    val libraryId: String get() = config.libraryId ?: "Library_$internId"

    /**
     * Generate up to content assets up to the count contentAssetCount which is defined in the
     * configuration. If the comdagen summary content asset gets created, the list of contentAssets
     * will be one short. The comdagen summary nevertheless will get rendered at the freemarker template.
     */
    val contentAssets: List<RandomContentAsset>
        get() {
            val rng = Random(seed + "contentassets".hashCode())
            val requestedGeneratedContentAssets: Long = config.contentAssetCount.toLong()
            return rng.longs(requestedGeneratedContentAssets).toList().mapIndexed { index, it ->
                IndexedRandomContentAsset(
                    index + 1,
                    it,
                    config.defaultContentAssetConfig,
                    SupportedZone.values().asList()
                )
            }
        }
    /**
     * Generate up to config.folderCount (from config) folders. Start by creating custom defined folders. Fill up with
     * generated, random indexed folders using the default folder configuration.
     */
    val folders: List<RandomFolder>
        get() {
            val rng = Random(seed + "folders".hashCode())
            // Create custom folders up to config.folderCount
            return ((1..Math.min(config.folderCount, config.folders.size)).map {
                RandomFolder(rng.nextLong(), config.folders[it - 1])
                // Fill with generated folders up to config.folderCount
            }.asSequence() + (1..config.folderCount - config.folders.size).map {
                IndexedRandomFolder(
                    it,
                    rng.nextLong(),
                    config.defaultFolderConfigs
                )
            }.asSequence()).toList()
        }
}