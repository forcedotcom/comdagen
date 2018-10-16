/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.config.ContentConfiguration
import com.salesforce.comdagen.config.FolderConfiguration
import com.salesforce.comdagen.config.LibraryConfiguration
import java.io.File
import java.util.*

/**
 * This is the model of a library which has its own attributes, contents and folders.
 */
data class Library(
    /**
     * This internId is necessary to set a unique libraryId when
     * none is defined in the configuration file. The internId
     * will be related to the order in which the libraries are
     * generated.
     */
    private val internId: Int,
    private val seed: Long,
    private val config: LibraryConfiguration,
    private val configDir: File
) {
    // Use the predefined libraryId or the internal id for the libraryId
    val libraryId: String get() = config.libraryId ?: "Library_"+internId.toString()

    val contentAssets: List<Content>
        get() {
            val rng = Random(seed + "contentassets".hashCode())
            return (1..config.contentAssetCount).map {
                Content(
                    it,
                    rng.nextLong(),
                    config.defaultContentAssetConfig,
                    SupportedZone.values().asList()
                )
            }
        }

    val folders: List<Folder>
        get() {
            val rng = Random(seed + "folders".hashCode())
            return config.folders.map {
                Folder(rng.nextLong(), it)
            }
        }
}

/**
 * This resembles one content asset inside a library.
 */
data class Content(
    private val internId: Int,
    val initialSeed: Long,
    val contentConfig: ContentConfiguration,
    val regions: List<SupportedZone>
) {

    val attributeId: String get() = contentConfig.attributeId

    //TODO: BUG: contentId is always null?? dont use get() here for ftlx
    val contentId: String
        get() {
            // If defined use random contentIds or ContentId_<number>
            return if (contentConfig.useRandomContentIds)
                RandomData.getRandomNoun(initialSeed + "contentId".hashCode())
            else "ContentId_" + internId.toString()

        }
    val name: Map<SupportedZone, String>
        get() = regions.associateBy(
            { it },
            { RandomData.getRandomNoun(initialSeed + "name".hashCode(), it) })

    val description: Map<SupportedZone, String>
        get() = regions.associateBy(
            { it },
            { RandomData.getRandomSentence(initialSeed + "description".hashCode(), it) })

    val onlineFlag: Boolean get() = contentConfig.onlineFlag

    val searchable: Boolean get() = contentConfig.searchableFlag

    val folderId: String? get() = contentConfig.classificationFolder

    val importMode: Boolean? get() = contentConfig.importModeDelete

    val classificationFolder: String? get() = contentConfig.classificationFolder

    /**
     * This implements customAttributes for libraries.
     */
    /*
     * Should I move this into the freemarkertemplate? Need nested region tags then and might loose flexibility
     */
    val customBody: Map<SupportedZone, String>
        get() = SupportedZone.values().associateBy({ it },
            {
                " <div style=\"padding:24px 16px 0 16px; font-size:1.1em;\">\n" +
                        "                    <h1 style=\"color: rgb(86, 79, 71);\">" +
                        RandomData.getRandomNoun(initialSeed + "header".hashCode(), it) + "</h1>\n" +
                        "                    <hr />\n                    " + "<p style=\"margin:0 0 8px 0;\">" +
                        RandomData.getRandomSentence(initialSeed + "body".hashCode(), it) + "</p>"
            })
}

// TODO: Create IndexedFolder
data class Folder(
    val initialSeed: Long,
    val folderConfig: FolderConfiguration
) {
    // Each folder has a Id, if none defined the folders wil be called unnamed-folder
    val folderId: String get() = folderConfig.folderId ?: "unnamed-folder"
    val displayName: String? get() = folderConfig.displayName
    val description: String? get() = folderConfig.description
    val onlineFlag: Boolean get() = folderConfig.onlineFlag
    val parent: String? get() = folderConfig.parent
}