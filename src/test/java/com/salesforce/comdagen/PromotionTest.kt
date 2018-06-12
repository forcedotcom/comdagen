package com.salesforce.comdagen

import com.salesforce.comdagen.config.*
import com.salesforce.comdagen.generator.*
import com.salesforce.comdagen.model.CampaignPromotionAssignment
import com.salesforce.comdagen.model.OrderPromotion
import com.salesforce.comdagen.model.ProductPromotion
import com.salesforce.comdagen.model.ShippingPromotion
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PromotionTest {

    private val seed: Long = 1234
    private val catalog = CatalogGenerator(CatalogListConfiguration(initialSeed = seed)).objects.first()
    private val shippingMethods = ShippingGenerator(ShippingConfiguration(initialSeed = seed)).objects.toList().map { it.id }

    @Test
    fun testPromotionElementCount() {
        val elementCount = 100

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(elementCount = 100, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog)

        assertEquals(elementCount, promotionGenerator.objects.count())
    }

    @Test
    fun testPromotionCampaignAssignments() {
        val minPromotions = 3
        val maxPromotions = 7

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(elementCount = 100, initialSeed = seed),
                campaigns = CampaignConfiguration(minPromotions = minPromotions, maxPromotions = maxPromotions, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog)

        // test if all promotions have one campaign assigned to them
        promotionGenerator.objects.forEach { promotion ->
            assertEquals(1, promotionGenerator.assignments.count { it.promotion == promotion })
        }

        // test if each campaign has the configured amount of promotions assigned
        promotionGenerator.campaigns.forEach { campaign ->
            val campaignAssignments: List<CampaignPromotionAssignment> = promotionGenerator.assignments.filter { it.campaign == campaign }.toList()

            assertTrue(campaignAssignments.size >= minPromotions)
            assertTrue(campaignAssignments.size <= maxPromotions)
        }
    }

    @Test
    fun testProductPromotionElementCount() {
        val elementCount = 150
        val promotionConfiguration = PromotionConfiguration(productConfig = ProductPromotionConfiguration(elementCount = elementCount, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfiguration, catalog)

        assertEquals(elementCount, promotionGenerator.objects.count { it is ProductPromotion })
    }

    @Test
    fun testProductPromotionDiscounts() {
        val minDiscount = 10
        val maxDiscount = 70

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(minDiscount = minDiscount, maxDiscount = maxDiscount, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ProductPromotion) {
                assertTrue(promotion.discount >= minDiscount)
                assertTrue(promotion.discount <= maxDiscount)
            }
        }
    }

    @Test
    fun testProductPromotionDiscountedCategory() {
        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog)

        // test if discounted category is a category of the sites catalog
        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ProductPromotion) {
                assertTrue(catalog.categories.filter { it.id == promotion.discountedCategory }.size == 1)
            }
        }
    }

    @Test
    fun testProductPromotionCustomAttributes() {
        val name = "test-attribute"
        val type = AttributeConfig.DataType.STRING
        val dataStore = "foobar"
        val generationStrategy = AttributeConfig.GenerationStrategy.STATIC

        val attributeConfig = AttributeConfig(type = type, dataStore = dataStore, generationStrategy = generationStrategy, searchable = false)
        val customAttributesConfig: Map<String, AttributeConfig> = mapOf(name to attributeConfig)

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(
                customAttributes = customAttributesConfig, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ProductPromotion) {
                assertEquals(customAttributesConfig.values.size, promotion.customAttributes.size)
                promotion.customAttributes.forEach { attribute ->
                    assertEquals(name, attribute.definition.id)
                    assertEquals(dataStore, attribute.value)
                    assertEquals(type, attribute.definition.type)
                }
            }
        }
    }

    @Test
    fun testProductPromotionGeneratedAttributes() {
        val elementCount = 15

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(
                generatedAttributes = GeneratedAttributeConfig(elementCount), initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ProductPromotion) {
                assertEquals(elementCount, promotion.customAttributes.count())
            }
        }
    }

    @Test
    fun testOrderPromotionElementCount() {
        val elementCount = 100
        val promotionConfig = PromotionConfiguration(orderConfig = OrderPromotionConfiguration(elementCount, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog)

        assertEquals(elementCount, promotionGenerator.objects.count { it is OrderPromotion })
    }

    @Test
    fun testOrderPromotionDiscountRange() {
        val minDiscount = 5
        val maxDiscount = 70
        val promotionConfig = PromotionConfiguration(orderConfig = OrderPromotionConfiguration(minDiscount = minDiscount, maxDiscount = maxDiscount, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is OrderPromotion) {
                assertTrue(promotion.discount >= minDiscount)
                assertTrue(promotion.discount <= maxDiscount)
            }
        }
    }

    @Test
    fun testOrderPromotionThresholdRange() {
        val minThreshold = 1.00F
        val maxThreshold = 100.0F

        val promotionConfig = PromotionConfiguration(orderConfig = OrderPromotionConfiguration(minThreshold = minThreshold, maxThreshold = maxThreshold, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is OrderPromotion) {
                assertTrue(promotion.threshold >= minThreshold)
                assertTrue(promotion.threshold <= maxThreshold)
            }
        }
    }

    @Test
    fun testOrderPromotionQualifyingCategory() {
        val promotionConfig = PromotionConfiguration(orderConfig = OrderPromotionConfiguration(initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog)

        // test if qualifying category of order promotions exist in catalog
        promotionGenerator.objects.forEach { promotion ->
            if (promotion is OrderPromotion) {
                assertTrue(catalog.categories.map { it.id }.contains(promotion.qualifyingCategory))
            }
        }
    }

    @Test
    fun testOrderPromotionCustomAttributes() {
        val name = "test-attribute"
        val type = AttributeConfig.DataType.STRING
        val dataStore = "foobar"
        val generationStrategy = AttributeConfig.GenerationStrategy.STATIC

        val attributeConfig = AttributeConfig(type = type, dataStore = dataStore, generationStrategy = generationStrategy, searchable = false)
        val customAttributesConfig: Map<String, AttributeConfig> = mapOf(name to attributeConfig)

        val promotionConfig = PromotionConfiguration(orderConfig = OrderPromotionConfiguration(
                customAttributes = customAttributesConfig, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is OrderPromotion) {
                assertEquals(customAttributesConfig.values.size, promotion.customAttributes.size)
                promotion.customAttributes.forEach { attribute ->
                    assertEquals(name, attribute.definition.id)
                    assertEquals(dataStore, attribute.value)
                    assertEquals(type, attribute.definition.type)
                }
            }
        }
    }

    @Test
    fun testOrderPromotionGeneratedAttributes() {
        val elementCount = 15

        val promotionConfig = PromotionConfiguration(orderConfig = OrderPromotionConfiguration(
                generatedAttributes = GeneratedAttributeConfig(elementCount), initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is OrderPromotion) {
                assertEquals(elementCount, promotion.customAttributes.count())
            }
        }
    }

    @Test
    fun testShippingPromotionElementCount() {
        val elementCount = 100
        val promotionConfig = PromotionConfiguration(shippingConfig = ShippingPromotionConfiguration(elementCount, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog, shippingMethods = shippingMethods)

        assertEquals(elementCount, promotionGenerator.objects.count { it is ShippingPromotion })
    }

    @Test
    fun testShippingPromotionDiscountRange() {
        val minDiscount = 5
        val maxDiscount = 70
        val promotionConfig = PromotionConfiguration(shippingConfig = ShippingPromotionConfiguration(minDiscount = minDiscount, maxDiscount = maxDiscount, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog, shippingMethods = shippingMethods)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ShippingPromotion) {
                assertTrue(promotion.discount >= minDiscount)
                assertTrue(promotion.discount <= maxDiscount)
            }
        }
    }

    @Test
    fun testShippingPromotionThresholdRange() {
        val minThreshold = 1.00F
        val maxThreshold = 100.0F

        val promotionConfig = PromotionConfiguration(shippingConfig = ShippingPromotionConfiguration(minThreshold = minThreshold, maxThreshold = maxThreshold, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog, shippingMethods = shippingMethods)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ShippingPromotion) {
                assertTrue(promotion.threshold >= minThreshold)
                assertTrue(promotion.threshold <= maxThreshold)
            }
        }
    }

    @Test
    fun testShippingPromotionQualifyingCategory() {
        val promotionConfig = PromotionConfiguration(shippingConfig = ShippingPromotionConfiguration(initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog, shippingMethods = shippingMethods)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ShippingPromotion) {
                assertTrue(catalog.categories.map { it.id }.contains(promotion.qualifyingCategory))
            }
        }
    }

    @Test
    fun testShippingPromotionShippingMethod() {
        val promotionConfig = PromotionConfiguration(shippingConfig = ShippingPromotionConfiguration(initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog, shippingMethods = shippingMethods)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ShippingPromotion) {
                assertTrue(shippingMethods.contains(promotion.shippingMethod))
            }
        }
    }

    @Test
    fun testShippingPromotionCustomAttributes() {
        val name = "test-attribute"
        val type = AttributeConfig.DataType.STRING
        val dataStore = "foobar"
        val generationStrategy = AttributeConfig.GenerationStrategy.STATIC

        val attributeConfig = AttributeConfig(type = type, dataStore = dataStore, generationStrategy = generationStrategy, searchable = false)
        val customAttributesConfig: Map<String, AttributeConfig> = mapOf(name to attributeConfig)

        val promotionConfig = PromotionConfiguration(shippingConfig = ShippingPromotionConfiguration(
                customAttributes = customAttributesConfig, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ShippingPromotion) {
                assertEquals(customAttributesConfig.values.size, promotion.customAttributes.size)
                promotion.customAttributes.forEach { attribute ->
                    assertEquals(name, attribute.definition.id)
                    assertEquals(dataStore, attribute.value)
                    assertEquals(type, attribute.definition.type)
                }
            }
        }
    }

    @Test
    fun testShippingPromotionGeneratedAttributes() {
        val elementCount = 15

        val promotionConfig = PromotionConfiguration(shippingConfig = ShippingPromotionConfiguration(
                generatedAttributes = GeneratedAttributeConfig(elementCount), initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog)

        promotionGenerator.objects.forEach { promotion ->
            if (promotion is ShippingPromotion) {
                assertEquals(elementCount, promotion.customAttributes.count())
            }
        }
    }

    @Test
    fun testCampaignCustomerGroups() {
        val minGroups = 3
        val maxGroups = 7

        val customerConfig = CustomerConfiguration(initialSeed = seed)
        val customerGroupConfig = CustomerGroupConfiguration(initialSeed = seed)
        val customerGroupGenerator = CustomerGroupGenerator(customerGroupConfig, customerConfig)

        val customerGroupIds = customerGroupGenerator.objects.map { it.id }.toList()
                .plus(listOf("Everyone", "Registered", "Unregistered"))

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(initialSeed = seed),
                campaigns = CampaignConfiguration(minCustomerGroups = minGroups, maxCustomerGroups = maxGroups, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog,
                customerGroups = customerGroupIds)

        promotionGenerator.campaigns.forEach { campaign ->
            assertTrue(campaign.customerGroups.size >= minGroups)
            assertTrue(campaign.customerGroups.size <= maxGroups)

            campaign.customerGroups.forEach { customerGroup ->
                assertTrue(customerGroupIds.contains(customerGroup))
            }
        }
    }

    @Test
    fun testCampaignCoupons() {
        val minCoupons = 3
        val maxCoupons = 7

        val couponConfig = CouponConfiguration(initialSeed = seed)
        val couponGenerator = CouponGenerator(couponConfig)

        val couponIds = couponGenerator.objects.map { it.id }.toList()

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(initialSeed = seed),
                campaigns = CampaignConfiguration(minCoupons = minCoupons, maxCoupons = maxCoupons, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog, couponIds = couponIds)

        promotionGenerator.campaigns.forEach { campaign ->
            assertTrue(campaign.coupons.size >= minCoupons)
            assertTrue(campaign.coupons.size <= maxCoupons)

            campaign.coupons.forEach { coupon ->
                assertTrue(couponIds.contains(coupon))
            }
        }
    }

    @Test
    fun testCampaignSourceCodes() {
        val minCodes = 3
        val maxCodes = 7

        val sourceCodeConfig = SourceCodeConfiguration(initialSeed = seed)
        val sourceCodeGenerator = SourceCodeGenerator(sourceCodeConfig)

        val sourceCodeIds = sourceCodeGenerator.objects.map { it.id }.toList()

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(initialSeed = seed),
                campaigns = CampaignConfiguration(minSourceCodes = minCodes, maxSourceCodes = maxCodes, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog, sourceCodeIds = sourceCodeIds)

        promotionGenerator.campaigns.forEach { campaign ->
            assertTrue(campaign.sourceCodes.size >= minCodes)
            assertTrue(campaign.sourceCodes.size <= maxCodes)

            campaign.sourceCodes.forEach { code ->
                assertTrue(sourceCodeIds.contains(code))
            }
        }
    }

    @Test
    fun testCampaignCustomAttributes() {
        val name = "test-attribute"
        val type = AttributeConfig.DataType.STRING
        val dataStore = "foobar"
        val generationStrategy = AttributeConfig.GenerationStrategy.STATIC

        val attributeConfig = AttributeConfig(type = type, dataStore = dataStore, generationStrategy = generationStrategy, searchable = false)
        val customAttributesConfig: Map<String, AttributeConfig> = mapOf(name to attributeConfig)

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(initialSeed = seed),
                campaigns = CampaignConfiguration(customAttributes = customAttributesConfig, initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog = catalog, shippingMethods = shippingMethods)

        promotionGenerator.campaigns.forEach { campaign ->
            assertEquals(customAttributesConfig.values.size, campaign.customAttributes.size)
            campaign.customAttributes.forEach { attribute ->
                assertEquals(name, attribute.definition.id)
                assertEquals(dataStore, attribute.value)
                assertEquals(type, attribute.definition.type)
            }
        }
    }

    @Test
    fun testCampaignGeneratedAttributes() {
        val elementCount = 15

        val promotionConfig = PromotionConfiguration(productConfig = ProductPromotionConfiguration(initialSeed = seed),
                campaigns = CampaignConfiguration(generatedAttributes = GeneratedAttributeConfig(elementCount), initialSeed = seed), initialSeed = seed)
        val promotionGenerator = PromotionGenerator(promotionConfig, catalog, shippingMethods = shippingMethods)

        promotionGenerator.campaigns.forEach { campaign ->
            assertEquals(elementCount, campaign.customAttributes.count())
        }
    }
}
