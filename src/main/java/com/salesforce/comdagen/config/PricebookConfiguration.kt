/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.ExtendableObjectConfig
import com.salesforce.comdagen.RenderConfig
import com.salesforce.comdagen.SupportedCurrency

/**
 * Root configuration for PricebookGenerator
 */
@JsonRootName("pricebooks")
@JsonIgnoreProperties(ignoreUnknown = true)
data class PricebookConfiguration(
    /**
     * pricebook id (must be unique)
     */
    val id: String,

    /**
     * PriceTables for how many products should get generated?
     * >= 1 -> generate pricetables for all products
     * 0.75 -> generate pricetables for 75% of the products
     */
    val coverage: Float = 1f,

    /**
     * minimum amount value in USD
     */
    val minAmount: Double = 0.01,

    /**
     * maximum amount value in USD
     */
    val maxAmount: Double = 2000.0,

    /**
     * minimum number of values per product
     */
    val minAmountCount: Int = 1,

    /**
     * maximum number of values per product
     */
    val maxAmountCount: Int = 5,

    /**
     * overwrites site currencies
     */
    val currencies: List<SupportedCurrency>? = null,

    /**
     * is this pricebook a sales pricebook?
     */
    val sales: Boolean = false,

    /**
     * custom attributes for pricebooks
     */
    override val customAttributes: Map<String, AttributeConfig>? = null,
    override val localizableCustomAttributes: Map<String, AttributeConfig>? = null,

    /**
     * randomly generate custom attributes
     */
    override val generatedAttributes: GeneratedAttributeConfig? = null,

    /**
     * child pricebooks get generated for each parent pricebook
     */
    val children: List<PricebookConfiguration>? = null,

    override val elementCount: Int = 1,
    override val initialSeed: Long,
    override val outputFilePattern: String = "pricebooks\${i}.xml",
    override val outputDir: String = "pricebooks",
    override val templateName: String = "pricebooks.ftlx"
) : RenderConfig, ExtendableObjectConfig {
    init {
        require(maxAmount >= minAmount, { "maxAmount needs to be greater equal minAmount" })
        require(maxAmountCount >= minAmountCount, { "maxAmountCount needs to be greater equal minAmountCount" })
    }
}
