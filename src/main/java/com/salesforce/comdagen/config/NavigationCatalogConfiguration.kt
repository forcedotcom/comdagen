/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName("navigation-catalog")
data class NavigationCatalogConfiguration(
        val coverage: Float = 1.0F,

        val templateName: String = "catalogs.ftlx",

        @JsonProperty("categories")
        override val categoryConfig: CategoryConfiguration = CategoryConfiguration(),

        override val generatedAttributes: GeneratedAttributeConfig?,

        override val customAttributes: Map<String, AttributeConfig> = emptyMap(),

        /** Not used. See [coverage]. */
        override val elementCount: Int = 0,

        override val initialSeed: Long
) : CatalogConfiguration
