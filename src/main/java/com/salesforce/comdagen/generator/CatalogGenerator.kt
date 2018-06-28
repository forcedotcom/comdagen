/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.SupportedCurrency
import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.CatalogListConfiguration
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.MasterCatalog
import com.salesforce.comdagen.model.VariationAttribute
import java.util.*

data class CatalogGenerator(
    override val configuration: CatalogListConfiguration,
    private val currencies: List<SupportedCurrency> = listOf(SupportedCurrency.USD),
    private val regions: List<SupportedZone> = listOf(SupportedZone.Generic)
) : Generator<CatalogListConfiguration, MasterCatalog> {

    override val creatorFunc = { idx: Int, seed: Long -> MasterCatalog(seed, configuration, idx, currencies, regions) }

    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = mapOf(
            "Product" to productCustomAttributes,
            "Category" to configuration.attributeDefinitions()
        )

    // This _must_ be consistent with how Catalog.kt generates products or you'll see a mismatch between declared and
    // used custom attributes
    val productCustomAttributes: Set<AttributeDefinition>
        get() {
            val standardProducts = configuration.products.attributeDefinitions()

            val localVariationAttributes = configuration.variationProducts
                .flatMap { it.localVariationAttributes }
                .map { VariationAttribute(it.name, it.values) }
                .toSet()

            val sharedVariationAttributes =
                configuration.sharedVariationAttributes.map { VariationAttribute(it.name, it.values) }.toSet()

            val sharedOptions = objects.flatMap { it.sharedOptions.asSequence() }.toSet()

            val localOptions =
                if (configuration.products.options != null) { // avoid iterating all products when no local options are defined
                    objects.flatMap { catalog ->
                        catalog.products.asSequence().flatMap { it.localOptions.asSequence() }
                    }.toSet()
                } else {
                    emptySet()
                }

            return (standardProducts + localVariationAttributes + sharedVariationAttributes + sharedOptions + localOptions).toSet()
        }

    companion object {
        val EXCHANGE_RATES: Properties = {
            val prop = Properties()
            PricebookGenerator::class.java.getResourceAsStream("/contentfiles/currencies.properties").use {
                prop.load(it)
            }
            prop
        }()
    }
}
