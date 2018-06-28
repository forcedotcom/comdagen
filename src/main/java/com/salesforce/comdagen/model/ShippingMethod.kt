/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.ShippingConfiguration
import java.util.*

data class ShippingMethod(
    private val seed: Long,
    private val config: ShippingConfiguration,
    val default: Boolean = false
) {
    val priceTable: List<AmountEntry>
        get() {
            val priceTableSeed = seed + "priceTable".hashCode()

            val orderValues = arrayOf(0F, 0.01F, 100F, 200F, 500F)

            return orderValues.mapIndexed { index, orderValue ->
                AmountEntry(priceTableSeed, config, orderValue, index)
            }
        }

    val id: String
        get() = "comdagen-${Math.abs(seed + "shippingMethodId".hashCode())}"

    val name: String
        get() = RandomData.getRandomNoun(seed + "shippingMethodName".hashCode())

    val description: String
        get() = RandomData.getRandomSentence(seed + "shippingMethodDescription".hashCode())

    val customAttributes: List<CustomAttribute>
        get() = config.attributeDefinitions().map { CustomAttribute(it, seed) }
}

data class AmountEntry(
    private val seed: Long,
    private val config: ShippingConfiguration,
    val orderValue: Float,
    private val factor: Int
) {
    val amount: Float
        get() = Random(seed).nextFloat() * ((config.maxPrice - config.minPrice) + config.minPrice) * factor
}
