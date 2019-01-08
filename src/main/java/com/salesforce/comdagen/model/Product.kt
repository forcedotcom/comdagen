/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.google.common.collect.Sets
import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.SupportedCurrency
import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.config.*
import com.salesforce.comdagen.generator.CatalogGenerator
import java.util.*

/**
 * Abstract definition of a SKU.
 *
 * @property seed all attributes derive from this
 * @property regions all the languages we need to support
 */
sealed class Product {
    abstract val seed: Long

    abstract val regions: List<SupportedZone>

    val id: String
        get() = generateId(seed)

    val name: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.getRandomNoun(seed + "name".hashCode(), it) })

    val shortDescription: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.bookCite(seed + "shortDescription".hashCode(), 200, it) })

    val longDescription: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.bookCite(seed + "longDescription".hashCode(), 1000, it) })

    val pageTitle: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.getRandomNoun(seed + "pageTitle".hashCode(), it) })

    val pageDescription: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.bookCite(seed + "pageDescription".hashCode(), 128, it) })

    open fun hasOptions(): Boolean {
        return false
    }

    companion object {
        fun generateId(seed: Long): String {
            return "comdagen-${Math.abs(seed)}"
        }
    }

}

/**
 * Represents a single standard product.
 *
 * @property extraAttributes custom attribute configurations
 * @property customAttributes custom attributes of this product
 */
data class StandardProduct(
    override val seed: Long,
    override val regions: List<SupportedZone>,
    val sharedOptions: List<ProductOption>, val localOptions: List<ProductOption>,
    private val extraAttributes: Set<AttributeDefinition>
) : Product() {
    val customAttributes
        get() = extraAttributes.map { CustomAttribute(it, seed + it.id.hashCode()) }

    override fun hasOptions() = sharedOptions.isNotEmpty() || localOptions.isNotEmpty()
}

/**
 * Represents a single variation master product that owns multiple variants.
 *
 * @param config variation master product configuration
 * @param sharedVariationAttributes variation attributes shared by catalog, will be reduced to the actually selected
 *                                  attributes, given by [VariationProductConfiguration.sharedVariationAttributes]
 *
 * @property variants list of product variants
 * @property localVariationAttributes variation attributes defined by master product
 */
data class MasterProduct(
    override val seed: Long,
    override val regions: List<SupportedZone>,
    private val config: VariationProductConfiguration
) : Product() {

    val variants: List<VariationProduct>
        get() = generateVariants()

    val localVariationAttributes
        get() = config.localVariationAttributes.map { VariationAttribute(it.name, it.values) }

    val sharedVariationAttributes
        get() = (config.attributes - config.localVariationAttributes).map { VariationAttribute(it.name, it.values) }


    private fun generateVariants(): List<VariationProduct> {
        val rng = Random(seed + "variants".hashCode())

        // the filter() gets rid of all values that don't meet the probability bar _for this_ master
        val variationAttributes = config.attributes.map { attrDef ->

            VariationAttribute(attrDef.name, attrDef.values.filter({ rng.nextFloat() <= attrDef.probability }))
        }

        // generate variant products for all possible variation attribute value combinations
        // first connect each id to each value (we need this to construct CustomAttribute later)
        // then build a Set (so we can leverage google's solution) of each value list (so one set per attribute definition)
        // the output is a list of custom attribute definitions for each required variant
        val idToValueMap =
            variationAttributes.map { attrDef -> attrDef.dataStore.map { attrVal -> attrDef.id to attrVal }.toSet() }
        return Sets.cartesianProduct(idToValueMap)
            .map { VariationProduct(rng.nextLong(), regions, it.map { (k, v) -> getCustomAttribute(k, v) }) }
    }

    private fun getCustomAttribute(name: String, value: String) =
        CustomAttribute(
            "product.$name", AttributeConfig(
                AttributeConfig.DataType.STRING,
                false, AttributeConfig.GenerationStrategy.STATIC, value
            ), 0
        )
}

/**
 * Represents a single variation of a variation master product
 *
 * @property customAttributes List of variation attributes and there values
 */
data class VariationProduct(
    override val seed: Long, override val regions: List<SupportedZone>,
    val customAttributes: List<CustomAttribute>
) : Product()

/**
 * Represents a single product bundle
 *
 * @param catalog where to get bundled products from
 *
 * @property bundledProducts Map with standard products as key and there quantity as value
 */
data class BundleProduct(
    override val seed: Long, override val regions: List<SupportedZone>,
    private val config: BundleProductConfiguration,
    private val catalog: MasterCatalog
) : Product() {
    val bundledProducts: Map<Product, Int>
        get() {
            val rng = Random(seed)

            val productList = catalog.getAllProducts()

            val elementCount = if (config.maxBundledProducts > config.minBundledProducts)
                rng.nextInt(config.maxBundledProducts - config.minBundledProducts) + config.minBundledProducts
            else config.minBundledProducts


            val startIdx = rng.nextInt(productList.size - elementCount)

            val bundledProductMap: MutableMap<Product, Int> = mutableMapOf()

            if (startIdx >= 0) {
                val products = productList.subList(startIdx, startIdx + elementCount)
                products.forEach { product ->
                    val quantity = if (config.maxQuantity > config.minQuantity)
                        rng.nextInt(config.maxQuantity - config.minQuantity) + config.minQuantity
                    else config.minQuantity

                    bundledProductMap.put(product, quantity)
                }
            }
            return bundledProductMap
        }
}

data class ProductSet(
    override val seed: Long, override val regions: List<SupportedZone>,
    private val config: ProductSetConfiguration,
    private val catalog: MasterCatalog
) : Product() {
    val products: List<Product>
        get() {
            val rng = Random(seed)

            val productList = catalog.getAllProducts()
            val elementCount = if (config.maxSetProducts > config.minSetProducts)
                rng.nextInt(config.maxSetProducts - config.minSetProducts) + config.minSetProducts
            else
                config.minSetProducts

            val startIdx = rng.nextInt(productList.size - elementCount)

            return productList.subList(startIdx, startIdx + elementCount)
        }
}

/**
 * Represents a shared or local product option
 */
data class ProductOption(
    private val seed: Long, private val config: ProductOptionConfiguration,
    private val currencies: List<SupportedCurrency>
) : AttributeDefinition {
    override val id: String
        get() = "comdagen-option-${Math.abs(seed)}"

    override val path: String
        get() = "product.$id"

    override val searchable: Boolean
        get() = false

    override val displayName: String
        get() = RandomData.getRandomNoun(seed + "optionName".hashCode())

    override val type: AttributeConfig.DataType
        get() = AttributeConfig.DataType.STRING

    override val generationStrategy: AttributeConfig.GenerationStrategy
        get() = AttributeConfig.GenerationStrategy.LIST

    override val dataStore: List<OptionValue>
        get() {
            val rng = Random(seed)
            val n = if (config.maxValues > config.minValues)
                rng.nextInt(config.maxValues - config.minValues) + config.minValues
            else config.minValues


            return (1..n).map {
                if (it == 1) {
                    OptionValue(rng.nextLong(), config, currencies, true)
                } else {
                    OptionValue(rng.nextLong(), config, currencies)
                }
            }
        }

    val values: List<OptionValue>
        get() = dataStore // alias for historic reasons

    companion object {
        fun generateProductOptions(config: ProductOptionConfiguration?, currencies: List<SupportedCurrency>, seed: Long)
                : List<ProductOption> {
            if (config == null) {
                return emptyList()
            }

            val rng = Random(seed)
            return (1..config.elementCount).map { ProductOption(rng.nextLong(), config, currencies) }
        }
    }
}

/**
 * Represents a value of a product option
 */
class OptionValue(
    private val seed: Long, private val config: ProductOptionConfiguration,
    private val currencies: List<SupportedCurrency>, val default: Boolean = false
) {
    val id: String
        get() = "comdagen-option-value-${Math.abs(seed)}"

    val displayName: String
        get() = RandomData.getRandomNoun(seed + "optionValueName".hashCode())

    val prices: Map<String, Double>
        get() {
            return currencies.associateBy({ it.toString() }, { getPrice(it) })
        }

    private fun getPrice(currency: SupportedCurrency): Double {
        val rng = Random(seed)
        val exchangeRate = CatalogGenerator.EXCHANGE_RATES.getProperty(currency.toString()).toDouble()
        return config.minPrice + (config.maxPrice - config.minPrice) * rng.nextDouble() * exchangeRate
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OptionValue

        if (seed != other.seed) return false
        if (config != other.config) return false
        if (currencies != other.currencies) return false

        return true
    }

    override fun hashCode(): Int {
        var result = seed.hashCode()
        result = 31 * result + config.hashCode()
        result = 31 * result + currencies.hashCode()
        return result
    }
}