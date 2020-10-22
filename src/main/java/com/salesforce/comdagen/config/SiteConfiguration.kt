/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.salesforce.comdagen.RenderConfig
import com.salesforce.comdagen.SupportedCurrency
import com.salesforce.comdagen.SupportedZone

/**
 * Configure generation of one site.
 *
 * The biggest part are configuration file references (everything with a `*Config` name). They must reference existing
 * files that will be included and parsed as YAML files.
 */
data class SiteConfiguration(
    /**
     * Optional site name, if null site name is "Site ${index}"
     */
    val siteName: String?,

    val siteDescription: String? = null,

    override val initialSeed: Long = 1234,

    val regions: List<SupportedZone> = listOf(SupportedZone.Generic),

    val currencies: List<SupportedCurrency> = listOf(SupportedCurrency.USD),

    @JsonProperty("navigationCatalog")
    val navigationCatalogConfig: NavigationCatalogConfiguration? = null,

    val customCartridges: List<String> = listOf("sitegenesis_storefront_pipelines", "sitegenesis_storefront_core"),

    val staticFiles: List<String>? = null,

    // default for config files is to not generate anything
    val pricebookConfig: String? = null,

    val catalogConfig: String? = null,

    val customerConfig: String? = null,

    val couponConfig: String? = null,

    val inventoryConfig: String? = null,

    val customerGroupConfig: String? = null,

    val promotionConfig: String? = null,

    val productlistConfig: String? = null,

    val shippingConfig: String? = null,

    val sourceCodeConfig: String? = null,

    val storeConfig: String? = null,

    val sortingRuleConfig: String? = null,

    val redirectUrlConfig: String? = null,

    override val outputFilePattern: String = "site.xml",

    override val outputDir: String = "sites",

    override val elementCount: Int = 1, // single site only ever has elementCount=1

    override val templateName: String = "site.ftlx"
) : RenderConfig
