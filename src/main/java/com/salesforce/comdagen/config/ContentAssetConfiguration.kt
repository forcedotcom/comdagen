/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.salesforce.comdagen.Configuration

/**
 * Configuration class for content in libraries.
 */
data class ContentConfiguration(
    override val initialSeed: Long = 1234,
    val contentId: String?,
    val classificationFolder: String?,

    /**
     * Using either random nouns or incrementing ContentIds
     */
    val useRandomContentIds: Boolean = true,

    // This is can be either body or customCSSFile.
    val attributeId: AttributeId = AttributeId.BODY,

    // TODO: Implement customAttributes for content assets

    /**
     * This is one because the [ContentConfiguration] is specific to one ContentAsset
     */
    override val elementCount: Int = 1

) : Configuration

enum class AttributeId(val asAttributeString: String) {
    BODY("body"), CUSTOMCSSFILE("customCSSFile");

    /**
     * Will be used in the freemarker template.
     */
    override fun toString(): String {
        return asAttributeString
    }
}
