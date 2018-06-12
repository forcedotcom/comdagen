/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.salesforce.comdagen.ExtendableObjectConfig

data class ShippingPromotionConfiguration(
        override val elementCount: Int = 5,

        val minDiscount: Int = 1,

        val maxDiscount: Int = 50,

        val minThreshold: Float = 0.01F,

        val maxThreshold: Float = 200.0F,
        override val initialSeed: Long,

        override val customAttributes: Map<String, AttributeConfig>? = null,

        override val generatedAttributes: GeneratedAttributeConfig? = null
) : ExtendableObjectConfig