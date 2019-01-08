/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.config.PricebookConfiguration
import com.salesforce.comdagen.generator.CatalogGenerator
import java.util.*

/**
 * Abstract Pricebook
 *
 * @property config pricebook configuration
 * @property seed pseudo randomization seed
 * @property productIds the product ids to generate price entries for
 * @property index this is the N-th price book (out of [PricebookConfiguration.elementCount] in total)
 * @property catalogHashCode hash code of the catalog config the price book is based on (differentiates price books for
 *                        different catalog configurations)
 * @property pricetables each list entry defines price for one product
 * @property productIds the catalog products, mapped to their [Product.id]
 *
 * @property id unique identifier of the pricebook
 * @property currency price book defines prices for one currency
 * @property customAttributes list of custom attributes
 */
abstract class Pricebook(
    protected val config: PricebookConfiguration, val currency: String, protected val seed: Long,
    private val attributeDefinitions: Set<AttributeDefinition>,
    private val productIds: Sequence<String>,
    private val index: Int, private val catalogHashCode: Int
) {
    val pricetables: Sequence<PriceTable>
        get() {
            val rng = Random(seed)
            return productIds.map { PriceTable(it, rng.nextLong(), config, currency, salePriceBook) }
        }

    open val parentId: String? = null

    open val salePriceBook: Boolean = false

    val id: String
        get() = "${config.id}-$currency-${Math.abs(config.hashCode() * catalogHashCode)}-$index"

    val customAttributes: List<CustomAttribute>
        get() {
            val rng = Random(seed + "customAttributes".hashCode())
            return attributeDefinitions.map { CustomAttribute(it, rng.nextLong()) }
        }
}

/**
 * Represents a price for a product and a specific quantity
 *
 * @param seed randomization seed
 *
 * @param config configuration of the pricebook the amount belongs to
 *
 * @param currency currency of the price
 *
 * @param sale is the price a sales price?
 *
 * @property quantity how many products do you have to order to get this price?
 *
 * @property amount price of the product for a given quantity
 *
 * @author ojauch
 */
class Amount(
    private val seed: Long, private val config: PricebookConfiguration, val quantity: Int,
    private val currency: String, private val sale: Boolean
) {
    val amount: Double
        get() {
            val rng = Random(seed)
            val price = (config.minAmount + (config.maxAmount - config.minAmount) * rng.nextDouble()) / quantity
            val exchangeRate: Double = CatalogGenerator.EXCHANGE_RATES.getProperty(currency).toDouble()

            // 10% discount for sales pricelist
            if (sale) {
                return price * exchangeRate * 0.9
            }

            return price * exchangeRate
        }
}

/**
 * Represents prices for different quantities for a single product
 *
 * @param seed randomization seed
 *
 * @param config configuration of the pricebook the PriceTable belongs to
 *
 * @param currency currency of the generated prices
 *
 * @param sale are the prices sale prices?
 *
 * @property productId id of the product
 *
 * @property amounts list of different prices for one product
 *
 * @author ojauch
 */
class PriceTable(
    val productId: String, private val seed: Long, private val config: PricebookConfiguration,
    private val currency: String, private val sale: Boolean
) {
    val amounts: List<Amount>
        get() {
            val rng = Random(seed)

            var amountCount = 1
            if (config.maxAmountCount > 1) {
                amountCount = if (config.maxAmountCount > config.minAmountCount)
                    rng.nextInt(config.maxAmountCount - config.minAmountCount) + config.minAmountCount
                else config.minAmountCount

            }
            return (1..amountCount).map { quantity -> Amount(seed, config, quantity, currency, sale) }
        }
}

/**
 * Root of several price books.
 */
class ParentPriceBook(
    productIds: Sequence<String>,
    seed: Long, attributeDefinitions: Set<AttributeDefinition>, config: PricebookConfiguration,
    currency: String, index: Int, catalogHashCode: Int
) : Pricebook(config, currency, seed, attributeDefinitions, productIds, index, catalogHashCode)

/**
 * Represents a single pricebook that has a parent pricebook, for example a sales pricebook that defines
 * cheaper prices for a subset of the products
 *
 * @param parentPriceBook pricebook that should be the parent pricebook of the child
 *
 * @param seed pseudo random data seed
 *
 * @param config child pricebook configuration
 *
 * @param currency currency of the defined prices
 */
class ChildPricebook(
    private val parentPriceBook: ParentPriceBook,
    productIds: Sequence<String>,
    seed: Long,
    attributeDefinitions: Set<AttributeDefinition>,
    config: PricebookConfiguration,
    currency: String,
    index: Int,
    catalogHashCode: Int
) : Pricebook(config, currency, seed, attributeDefinitions, productIds, index, catalogHashCode) {

    // by default child price books are for sales (get 10% discount applied)
    override val salePriceBook: Boolean
        get() = true

    override val parentId: String
        get() = parentPriceBook.id
}
