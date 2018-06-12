/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.salesforce.comdagen.ExtendableObjectConfig

data class InventoryRecordConfiguration(
        val minCount: Int = 0,

        val maxCount: Int = 1000,

        override val customAttributes: Map<String, AttributeConfig>? = null,

        override val generatedAttributes: GeneratedAttributeConfig? = null,

        /** Not used. Number of records is determined by [InventoryConfig.coverage] percentage. */
        override val elementCount: Int = 0,
        override val initialSeed: Long
) : ExtendableObjectConfig
