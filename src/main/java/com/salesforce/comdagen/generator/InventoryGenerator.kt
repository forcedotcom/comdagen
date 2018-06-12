/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.GeneratorHelper
import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.CatalogListConfiguration
import com.salesforce.comdagen.config.InventoryConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Inventory
import java.util.*

data class InventoryGenerator(override val configuration: InventoryConfiguration,
                              private val catalogConfiguration: CatalogListConfiguration)
    : Generator<InventoryConfiguration, Inventory> {

    override val objects: Sequence<Inventory>
        get() {
            // the seed _must_ be the one used for CatalogGenerator, or we'll never get corresponding pids.
            val allProducts = GeneratorHelper.getProductIds(catalogConfiguration)

            val rng = Random(configuration.initialSeed)
            val seeds = (1..configuration.elementCount).map { rng.nextLong() }

            return seeds.asSequence().mapIndexed { idx, seed ->
                val productIds = GeneratorHelper.getPartialProductSequence(seed, catalogConfiguration.totalProductCount(), configuration.coverage, allProducts)
                Inventory(productIds, seed, configuration, metadata["ProductInventoryList"].orEmpty(), metadata["ProductInventoryRecord"].orEmpty(),
                        idx, catalogConfiguration.hashCode())
            }
        }

    override val metadata: Map<String, Set<AttributeDefinition>> = mapOf(
            "ProductInventoryList" to configuration.attributeDefinitions(),
            "ProductInventoryRecord" to configuration.attributeDefinitions()
    )
}
