/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.salesforce.comdagen.ExtendableObjectConfig

/**
 * Configure order promotion generation
 */
data class OrderPromotionConfiguration(
    override val elementCount: Int = 5,

    /**
     * min order discount in percent
     */
    val minDiscount: Int = 1,
    /**
     * max order discount in percent
     */
    val maxDiscount: Int = 50,

    /**
     * min order amount to enable promotion (in USD)
     */
    val minThreshold: Float = 0.01F,

    /**
     * max order amount to enable promotion (in USD)
     */
    val maxThreshold: Float = 200.00F,
    override val initialSeed: Long,

    override val customAttributes: Map<String, AttributeConfig>? = null,

    override val generatedAttributes: GeneratedAttributeConfig? = null
) : ExtendableObjectConfig
