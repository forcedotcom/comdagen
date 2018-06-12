/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.config.RedirectUrlConfiguration
import com.salesforce.comdagen.model.*
import java.util.*

/**
 * Generates redirect urls for products and categories
 *
 * @param categoryAssignments list of product category assignments used for product redirects (optional)
 * @param categories list of generated categories to generate category redirects for (optional)
 */
data class RedirectUrlGenerator(override val configuration: RedirectUrlConfiguration,
                                private val categories: List<Category>? = null,
                                private val categoryAssignments: Sequence<CategoryAssignment> = emptySequence())
    : Generator<RedirectUrlConfiguration, RedirectUrl> {

    override val objects: Sequence<RedirectUrl>
        get() {
            val rng = Random(configuration.initialSeed)

            // generate static redirect urls
            val staticRedirects = (1..configuration.elementCount).asSequence().map { StaticRedirectUrl(rng.nextLong()) }

            // generate product redirects by taking from categoryAssignments, constructing N redirects from category to
            // product
            val productRedirects =
                    categoryAssignments.take(configuration.productRedirects).map { ProductRedirectUrl(rng.nextLong(), it) }

            // generate category redirects if categories list is not null
            val categoryRedirects = if (categories != null)
                (1..configuration.categoryRedirects).asSequence().map { CategoryRedirectUrl(rng.nextLong(), categories) } else emptySequence()

            return staticRedirects.plus(productRedirects).plus(categoryRedirects)
        }

    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}
