/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen

import com.salesforce.comdagen.config.CatalogListConfiguration
import com.salesforce.comdagen.model.MasterCatalog
import com.salesforce.comdagen.model.MasterProduct
import com.salesforce.comdagen.model.Product
import java.util.*

object GeneratorHelper {

    private val productIds: MutableMap<CatalogListConfiguration, Sequence<String>> = mutableMapOf()

    fun getProductIds(catalogConfig: CatalogListConfiguration, regions: List<SupportedZone> = listOf(SupportedZone.Generic)): Sequence<String> {
        return productIds.getOrPut(catalogConfig) { generateProductIds(catalogConfig) }
    }

    private fun generateProductIds(catalogConfig: CatalogListConfiguration, regions: List<SupportedZone> = listOf(SupportedZone.Generic)): Sequence<String> {
        val rng = Random(catalogConfig.initialSeed)
        // can not use rng inside the sequence, as re-running the sequence will use the advanced state of the RNG and
        // generate a different sequence.
        // so we save the seeds and use them as a starting point for the catalog sequences
        val seeds = (1..catalogConfig.elementCount).map { rng.nextLong() }

        // get product ids for each catalog configured in catalogConfig
        return seeds.asSequence().flatMap { catalogSeed -> generateProductIdsForCatalog(catalogSeed, catalogConfig, regions) }
    }

    /**
     * Generate sequence of product ids. Be careful in how you specify the seed: this _must_ match what [MasterCatalog.products] etc do.
     */
    private fun generateProductIdsForCatalog(seed: Long, catalogConfig: CatalogListConfiguration, regions: List<SupportedZone>)
            : Sequence<String> {

        // get standard product ids
        val standardProducts: Sequence<String> = ((1..catalogConfig.products.elementCount).asSequence().map {
            Product.generateId(Math.abs(seed + "product$it".hashCode()))
        })

        // get variation product ids
        // TODO: find smarter solution for variant product id generation

        val variationMasterConfig = catalogConfig.variationProducts
        val variantProducts: Sequence<String> = variationMasterConfig.asSequence().flatMap { config ->
            (1..config.elementCount).asSequence().flatMap {
                val masterProduct = MasterProduct(seed + "master$it".hashCode() + config.hashCode(), regions, config)
                masterProduct.variants.asSequence().map { it.id }
            }
        }


        // get bundle product ids
        val bundleConfig = catalogConfig.bundleConfig
        val bundles: Sequence<String> = if (bundleConfig != null) {
            ((1..bundleConfig.elementCount).asSequence().map {
                Product.generateId(Math.abs(seed * "bundleProduct$it".hashCode()))
            })
        } else emptySequence()

        // get product set ids
        val productSetConfig = catalogConfig.productSets
        val productSets: Sequence<String> = if (productSetConfig != null) {
            ((1..productSetConfig.elementCount).asSequence().map {
                Product.generateId(Math.abs(seed * "productSet$it".hashCode()))
            })
        } else emptySequence()

        return standardProducts + variantProducts + bundles + productSets
    }

    fun getPartialProductSequence(seed: Long, totalProductCount: Int, coverage: Float, allProductIds: Sequence<String>): Sequence<String> {
        return if (coverage < 1f) {
            val productsNotCovered = Random(seed).nextInt(
                    (totalProductCount * (1 - coverage)).toInt())
            allProductIds.drop(productsNotCovered).take((totalProductCount * coverage).toInt())
        } else {
            allProductIds
        }
    }
}
