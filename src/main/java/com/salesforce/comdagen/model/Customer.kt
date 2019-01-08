/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.Encryption
import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.config.CustomerConfiguration
import java.time.LocalDate
import java.util.*

class Profile(
    private val seed: Long, private val config: CustomerConfiguration,
    private val region: SupportedZone
) {
    val firstName: String
        get() = RandomData.getRandomFirstName(region, seed + "firstName".hashCode())

    val lastName: String
        get() = RandomData.getRandomLastName(region, seed + "lastName".hashCode())

    val email: String
        get() = RandomData.getRandomEmail(seed + "email".hashCode())

    val birthday: String
        // fixed starting date so we can re-generate the exact same data
        get() = LocalDate.of(2017, 1, 1).minusDays(Random(seed).nextInt(maxAge).toLong()).toString()

    val phoneMobile: String
        get() = RandomData.getRandomPhoneNumber(region, seed + "phoneMobile".hashCode())

    companion object {
        val maxAge = 365 * 70 // in days
    }
}

class Address(
    val id: Long, private val seed: Long, private val config: CustomerConfiguration,
    private val region: SupportedZone
) {
    val firstName: String
        get() = RandomData.getRandomFirstName(region, seed + "firstName".hashCode())

    val lastName: String
        get() = RandomData.getRandomLastName(region, seed + "lastName".hashCode())

    val street: String
        get() {
            val rng = Random(seed)
            val street = RandomData.getRandomStreetName(region, rng.nextLong())
            val number = rng.nextInt(200)
            return when (region) {
                SupportedZone.German -> "$street $number"
                else -> "$number $street"
            }
        }

    val city: String
        get() = RandomData.getRandomCity(region, seed + "city".hashCode())

    val postalCode: String
        get() = RandomData.getRandomPostalCode(region, seed + "postalCode".hashCode())

    val phone: String
        get() = RandomData.getRandomPhoneNumber(region, seed + "phone".hashCode())

    val preferred: String
        get() = (id == 0L).toString()
}

/**
 * Represents a single customer
 */
class Customer(
    val id: Int, private val seed: Long, private val config: CustomerConfiguration, val region: SupportedZone,
    val attributeDefinitions: Set<AttributeDefinition>
) {
    val profile
        get() = Profile(seed, config, region)

    val password: String
        get() {
            val passwd = "CloudIs4LetterWord!"

            if (config.prehashPasswords) {
                // cache password hashes because encryption is such an expensive operation
                // if we ever make this dynamic, may need to up memory requirements or go back to our default strategy
                // to spend CPU instead of memory.
                return hashCache.getOrPut(passwd) { Encryption.scrypt(passwd) }
            }

            return passwd
        }

    val encryptionScheme: String?
        get() {
            if (config.prehashPasswords) {
                return "scrypt"
            }
            return null
        }

    val addresses: List<Address>
        get() {
            val rng = Random(seed)
            val addressCount = if (config.maxAddressCount > config.minAddressCount)
                rng.nextInt(config.maxAddressCount - config.minAddressCount) + config.minAddressCount
            else config.minAddressCount

            return (0..addressCount).map { Address(it.toLong(), rng.nextLong(), config, region) }
        }

    val customAttributes: List<CustomAttribute>
        get() {
            val rng = Random(seed + "customAttributes".hashCode())
            return attributeDefinitions.map { CustomAttribute(it, rng.nextLong()) }
        }

    companion object {
        private val hashCache: MutableMap<String, String> = mutableMapOf()
    }
}
