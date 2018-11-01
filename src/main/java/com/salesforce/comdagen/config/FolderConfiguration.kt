/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.salesforce.comdagen.Configuration

/**
 * Folder configuration
 */
data class FolderConfiguration(
    override val initialSeed: Long,
    val folderId: String?,
    val displayName: String?,
    val description: String?,
    val onlineFlag: Boolean = true,
    val importModeDelete: Boolean? = false,
    val parent: String?,
    val randomFolderIds: Boolean = false,

    //TODO: Implement customAttributes

    override val elementCount: Int = 10
) : Configuration