/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.generator

import com.salesforce.comdagen.config.CouponConfiguration
import com.salesforce.comdagen.model.*
import java.util.*

data class CouponGenerator(override val configuration: CouponConfiguration) : Generator<CouponConfiguration, Coupon> {

    override val objects: Sequence<Coupon>
        get() {
            val rng = Random(configuration.initialSeed)

            var coupons: Sequence<Coupon> = emptySequence()

            // generate code list coupons
            coupons = coupons.plus((1..configuration.elementCount).asSequence().map {
                CodeListCoupon(rng.nextLong(), configuration)
            })

            // generate single code coupons
            coupons = coupons.plus((1..configuration.singleCodeCoupons).asSequence().map {
                SingleCodeCoupon(rng.nextLong())
            })

            // generate system code coupons
            if (configuration.systemCodes != null) {
                coupons = coupons.plus((1..configuration.systemCodeCoupons).asSequence().map {
                    SystemCodeCoupon(rng.nextLong(), configuration.systemCodes)
                })
            }

            return coupons
        }

    override val creatorFunc: (idx: Int, seed: Long) -> Coupon
        get() = throw UnsupportedOperationException("Use objects accessor")

    override val metadata: Map<String, Set<AttributeDefinition>>
        get() = emptyMap()
}
