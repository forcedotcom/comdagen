package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

import com.salesforce.comdagen.config.FolderConfiguration

/**
 * A abstract class for folders providing generally needed attributes.
 */
abstract class AbstractFolder(
    open val seed: Long,
    private val folderConfig: FolderConfiguration
) {
    // Each folder has a Id, if none defined the folders wil be called unnamed-folder
    open val folderId: String get() = folderConfig.folderId ?: "unnamed-folder"
    open val displayName: String? get() = folderConfig.displayName
    open val description: String? get() = folderConfig.description
    val onlineFlag: Boolean get() = folderConfig.onlineFlag
    val parent: String? get() = folderConfig.parent
}

/**
 * A folder model that sets its [folderId], [displayName] and [description] with the use of the [seed] and a
 * pseudo-RNG.
 */
open class RandomFolder(override val seed: Long, private val folderConfig: FolderConfiguration) :
    AbstractFolder(seed, folderConfig) {
    // Still accept folderId if it is set.
    override val folderId: String
        get() =
            folderConfig.folderId ?: RandomData.getRandomNoun(seed + "randomFolderId_".hashCode())
    override val displayName: String
        get() =
            RandomData.getRandomNoun(seed + "randomFolderDisplayName_".hashCode())
    override val description: String
        get() =
            RandomData.getRandomSentence(seed + "randomFolderDescription_".hashCode())
}

/**
 * This is [RandomFolder] with an additional index mixed in so that we get different [folderId] in
 * the "Folder_<index>" fashion and mix in the index at the random [displayName] and [description] generation.
 */
class IndexedRandomFolder(
    val index: Int,
    override val seed: Long,
    private val folderConfig: FolderConfiguration
) :
    RandomFolder(seed, folderConfig) {
    override val folderId: String
        get() = if (folderConfig.randomFolderIds)
            RandomData.getRandomNoun(seed + "randomFolderId_".hashCode()) else "Folder_$index"
    override val displayName: String
        get() = RandomData.getRandomNoun(seed + "randomFolderDisplayName_".hashCode() + index)
    override val description: String
        get() = RandomData.getRandomSentence(seed + "randomFolderDescription_".hashCode() + index)
}