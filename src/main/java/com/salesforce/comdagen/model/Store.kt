/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.StoreConfiguration
import java.util.*

data class Store(private val seed: Long, private val config: StoreConfiguration) {
    val name: String
        get() = RandomData.getRandomNoun(seed + "storeName".hashCode())

    val id: String
        get() = "comdagen-${Math.abs(seed + "storeId".hashCode())}"

    val coordinates: Pair<Double, Double>
        get() {
            if (config.centerLongitude != null && config.centerLatitude != null && config.distance != null) {
                val rng = Random(seed)

                // convert radius from kilometers to degrees
                val r: Double = (config.distance * 1000.0) / 111300.0

                // generate 2 random numbers
                val u = rng.nextDouble()
                val v = rng.nextDouble()

                // generate coordinates in the distance radius of the center point
                val w: Double = r * Math.sqrt(u)
                val t: Double = 2 * Math.PI * v
                val x: Double = (w * Math.cos(t)) / Math.cos(config.centerLongitude)
                val y: Double = w * Math.sin(t)

                return Pair(x + config.centerLatitude, y + config.centerLongitude)
            }

            val rng = Random(seed + "storeCoordinates".hashCode())

            val latitude = (180.0 * rng.nextDouble()) - 90.0
            val longitude = (360.0 * rng.nextDouble()) - 180.0

            return Pair(latitude, longitude)
        }

    val customAttributes: List<CustomAttribute>
        get() = config.attributeDefinitions().map { CustomAttribute(it, seed) }
}
