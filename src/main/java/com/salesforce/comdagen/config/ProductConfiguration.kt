/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.salesforce.comdagen.ExtendableObjectConfig


/**
 * Configuration for standard products (with options, no variants).
 */
data class ProductConfiguration(
    override val elementCount: Int = 1000,

    override val initialSeed: Long,

    val options: ProductOptionConfiguration? = null,

    override val generatedAttributes: GeneratedAttributeConfig? = null,
    override val localizableCustomAttributes: Map<String, AttributeConfig>? = null,

    override val customAttributes: Map<String, AttributeConfig>? = null
) : ExtendableObjectConfig
