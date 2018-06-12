/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.RenderConfig

/**
 * Root configuration for SiteGenerator
 */
@JsonRootName("sitesConfig")
data class SitesConfig(
        /**
         * Configuration properties missing for both user specified sites and generated ones will be read from here.
         */
        val defaults: SiteConfiguration?,

        val sites: List<SiteConfiguration> = listOf(),

        /** This is the email domain for all generated emails. */
        val emailDomain: String = "varmail.net",

        val preferencesTemplate: String = "preferences.ftlx",

        /** Global static files. */
        val staticFiles: List<String>? = null,

        /**
         * Total amount of sites that will be generated. Sites specified under [sites] count against this.
         * If you specify more sites below than in elementCount this number will be silently expanded.
         * If you specify less, we will generate "random" sites to fill the gap.
         */
        override val elementCount: Int = 2,

        override val initialSeed: Long = 1234,

        override val outputFilePattern: String = "generated.xml",
        override val outputDir: String = "sites",
        override val templateName: String = "site.ftlx"
) : RenderConfig {
    init {
        require(elementCount > 0, { "elementCount for sites must be positive integer" })
        RandomData.emailDomain = emailDomain
    }
}
