package com.salesforce.comdagen

import com.salesforce.comdagen.config.*
import com.salesforce.comdagen.generator.*
import com.salesforce.comdagen.model.AttributeDefinition
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.xmlunit.validation.Languages
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator
import kotlin.test.assertTrue


/**
 * Will do an integration test to ensure produced xml files are actually syntactically valid.
 */
class SchemaVerificationTest {

    companion object {
        private val seed: Long = 1234
    }

    @Rule
    @JvmField
    val inputDir = TemporaryFolder()

    @Rule
    @JvmField
    val outputDir = TemporaryFolder()

    @Test
    fun testCustomerValid() {
        val customerConfig = CustomerConfiguration(initialSeed = seed)
        val customerGenerator = CustomerGenerator(customerConfig, listOf(SupportedZone.Chinese))

        val v = getValidator("/schema/customerlist2.xsd")

        val templateFileName = "customers.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)
        producer.render(customerGenerator)
        assertTrue {
            try {
                v.validate(
                    StreamSource(
                        File(
                            outputDir.root,
                            "${customerConfig.outputDir}/${customerConfig.getFileName()}"
                        )
                    )
                )
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testPricebookValid() {
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val pricebookConfig = PricebookConfiguration(id = "pricebook", initialSeed = seed)
        val pricebookGenerator = PricebookGenerator(pricebookConfig, catalogConfig, listOf(SupportedCurrency.USD))

        val v = getValidator("/schema/pricebook.xsd")

        val templateFileName = "pricebooks.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)
        producer.render(pricebookGenerator)

        assertTrue {
            try {
                v.validate(
                    StreamSource(
                        File(
                            outputDir.root,
                            "${pricebookConfig.outputDir}/${pricebookConfig.getFileName()}"
                        )
                    )
                )
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testCatalogValid() {
        val catalogConfig = CatalogListConfiguration(elementCount = 1, initialSeed = seed)
        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)

        val catalogId = catalogGenerator.objects.first().id

        val v = getValidator("/schema/catalog.xsd")

        val templateFileName = "catalogs.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)
        producer.render(catalogGenerator)

        assertTrue {
            try {
                v.validate(
                    StreamSource(
                        File(
                            outputDir.root,
                            "${catalogConfig.outputDir}/$catalogId/${catalogConfig.outputFilePattern}"
                        )
                    )
                )
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testInventoryValid() {
        val catalogConfig = CatalogListConfiguration(initialSeed = seed)
        val inventoryConfig = InventoryConfiguration(
            inventoryRecords = InventoryRecordConfiguration(initialSeed = seed),
            initialSeed = seed
        )
        val inventoryGenerator = InventoryGenerator(inventoryConfig, catalogConfig)

        val v = getValidator("/schema/inventory.xsd")

        val templateFileName = "inventories.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)
        producer.render(inventoryGenerator)

        assertTrue {
            try {
                v.validate(
                    StreamSource(
                        File(
                            outputDir.root,
                            "${inventoryConfig.outputDir}/${inventoryConfig.getFileName()}"
                        )
                    )
                )
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testCouponValid() {
        val couponConfig = CouponConfiguration(initialSeed = seed)
        val couponGenerator = CouponGenerator(couponConfig)

        val v = getValidator("/schema/coupon.xsd")

        val templateFileName = "coupons.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)
        producer.render(couponGenerator)

        assertTrue {
            try {
                v.validate(
                    StreamSource(
                        File(
                            outputDir.root,
                            "${couponConfig.outputDir}/${couponConfig.outputFilePattern}"
                        )
                    )
                )
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testCustomerGroupValid() {
        val customerConfig = CustomerConfiguration(initialSeed = seed)
        val customerGroupConfig = CustomerGroupConfiguration(initialSeed = seed)
        val customerGroupGenerator = CustomerGroupGenerator(customerGroupConfig, customerConfig, sourceCodes = Collections.emptyList<String>())

        val v = getValidator("/schema/customergroup.xsd")

        val templateFileName = "customer-groups.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)
        producer.render(customerGroupGenerator)

        assertTrue {
            try {
                v.validate(
                    StreamSource(
                        File(
                            outputDir.root,
                            "${customerGroupConfig.outputDir}/${customerGroupConfig.outputFilePattern}"
                        )
                    )
                )
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testMetaValid() {
        val regions = listOf(SupportedZone.Generic, SupportedZone.Chinese)

        val catalogConfig =
            CatalogListConfiguration(generatedAttributes = GeneratedAttributeConfig(20), initialSeed = seed)
        val pricebookConfig = PricebookConfiguration(
            id = "pricebook",
            generatedAttributes = GeneratedAttributeConfig(20),
            initialSeed = seed
        )
        val customerConfig =
            CustomerConfiguration(generatedAttributes = GeneratedAttributeConfig(30), initialSeed = seed)

        val catalogGenerator = CatalogGenerator(configuration = catalogConfig)
        val pricebookGenerator = PricebookGenerator(
            configuration = pricebookConfig,
            catalogConfiguration = catalogConfig, currencies = listOf(SupportedCurrency.USD)
        )
        val customerGenerator = CustomerGenerator(configuration = customerConfig, regions = regions)

        val v = getValidator("/schema/metadata.xsd")
        val searchSchema = getValidator("/schema/search2.xsd")

        val templateFileName = "system-objecttype-extensions.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
        Files.copy(
            javaClass.getResourceAsStream("/templates/search2.ftlx"), inputDir.newFile("search2.ftlx").toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)

        val customAttributes: Map<String, Set<AttributeDefinition>> =
            catalogGenerator.metadata + pricebookGenerator.metadata + customerGenerator.metadata

        producer.renderMeta(customAttributes = customAttributes, regions = regions.toSet())

        assertTrue {
            try {
                v.validate(StreamSource(File(outputDir.root, "meta/system-objecttype-extensions.xml")))
                searchSchema.validate(StreamSource(File(outputDir.root, "search2.xml")))
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testPromotionsValid() {
        val catalog = CatalogGenerator(CatalogListConfiguration(initialSeed = seed)).objects.first()
        val customerGroupIds = CustomerGroupGenerator(
            CustomerGroupConfiguration(initialSeed = seed),
            CustomerConfiguration(initialSeed = seed), sourceCodes = Collections.emptyList<String>()
        ).objects.map { it.id }.toList()
        val shippingMethods =
            ShippingGenerator(ShippingConfiguration(initialSeed = seed)).objects.map { it.id }.toList()

        val promotionConfig = PromotionConfiguration(
            campaigns = CampaignConfiguration(initialSeed = seed),
            productConfig = ProductPromotionConfiguration(initialSeed = seed),
            orderConfig = OrderPromotionConfiguration(initialSeed = seed),
            shippingConfig = ShippingPromotionConfiguration(initialSeed = seed),
            initialSeed = seed
        )
        val promotionGenerator = PromotionGenerator(
            configuration = promotionConfig, catalog = catalog,
            customerGroups = customerGroupIds, shippingMethods = shippingMethods
        )

        val v = getValidator("/schema/promotion.xsd")

        val templateFileName = "promotions.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)

        producer.render(promotionGenerator)

        assertTrue {
            try {
                v.validate(
                    StreamSource(
                        File(
                            outputDir.root,
                            "${promotionConfig.outputDir}/${promotionConfig.outputFilePattern}"
                        )
                    )
                )
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testShippingMethodsValid() {
        val shippingConfig = ShippingConfiguration(initialSeed = seed)
        val shippingGenerator = ShippingGenerator(shippingConfig)

        val v = getValidator("/schema/shipping.xsd")

        val templateFileName = "shipping.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)

        producer.render(shippingGenerator)

        assertTrue {
            try {
                v.validate(StreamSource(File(outputDir.root, shippingConfig.outputFilePattern)))
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testStoresValid() {
        val storeConfig = StoreConfiguration(initialSeed = seed)
        val storeGenerator = StoreGenerator(storeConfig)

        val v = getValidator("/schema/store.xsd")

        val templateFileName = "stores.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)

        producer.render(storeGenerator)

        assertTrue {
            try {
                v.validate(StreamSource(File(outputDir.root, storeConfig.outputFilePattern)))
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testSortingRulesValid() {
        val sortingRuleConfig = SortingRuleConfiguration(initialSeed = seed)
        val sortingRuleGenerator = SortingRuleGenerator(sortingRuleConfig)

        val v = getValidator("/schema/sort.xsd")

        val templateFileName = "sortingrules.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)

        producer.render(sortingRuleGenerator)

        assertTrue {
            try {
                v.validate(StreamSource(File(outputDir.root, sortingRuleConfig.outputFilePattern)))
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    @Test
    fun testRedirectUrlsValid() {
        val catalogGenerator = CatalogGenerator(CatalogListConfiguration(initialSeed = seed))

        val categories = catalogGenerator.objects.toList().flatMap { it.categories }
        val categoryAssignments = catalogGenerator.objects.flatMap { it.categoryAssignments }

        val redirectUrlConfig = RedirectUrlConfiguration(initialSeed = seed)
        val redirectUrlGenerator = RedirectUrlGenerator(redirectUrlConfig, categories, categoryAssignments)

        val v = getValidator("/schema/redirecturl.xsd")

        val templateFileName = "redirect-urls.ftlx"
        val template = inputDir.newFile(templateFileName)
        Files.copy(
            javaClass.getResourceAsStream("/templates/$templateFileName"), template.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )

        val producer = XMLOutputProducer(inputDir.root, outputDir.root)

        producer.render(redirectUrlGenerator)

        assertTrue {
            try {
                v.validate(StreamSource(File(outputDir.root, redirectUrlConfig.outputFilePattern)))
                return@assertTrue true
            } catch (e: Exception) {
                e.printStackTrace()
                return@assertTrue false
            }
        }
    }

    private fun getValidator(path: String): Validator {
        val factory = SchemaFactory.newInstance(Languages.W3C_XML_SCHEMA_NS_URI)
        factory.resourceResolver = ResourceResolver()

        val schema = factory.newSchema(StreamSource(javaClass.getResourceAsStream(path)))
        return schema.newValidator()
    }
}
