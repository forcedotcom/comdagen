/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.salesforce.comdagen.ExtendableObjectConfig

/**
 * Configuration class for content in libraries.
 */
data class ContentConfiguration(
    override val initialSeed: Long = 1234,
    val contentId: String?,
    val importModeDelete: Boolean?,
    val onlineFlag: Boolean = true,
    val searchableFlag: Boolean = true,
    val classificationFolder: String?,

    /**
     * Using either random nouns or incrementing ContentIds
     */
    val useRandomContentIds: Boolean = true,

    // This is can be either body or customCSSFile.
    val attributeId: String = "body",

    // TODO: Implement customAttributes
    /**
     * Custom attributes for content
     */
    override val customAttributes: Map<String, AttributeConfig>? = null,
    /**
     * Randomly generated custom attributes
     */
    override val generatedAttributes: GeneratedAttributeConfig? = null,

    /**
     * This is one because the [ContentConfiguration] is specific to one ContentAsset
     */
    override val elementCount: Int = 1

) : ExtendableObjectConfig