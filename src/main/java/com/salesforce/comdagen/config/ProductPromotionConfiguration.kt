/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.salesforce.comdagen.ExtendableObjectConfig

data class ProductPromotionConfiguration(
    override val elementCount: Int = 5,

    /**
     * minimum discount for a product in percent
     */
    val minDiscount: Int = 1,

    /**
     * maximum discount for a product in percent
     */
    val maxDiscount: Int = 50,
    override val initialSeed: Long,

    override val customAttributes: Map<String, AttributeConfig>? = null,
    override val localizableCustomAttributes: Map<String, AttributeConfig>? = null,

    override val generatedAttributes: GeneratedAttributeConfig? = null
) : ExtendableObjectConfig {
    init {
        require(maxDiscount >= minDiscount, { "maxDiscount needs to be greater equal minDiscount" })
    }
}