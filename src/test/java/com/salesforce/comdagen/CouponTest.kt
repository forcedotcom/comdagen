package com.salesforce.comdagen

import com.salesforce.comdagen.config.CouponConfiguration
import com.salesforce.comdagen.config.SystemCodeConfig
import com.salesforce.comdagen.generator.CouponGenerator
import com.salesforce.comdagen.model.CodeListCoupon
import com.salesforce.comdagen.model.SystemCodeCoupon
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CouponTest {

    companion object {
        private val seed: Long = 1234
    }

    @Test
    fun testCouponCount() {
        val codeListCouponsCount = 50
        val singleCodeCouponsCount = 50
        val systemCodeCouponsCount = 50

        val systemCodeConfig = SystemCodeConfig()
        val couponConfig = CouponConfiguration(elementCount = codeListCouponsCount, singleCodeCoupons = singleCodeCouponsCount,
                systemCodeCoupons = systemCodeCouponsCount, systemCodes = systemCodeConfig, initialSeed = seed)
        val couponGenerator = CouponGenerator(configuration = couponConfig)

        assertEquals(codeListCouponsCount + singleCodeCouponsCount + systemCodeCouponsCount,
                couponGenerator.objects.count())
    }

    @Test
    fun testCouponListCodeCount() {
        val elementCount = 100
        val minCodes = 15
        val maxCodes = 75

        val couponConfig = CouponConfiguration(elementCount = elementCount, minCodes = minCodes, maxCodes = maxCodes, initialSeed = seed)
        val couponGenerator = CouponGenerator(configuration = couponConfig)

        couponGenerator.objects.forEach { coupon ->
            if (coupon is CodeListCoupon) {
                assertTrue(coupon.codeList.size >= minCodes)
                assertTrue(coupon.codeList.size <= maxCodes)
            }
        }
    }

    @Test
    fun testCouponSystemCodeCount() {
        val elementCount = 100
        val minCodes = 15
        val maxCodes = 75

        val systemCodeConfig = SystemCodeConfig(minCodes = minCodes, maxCodes = maxCodes)
        val couponConfig = CouponConfiguration(systemCodeCoupons = elementCount, systemCodes = systemCodeConfig, initialSeed = seed)
        val couponGenerator = CouponGenerator(configuration = couponConfig)

        couponGenerator.objects.forEach { coupon ->
            if (coupon is SystemCodeCoupon) {
                assertTrue(coupon.systemCodes.maxNumberOfCodes >= minCodes)
                assertTrue(coupon.systemCodes.maxNumberOfCodes <= maxCodes)
            }
        }
    }

    @Test
    fun testCouponCodeUniqueness() {
        val elementCount = 100

        val couponConfig = CouponConfiguration(elementCount = elementCount, singleCodeCoupons = 0, systemCodeCoupons = 0, initialSeed = seed)
        val couponGenerator = CouponGenerator(configuration = couponConfig)

        val codes = couponGenerator.objects.toList().flatMap {
            it as CodeListCoupon
            it.codeList.map { it }
        }

        codes.forEach { code ->
            assertFalse(codes.minus(code).contains(code))
        }
    }

    @Test
    fun testCouponConfigEquals() {
        val configA = CouponConfiguration(elementCount = 10, initialSeed = seed)
        val configB = CouponConfiguration(elementCount = 10, initialSeed = seed)
        val configC = CouponConfiguration(elementCount = 15, initialSeed = seed)

        assertEquals(configA, configB)
        assertNotEquals(configA, configC)
    }
}
