/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.attributeDefinitions
import com.salesforce.comdagen.config.CampaignConfiguration
import com.salesforce.comdagen.config.OrderPromotionConfiguration
import com.salesforce.comdagen.config.ProductPromotionConfiguration
import com.salesforce.comdagen.config.ShippingPromotionConfiguration
import java.util.*

abstract class Promotion(private val seed: Long) {
    val id: String
        get() = "comdagen-${Math.abs(seed) + "promotionId".hashCode()}"

    val name: String
        get() = RandomData.getRandomNoun(seed + "promotionName".hashCode())

    val description: String
        get() = RandomData.getRandomSentence(seed + "promotionDescription".hashCode())

    abstract val calloutMsg: String

    abstract val customAttributes: List<CustomAttribute>
}

data class ProductPromotion(
    private val seed: Long, private val categoryIds: List<String>, val catalogId: String,
    private val config: ProductPromotionConfiguration
) : Promotion(seed) {
    val discountedCategory: String
        get() = categoryIds[Random(seed).nextInt(categoryIds.size)]

    val discount: Int
        get() = if (config.maxDiscount > config.minDiscount)
            Random(seed).nextInt(config.maxDiscount - config.minDiscount) + config.minDiscount
        else config.minDiscount

    override val calloutMsg: String
        get() = "Get $discount% off!"

    override val customAttributes: List<CustomAttribute>
        get() = config.attributeDefinitions("Promotion").map { CustomAttribute(it, seed) }
}

data class OrderPromotion(
    private val seed: Long, private val categoryIds: List<String>, val catalogId: String,
    private val config: OrderPromotionConfiguration
) : Promotion(seed) {
    val discount: Int
        get() = if (config.maxDiscount > config.minDiscount)
            Random(seed).nextInt(config.maxDiscount - config.minDiscount) + config.minDiscount
        else config.minDiscount

    val threshold: Float
        get() = Random(seed).nextFloat() * (config.maxThreshold - config.minThreshold) + config.minThreshold

    val qualifyingCategory: String
        get() = categoryIds[Random(seed).nextInt(categoryIds.size)]

    override val calloutMsg: String
        get() = "Get $discount% off for your order!"

    override val customAttributes: List<CustomAttribute>
        get() = config.attributeDefinitions("Promotion").map { CustomAttribute(it, seed) }
}

data class ShippingPromotion(
    private val seed: Long, private val categoryIds: List<String>, val catalogId: String,
    private val config: ShippingPromotionConfiguration, private val shippingIds: List<String>
) : Promotion(seed) {
    val qualifyingCategory: String
        get() = categoryIds[Random(seed).nextInt(categoryIds.size)]

    val threshold: Float
        get() = Random(seed).nextFloat() * (config.maxThreshold - config.minThreshold) + config.minThreshold

    val discount: Int
        get() = if (config.maxDiscount > config.minDiscount)
            Random(seed).nextInt(config.maxDiscount - config.minDiscount) + config.minDiscount
        else
            config.minDiscount

    val shippingMethod: String
        get() = shippingIds[Random(seed + "shippingMethod".hashCode()).nextInt(shippingIds.size)]

    override val calloutMsg: String
        get() = "Get $discount% off for shipping!"

    override val customAttributes: List<CustomAttribute>
        get() = config.attributeDefinitions("Promotion").map { CustomAttribute(it, seed) }
}

data class Campaign(
    private val seed: Long, private val attributeDefinitions: Set<AttributeDefinition>,
    private val config: CampaignConfiguration, private val couponIds: List<String> = emptyList(),
    private val customerGroupIds: List<String> = emptyList(), private val sourceCodeIds: List<String> = emptyList()
) {
    val id: String
        get() = "comdagen-${Math.abs(seed) + "campaignId".hashCode()}"

    val name: String
        get() = RandomData.getRandomNoun(seed + "campaignName".hashCode())

    val description: String
        get() = RandomData.getRandomSentence(seed + "campaignDescription".hashCode())

    val coupons: List<String>
        get() {
            val rng = Random(seed)

            val count = if (config.maxCoupons > config.minCoupons)
                rng.nextInt(config.maxCoupons - config.minCoupons) + config.minCoupons
            else config.minCoupons

            if (count >= couponIds.size) {
                return couponIds
            }

            val startIdx = rng.nextInt(couponIds.size - count)

            return couponIds.subList(startIdx, startIdx + count)
        }

    val customerGroups: List<String>
        get() {
            val rng = Random(seed)

            val count = if (config.maxCustomerGroups > config.minCustomerGroups)
                rng.nextInt(config.maxCustomerGroups - config.minCustomerGroups) + config.minCustomerGroups
            else config.minCustomerGroups

            if (count >= customerGroupIds.size) {
                return customerGroupIds
            }

            val startIdx = rng.nextInt(customerGroupIds.size - count)

            return customerGroupIds.subList(startIdx, startIdx + count)
        }

    val sourceCodes: List<String>
        get() {
            val rng = Random(seed + "campaignSourceCodes".hashCode())

            val count = if (config.maxSourceCodes > config.minSourceCodes)
                rng.nextInt(config.maxSourceCodes - config.minSourceCodes) + config.minSourceCodes
            else config.minSourceCodes

            if (count >= sourceCodeIds.size) {
                return sourceCodeIds
            }

            val startIdx = rng.nextInt(sourceCodeIds.size - count)

            return sourceCodeIds.subList(startIdx, startIdx + count)
        }

    val customAttributes: List<CustomAttribute>
        get() {
            val rng = Random(seed + "customAttributes".hashCode())
            return attributeDefinitions.map { CustomAttribute(it, rng.nextLong()) }
        }
}

data class CampaignPromotionAssignment(val campaign: Campaign, val promotion: Promotion)
