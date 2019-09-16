/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.SupportedCurrency
import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.CatalogConfiguration
import com.salesforce.comdagen.config.CatalogListConfiguration
import com.salesforce.comdagen.config.NavigationCatalogConfiguration
import java.lang.Math.pow
import java.util.*
import kotlin.collections.ArrayList

/**
 * Represents a single catalog category.

 * @property catalogIndex index of the catalog which owns the category
 * @property categoryIndex index of the category in the catalog
 */
data class Category(
    val parent: Category?, private val seed: Long, val config: CatalogConfiguration,
    private val catalogIndex: Int, private val categoryIndex: Int, val template: String?,
    private val regions: List<SupportedZone>
) {
    val id: String
        get() = "${name.values.first()}_L${catalogIndex}_$categoryIndex"

    val name: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.getRandomNoun(seed + "name".hashCode(), it) })

    val description: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.getRandomSentence(seed + "description".hashCode(), it) })

    val pageTitle: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.getRandomNoun(seed + "pageTitle".hashCode(), it) })

    val pageDescription: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.getRandomSentence(seed + "pageDescription".hashCode(), it) })

    val customAttributes: List<CustomAttribute>
        get() = config.attributeDefinitions().map { CustomAttribute(it, seed + "customAttribute".hashCode()) }
}

/**
 * Represents an assignment of a product to a category.
 */
data class CategoryAssignment(val product: Product, val category: Category)


abstract class Catalog(
    protected val seed: Long, private val config: CatalogConfiguration, private val catalogIndex: Int,
    protected val regions: List<SupportedZone>
) {

    open val id: String
        get() = this.javaClass.simpleName + "_" + Math.abs(seed + "catalogId".hashCode())

    val name: Map<SupportedZone, String>
        get() = regions.associateBy({ it }, { RandomData.getRandomNoun(seed + "name".hashCode(), it) })

    val categories: List<Category>
        get() {
            // constructs a category forest
            // the root of each tree get's assigned to Commerce Cloud's 'root' category in the 'catalogs.ftlx' template

            val categoryConfig = config.categoryConfig
            val rng = Random(seed + "categoryTree".hashCode())
            var categoryIndex = 0

            val categories = ArrayList<Category>(categoryConfig.elementCount)

            var treeDepth = categoryConfig.categoryTreeDepth
            var treeBreadth = categoryConfig.categoryTreeBreadth

            // check if category tree is big enough for elementCount, if not increment the tree size
            var i = 0
            while (categoryConfig.elementCount > treeSize(treeDepth, treeBreadth)) {
                if (i == 0) {
                    treeDepth += 1
                    i = 1
                } else if (i == 1) {
                    treeBreadth += 1
                    i = 0
                }
            }

            // this assumes a uniform category distribution across breadth-depth. Future versions might make this more
            // random
            var depth = 1
            while (depth <= treeDepth && categories.size < categoryConfig.elementCount) {
                var breadth = 0
                while (breadth < pow(
                        treeBreadth.toDouble(),
                        depth.toDouble()
                    ) && categories.size < categoryConfig.elementCount
                ) {
                    // parent of a perfect k-ary tree is at floor((i-1)/k) and the list size before node insertion gives
                    // (i-1).
                    // java integer division happily gives us floor() for free
                    val parentIdx = (categories.size / treeBreadth) - 1

                    // first category level should have 'root' as parent
                    categories.add(
                        Category(
                            if (parentIdx == -1) null else categories[parentIdx], rng.nextLong(), config,
                            catalogIndex, categoryIndex, categoryConfig.categoryTemplate, regions
                        )
                    )

                    categoryIndex++
                    breadth++
                }
                depth++
            }
            return categories
        }

    abstract val categoryAssignments: Sequence<CategoryAssignment>

    companion object {
        fun constructCategoryAssignments(
            products: Sequence<Product>, categories: List<Category>, seed: Long,
            coverage: Float = 1.0F, productSetCoverage: Float = 1.0F
        ): Sequence<CategoryAssignment> {

            val assignmentRNG = Random(seed + "categoryAssignments".hashCode())
            val productsetRNG = Random(seed + "productSetAssignments".hashCode())
            val coverageRng = Random(seed + "assignmentCoverage".hashCode())

            return products.map productmapping@{ product ->
                // only assign the coverage percentage of the product to the categories
                if (coverageRng.nextFloat() < (1.0F - coverage)) {
                    return@productmapping null
                }

                val category = categories[assignmentRNG.nextInt(categories.size)]

                when (product) {
                    // TODO: Allow variations to be included without master or only the master
                    // assign master and variation products to the same category
                    is MasterProduct -> product.variants.map { variantProduct ->
                        CategoryAssignment(
                            variantProduct,
                            category
                        )
                    }.asSequence()
                        .plusElement(CategoryAssignment(product, category))
                    is ProductSet -> {
                        // don't assign empty productsets to the navigationcatalog. This would lead to 404 pages
                        if (product.products.count() < 1) {
                            return@productmapping null
                        }
                        product.products.map { productSetItem ->
                            // determine included productsetitems
                            if (productsetRNG.nextFloat() < (1.0F - productSetCoverage)) {
                                return@map null
                            } else {
                                CategoryAssignment(productSetItem, category)
                            }
                        }.filterNotNull().apply {
                            /* Each productset needs to contain one assigned productsetitem so as
                             * a workaround add the first productsetitem. */
                            if (this.count() < 1) {
                                this.plus(CategoryAssignment(product.products[0], category))
                            }
                        }.asSequence().plusElement(CategoryAssignment(product, category))
                    }
                    else -> sequenceOf(CategoryAssignment(product, category))
                }
            }.filterNotNull().flatten()
        }

        /**
         * Calculate the maximum number of nodes in a k-ary tree
         */
        private fun treeSize(treeDepth: Int, treeBreadth: Int) =
            ((pow(treeBreadth.toDouble(), treeDepth.toDouble()) - 1) / (treeBreadth - 1)).toInt()
    }
}

/**
 * Represents a single catalog.
 */
class MasterCatalog(
    seed: Long, val config: CatalogListConfiguration, catalogIndex: Int,
    private val currencies: List<SupportedCurrency>, regions: List<SupportedZone>
) : Catalog(seed, config, catalogIndex, regions) {

    val generatedProductAttributes = config.products.attributeDefinitions()

    val products: Sequence<StandardProduct>
        get() {
            val rng = Random(seed)

            val seeds = (1..config.products.elementCount).map {
                Pair(/* shared options */ (if (config.sharedOptions != null) {
                    rng.nextFloat() to rng.nextInt(sharedOptions.size + 1)
                } else {
                    0.0f to 0
                }),
                    /* local options */ if (config.products.options != null) {
                        rng.nextFloat() to rng.nextLong()
                    } else {
                        0.0f to 0L
                    }
                )
            } // materialize so we don't re-use RNG state

            return seeds.mapIndexed { idx, (shared, local) ->
                val sharedOptions =
                    if (config.sharedOptions != null && shared.first < config.sharedOptions.probability) {
                        sharedOptions.subList(0, shared.second)
                    } else {
                        emptyList()
                    }
                val localOptions =
                    if (config.products.options != null && local.first < config.products.options.probability) {
                        ProductOption.generateProductOptions(config.products.options, currencies, local.second)
                    } else {
                        emptyList()
                    }

                StandardProduct(
                    seed + "product${idx + 1}".hashCode(), regions, sharedOptions,
                    localOptions, generatedProductAttributes
                )
            }.asSequence()
        }

    val masterProducts: Sequence<MasterProduct>
        get() = config.variationProducts.asSequence().flatMap { variationConfig ->
            (1..variationConfig.elementCount).asSequence().map {
                MasterProduct(seed + "master$it".hashCode() + variationConfig.hashCode(), regions, variationConfig)
            }
        }

    val bundles: List<BundleProduct>
        get() {
            if (config.bundleConfig == null) {
                return emptyList()
            }

            return (1..config.bundleConfig.elementCount).map {
                BundleProduct(seed * "bundleProduct$it".hashCode(), regions, config.bundleConfig, this)
            }
        }

    val productSets: List<ProductSet>
        get() {
            if (config.productSets == null) {
                return emptyList()
            }

            return (1..config.productSets.elementCount).map {
                ProductSet(seed * "productSet$it".hashCode(), regions, config.productSets, this)
            }
        }

    val sharedOptions: List<ProductOption>
        get() = ProductOption.generateProductOptions(
            config.sharedOptions,
            currencies,
            seed + "sharedOptions".hashCode()
        )

    val sharedVariationAttributes: List<AttributeDefinition>
        get() = config.sharedVariationAttributes.map { VariationAttribute(it.name, it.values) }

    override val categoryAssignments: Sequence<CategoryAssignment>
        get() = Catalog.constructCategoryAssignments(
            products.plus(masterProducts).plus(bundles).plus(productSets),
            categories, seed + "categoryAssignments".hashCode()
        )

    /**
     * All individual SKUs customers would buy off a catalog.
     * This does _not_ include bundles and sets, because they are packages of SKUs.
     */
    fun getAllProducts(): List<Product> {
        return products.plus(masterProducts).plus(masterProducts.flatMap { it.variants.asSequence() }).toList()
    }

}

class NavigationCatalog(
    seed: Long, private val config: NavigationCatalogConfiguration, catalogIndex: Int,
    private val masterCatalogs: Sequence<MasterCatalog>, private val siteName: String,
    regions: List<SupportedZone> = listOf(SupportedZone.Generic)
) : Catalog(seed, config, catalogIndex, regions) {

    override val id: String
        get() = this.javaClass.simpleName + "_" + Math.abs(seed + siteName.hashCode())

    override val categoryAssignments: Sequence<CategoryAssignment>
        get() {
            val products = masterCatalogs.flatMap {
                it.products.plus(it.masterProducts).plus(it.bundles).plus(it.productSets)
            }

            return Catalog.constructCategoryAssignments(
                products,
                categories,
                seed + "categoryAssignments".hashCode(),
                config.coverage, config.productSetCoverage
            )
        }

    val assignedProducts: Sequence<Product>
        get() = categoryAssignments.map { it.product }
}
