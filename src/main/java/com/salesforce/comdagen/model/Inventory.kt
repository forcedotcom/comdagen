/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.InvalidComdagenConfigurationValueException
import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.config.InventoryConfiguration
import com.salesforce.comdagen.config.InventoryRecordConfiguration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Represents an inventory list.
 *
 * @property productIds list of product ids for which inventory records get generated
 *
 * @property seed pseudo randomization seed
 *
 * @property config inventory list configuration
 *
 * @property index used to differentiate multiple inventory lists based on the same catalog
 *
 * @property catalogHashCode hash code of the catalog configuration, used to differentiate inventory lists based on different
 *                        catalogs
 */
class Inventory(
    private val productIds: Sequence<String>,
    private val seed: Long,
    private val config: InventoryConfiguration,
    private val inventoryExtraAttributes: Set<AttributeDefinition>,
    private val recordExtraAttributes: Set<AttributeDefinition>,
    private val index: Int,
    private val catalogHashCode: Int
) {

    val defaultInstock: Boolean
        get() = false

    val description: String
        get() = RandomData.getRandomSentence(seed + "inventoryDescription".hashCode())

    val customAttributes: List<CustomAttribute>
        get() {
            val rng = Random(seed + "customAttributes".hashCode())
            return inventoryExtraAttributes.map { CustomAttribute(it, rng.nextLong()) }
        }

    val listId: String
        get() = "inventory-$index-${config.hashCode() + catalogHashCode}"

    val inventoryRecords: Sequence<InventoryRecord>
        get() {
            val rng = Random(seed)
            return productIds.map {
                InventoryRecord(
                    rng.nextLong(),
                    it,
                    recordExtraAttributes,
                    config.inventoryRecords
                )
            }
        }

}

/**
 * Represents an inventory record for one product.
 *
 * @property seed pseudo randomization seed
 *
 * @property productId id of the product
 *
 * @property config configuration of the inventory record
 */
class InventoryRecord(
    private val seed: Long, val productId: String, private val attributeDefinitions: Set<AttributeDefinition>,
    private val config: InventoryRecordConfiguration
) {

    val allocation: Int
        get() = when {
            config.maxCount > config.minCount -> Random(seed).nextInt(config.maxCount - config.minCount) +
                    config.minCount
            config.maxCount == config.minCount -> config.minCount
            else -> throw InvalidComdagenConfigurationValueException(
                "minCount value ${config.minCount} needs to be bigger than maxCount " +
                        "${config.maxCount} in the inventories.yaml configuration file."
            )
        }

    // use yesterday as allocation date to not conflict with different timezones
    val allocationDateTime: String
        get() = DateTimeFormatter.ISO_INSTANT.format(LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC))

    val ats: Int
        get() = when {
            config.maxCount > config.minCount -> Random(seed).nextInt(config.maxCount - config.minCount) +
                    config.minCount
            config.maxCount == config.minCount -> config.minCount
            else -> throw InvalidComdagenConfigurationValueException(
                "minCount value ${config.minCount} needs to be bigger than maxCount " +
                        "${config.maxCount} in the inventories.yaml configuration file."
            )
        }

    val perpetual: Boolean
        get() = false

    val customAttributes: List<CustomAttribute>
        get() {
            val rng = Random(seed + "customAttributes".hashCode())
            return attributeDefinitions.map { CustomAttribute(it, rng.nextLong()) }
        }
}
