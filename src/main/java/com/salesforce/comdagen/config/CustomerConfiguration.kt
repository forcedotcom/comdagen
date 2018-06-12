/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonRootName
import com.salesforce.comdagen.ExtendableObjectConfig
import com.salesforce.comdagen.RenderConfig

/**
 * Root configuration for CustomerGenerator.
 */
@JsonRootName("customers")
@JsonIgnoreProperties(ignoreUnknown = true)
data class CustomerConfiguration(
        /**
         * Customer ids will start with this value.
         */
        val startingId: Int = 1,

        val minAddressCount: Int = 0,

        val maxAddressCount: Int = 5,

        override val customAttributes: Map<String, AttributeConfig>? = null,
        override val generatedAttributes: GeneratedAttributeConfig? = null,

        /**
         * The id attribute of the customer list.
         */
        val id: String = "customer-list",

        /**
         * Should customer passwords be stored hashed?
         *
         * If you say `false` here, the password will be plaintext, forcing the server to encrypt each password during
         * the import. This is a very time-intensive operation by design.
         */
        val prehashPasswords: Boolean = true,

        override val elementCount: Int = 5,
        override val initialSeed: Long,
        override val outputFilePattern: String = "customer-list\${i}.xml",
        override val outputDir: String = "customer-lists",
        override val templateName: String = "customers.ftlx"
) : RenderConfig, ExtendableObjectConfig
