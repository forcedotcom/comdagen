/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.PromotionConfiguration
import com.salesforce.comdagen.model.*
import java.util.*

data class PromotionGenerator(override val configuration: PromotionConfiguration,
                              private val catalog: Catalog, private val customerGroups: List<String> = emptyList(),
                              private val couponIds: List<String> = emptyList(), private val shippingMethods: List<String>? = null,
                              private val sourceCodeIds: List<String> = emptyList())
    : Generator<PromotionConfiguration, Promotion> {

    override val objects: Sequence<Promotion>
        get() {
            val categoryIds: List<String> = catalog.categories.map { it.id }

            // generate product promotions
            val productRng = Random(configuration.initialSeed + "productPromotions".hashCode())
            val productPromotions = if (configuration.productConfig != null) (1..configuration.productConfig.elementCount).asSequence().map {
                ProductPromotion(productRng.nextLong(), categoryIds, catalog.id, configuration.productConfig)
            } else emptySequence()

            // generate order promotions
            val orderRng = Random(configuration.initialSeed + "orderPromotions".hashCode())
            val orderPromotions = if (configuration.orderConfig != null) (1..configuration.orderConfig.elementCount).asSequence().map {
                OrderPromotion(orderRng.nextLong(), categoryIds, catalog.id, configuration.orderConfig)
            } else emptySequence()

            val shippingRng = Random(configuration.initialSeed + "shippingPromotions".hashCode())
            val shippingPromotions = if (configuration.shippingConfig != null && shippingMethods != null) (1..configuration.shippingConfig.elementCount).asSequence().map {
                ShippingPromotion(shippingRng.nextLong(), categoryIds, catalog.id, configuration.shippingConfig, shippingMethods)
            } else emptySequence()

            return productPromotions.plus(orderPromotions).plus(shippingPromotions)
        }

    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = mapOf(
                "Promotion" to buildPromotionAttributeDefinitions(),
                "Campaign" to (configuration.campaigns?.attributeDefinitions("Campaign") ?: emptySet())
        )

    val campaigns: Sequence<Campaign>
        get() {
            if (configuration.campaigns == null) {
                return emptySequence()
            }

            val assignmentRng = Random(configuration.initialSeed + "assignments".hashCode())
            val campaignRng = Random(configuration.initialSeed + "campaign".hashCode())

            val promotions: MutableList<Promotion> = objects.toMutableList()
            val campaigns: MutableList<Campaign> = mutableListOf()

            val defaultCustomerGroups = listOf("Everyone", "Registered", "Unregistered")

            while (promotions.isNotEmpty()) {
                val promotionCount = assignmentRng.nextInt(configuration.campaigns.maxPromotions - configuration.campaigns.minPromotions) + configuration.campaigns.minPromotions
                val campaign = Campaign(seed = campaignRng.nextLong(), attributeDefinitions = metadata["Campaign"].orEmpty(), config = configuration.campaigns,
                        customerGroupIds = customerGroups.plus(defaultCustomerGroups), couponIds = couponIds, sourceCodeIds = sourceCodeIds)
                campaigns.add(campaign)
                (1..promotionCount).forEach {
                    if (promotions.isNotEmpty()) {
                        val promotion = promotions[assignmentRng.nextInt(promotions.size)]
                        promotions.remove(promotion)
                    }
                }
            }

            return campaigns.asSequence()
        }

    val assignments: Sequence<CampaignPromotionAssignment>
        get() {
            if (configuration.campaigns == null) {
                return emptySequence()
            }

            val rng = Random(configuration.initialSeed + "assignments".hashCode())

            val promotions: MutableList<Promotion> = objects.toMutableList()
            val campaigns = campaigns
            val assignments = mutableListOf<CampaignPromotionAssignment>()

            campaigns.forEach { campaign ->
                val promotionCount = rng.nextInt(configuration.campaigns.maxPromotions - configuration.campaigns.minPromotions) + configuration.campaigns.minPromotions
                (1..promotionCount).forEach {
                    if (promotions.isNotEmpty()) {
                        val promotion = promotions[rng.nextInt(promotions.size)]
                        promotions.remove(promotion)
                        assignments.add(CampaignPromotionAssignment(campaign, promotion))
                    }
                }
            }

            return assignments.asSequence()
        }

    private fun buildPromotionAttributeDefinitions(): Set<AttributeDefinition> {
        return listOf(configuration.shippingConfig, configuration.productConfig, configuration.orderConfig).map {
            if (it != null)
                it.attributeDefinitions("Promotion")
            else
                emptySet()
        }.flatten().toSet()
    }
}
