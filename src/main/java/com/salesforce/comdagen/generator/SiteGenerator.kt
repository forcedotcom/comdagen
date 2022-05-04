/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.config.SitesConfig
import com.salesforce.comdagen.model.AttributeDefinition
import com.salesforce.comdagen.model.Site
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.streams.toList

/**
 * Generates stream of sites. So this is the top-level generator.
 *
 * @property configDir directory where linked configuration files are stored
 * @property generatorTemplate site configuration happens in this template
 */
data class SiteGenerator(
    override val configuration: SitesConfig,
    private val configDir: File,
    val generateComdagenSummaryContentAsset: Boolean
) :
    Generator<SitesConfig, Site> {

    private val rng: Random
        get() = Random(configuration.initialSeed)

    /**
     * Site preferences/settings rendered in this template.
     *
     * The distinction between site metadata (in [generatorTemplate]) and preferences isn't very clear, but if you don't
     * know, it's a preference.
     */
    val preferencesTemplate: String
        get() = configuration.preferencesTemplate

    override val objects: Sequence<Site> = (
            // pre-specified sites always get constructed
            configuration.sites.asSequence().mapIndexed { index, siteConfiguration ->
                Site(index + 1, siteConfiguration.initialSeed, configuration.defaults, siteConfiguration, configDir)

            } +
                    // for random sites we materialize a series of seeds (even thousands are a tiny memory footprint)
                    // note how defaults is all we got for the _actual_ site configuration, so the defaults _param_ is null
                    rng.longs(
                        max(
                            0,
                            configuration.elementCount - configuration.sites.size
                        ).toLong()
                    ).toList().mapIndexed { index, siteSeed ->
                        Site(index + configuration.sites.size + 1, siteSeed, null, configuration.defaults!!, configDir)
                    })

    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}