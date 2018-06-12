/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.Comdagen
import com.salesforce.comdagen.Configuration
import com.salesforce.comdagen.SeedInheritance
import com.salesforce.comdagen.config.*
import com.salesforce.comdagen.generator.*
import java.io.File

class Site(val id: Int, private val seed: Long, private val defaults: SiteConfiguration?, private val config: SiteConfiguration, private val configDir: File) {
    val currencies
        get() = config.currencies

    val regions
        get() = config.regions

    val name: String
        get() = config.siteName ?: "Site $id"

    val description: String?
        get() = config.siteDescription ?: defaults?.siteDescription

    val customCartridges: List<String>
        get() = config.customCartridges

    val staticFiles: List<String>
        get() = config.staticFiles ?: defaults?.staticFiles ?: emptyList()

    // TODO does every site _has_ to have a navigation catalog?
    // right now, we force every site to have one either inheriting it via defaults or specifying it explicitly
    val navigationCatalog: NavigationCatalog
        get() = NavigationCatalog(seed + "navigationCatalog".hashCode(), config.navigationCatalogConfig
                ?: defaults!!.navigationCatalogConfig!!, 0,
                catalogGenerator?.objects ?: emptySequence(), name, regions)

    // same as the navigation catalog - if you don't have it explicitly, we need to force it in the defaults
    val navigationCatalogTemplateName: String
        get() = config.navigationCatalogConfig?.templateName ?: defaults!!.navigationCatalogConfig!!.templateName


    // Generator configurations

    private val pricebookConfig: PricebookConfiguration? = loadConfig(config.pricebookConfig
            ?: defaults?.pricebookConfig)

    private val catalogConfig: CatalogListConfiguration? = loadConfig(config.catalogConfig ?: defaults?.catalogConfig)

    private val customerConfig: CustomerConfiguration? = loadConfig(config.customerConfig ?: defaults?.customerConfig)

    private val couponConfig: CouponConfiguration? = loadConfig(config.couponConfig ?: defaults?.couponConfig)

    private val inventoryConfig: InventoryConfiguration? = loadConfig(config.inventoryConfig
            ?: defaults?.inventoryConfig)

    private val customerGroupConfig: CustomerGroupConfiguration? = loadConfig(config.customerGroupConfig
            ?: defaults?.customerGroupConfig)

    private val promotionConfig: PromotionConfiguration? = loadConfig(config.promotionConfig
            ?: defaults?.promotionConfig)

    private val shippingConfig: ShippingConfiguration? = loadConfig(config.shippingConfig ?: defaults?.shippingConfig)

    private val sourceCodeConfig: SourceCodeConfiguration? = loadConfig(config.sourceCodeConfig
            ?: defaults?.sourceCodeConfig)

    private val storeConfig: StoreConfiguration? = loadConfig(config.storeConfig ?: defaults?.storeConfig)

    private val sortingRuleConfig: SortingRuleConfiguration? = loadConfig(config.sortingRuleConfig
            ?: defaults?.sortingRuleConfig)

    private val redirectUrlConfig: RedirectUrlConfiguration? = loadConfig(config.redirectUrlConfig
            ?: defaults?.redirectUrlConfig)


    // Generators

    val pricebookGenerator: PricebookGenerator? =
            if (pricebookConfig != null && catalogConfig != null)
                PricebookGenerator(pricebookConfig, catalogConfig, currencies)
            else
                null

    val customerGenerator: CustomerGenerator? =
            if (customerConfig != null)
                CustomerGenerator(customerConfig, regions)
            else
                null

    val catalogGenerator: CatalogGenerator? =
            if (catalogConfig != null)
                CatalogGenerator(catalogConfig, currencies, regions)
            else
                null

    val couponGenerator: CouponGenerator? =
            if (couponConfig != null)
                CouponGenerator(couponConfig)
            else
                null

    val inventoryGenerator: InventoryGenerator? =
            if (inventoryConfig != null && catalogConfig != null)
                InventoryGenerator(inventoryConfig, catalogConfig)
            else
                null

    val customerGroupGenerator: CustomerGroupGenerator? =
            if (customerGroupConfig != null && customerConfig != null)
                CustomerGroupGenerator(customerGroupConfig, customerConfig)
            else
                null

    val shippingGenerator: ShippingGenerator? =
            if (shippingConfig != null)
                ShippingGenerator(shippingConfig)
            else
                null

    val sourceCodeGenerator: SourceCodeGenerator? =
            if (sourceCodeConfig != null)
                SourceCodeGenerator(sourceCodeConfig)
            else
                null

    val promotionGenerator: PromotionGenerator? = if (promotionConfig != null && catalogGenerator != null)
        PromotionGenerator(promotionConfig, catalogGenerator.objects.first(),
                customerGroups = customerGroupGenerator?.objects?.map { it.id }?.toList() ?: emptyList(),
                couponIds = couponGenerator?.objects?.toList()?.map { it.id } ?: emptyList(),
                shippingMethods = shippingGenerator?.objects?.toList()?.map { it.id },
                sourceCodeIds = sourceCodeGenerator?.objects?.toList()?.map { it.id } ?: emptyList()) else null

    val storeGenerator: StoreGenerator? = if (storeConfig != null) StoreGenerator(storeConfig) else null

    val sortingRuleGenerator: SortingRuleGenerator? = if (sortingRuleConfig != null) SortingRuleGenerator(sortingRuleConfig) else null

    val redirectUrlGenerator: RedirectUrlGenerator? = if (redirectUrlConfig != null && catalogGenerator != null)
        RedirectUrlGenerator(redirectUrlConfig,
                navigationCatalog.categories,
                navigationCatalog.categoryAssignments) else null

    private inline fun <reified T : Configuration> loadConfig(location: String?): T? {
        if (location == null) {
            return null // no error, user doesn't want to generate this type
        }

        val configFile = if (location.startsWith(File.separator) || location.startsWith(".")) // don't touch "complete" paths
            File(location)
        else
            File(configDir, location)

        return if (configFile.canRead())
            Comdagen.OBJECT_MAPPER.readerFor(T::class.java)
                    // initialize seed with site default
                    .withAttribute(SeedInheritance.siteSeedName, config.initialSeed)
                    .readValue(configFile)
        else null
    }
}
