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

    /**
     * First the library from the main library configuration file gets created. Following are custom libraries and
     * generated libraries up to the defined number of libraries elementCount.
     */
    override val objects: Sequence<Library>
        get() = sequenceOf(Library(configuration.initialSeed, configuration))


    // TODO: Implement metadata
    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}