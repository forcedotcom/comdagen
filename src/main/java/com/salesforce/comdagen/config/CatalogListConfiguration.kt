/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.RenderConfig

/**
 * Root configuration for CatalogGenerator
 */
@JsonRootName("catalogs")
@JsonIgnoreProperties(ignoreUnknown = true)
data class CatalogListConfiguration(
        /**
         * The number of master catalogs.
         */
        override val elementCount: Int = 5,

        override val initialSeed: Long,

        @JsonProperty("categories")
        override val categoryConfig: CategoryConfiguration = CategoryConfiguration(),

        /**
         * configuration for product generation
         */
        val products: ProductConfiguration = ProductConfiguration(elementCount = 100, initialSeed = initialSeed),

        /**
         * configuration for variation master products
         */
        val variationProducts: List<VariationProductConfiguration> = emptyList(),

        /**
         * configuration for product bundles
         */
        val bundleConfig: BundleProductConfiguration? = null,

        /**
         * configuration for product sets
         */
        val productSets: ProductSetConfiguration? = null,

        /**
         * Define custom attributes for the generated _products_.
         */
        override val customAttributes: Map<String, AttributeConfig> = emptyMap(),

        /**
         * configuration for randomly generated custom attributes
         */
        override val generatedAttributes: GeneratedAttributeConfig? = null,

        /**
         * configure shared variation attributes for catalog
         */
        val sharedVariationAttributes: List<VariationAttributeConfiguration> = emptyList(),

        /**
         * configuration for shared product options
         */
        val sharedOptions: ProductOptionConfiguration? = null,


        override val outputFilePattern: String = "catalog.xml",

        override val outputDir: String = "catalogs",

        override val templateName: String = "catalogs.ftlx"

) : RenderConfig, CatalogConfiguration {

    init {
        variationProducts.forEach {
            it.attributes = it.localVariationAttributes +
                    (sharedVariationAttributes.filter { attr -> it.sharedVariationAttributes.contains(attr.name) })
        }

    }

    /**
     * How many products will be in the catalogs described by this configuration?
     * This _must_ match the number of product ids generated in [GeneratorHelper#generateProductIds].
     */
    fun totalProductCount() =
            /* each catalog */ elementCount *
            (/* standard products */ products.elementCount
                    + /* variation products */ (variationProducts.sumBy { it.elementCount * (it.sharedVariationAttributes.size + it.localVariationAttributes.size) })
                    + /* bundles */ (bundleConfig?.elementCount ?: 0)
                    + /* sets */ (productSets?.elementCount ?: 0))
}
