/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen

import org.apache.commons.lang3.RandomStringUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton for all the data points we can use to construct objects.
 *
 * We lazily populate each data type/region pair as it's highly unlikely that one process uses several regions and
 * front-loading carries significant memory penalty.
 *
 * If too much data is loaded, we'll switch to pre-sorting step where the content files are copied to temporary storage
 * and pre-shuffled according to the start seed and then drawn from sequentially (preserving the `O(1)` access property).
 */
object RandomData {
    val SENTENCES = ConcurrentHashMap<SupportedZone, List<String>>(1)

    val NOUNS = ConcurrentHashMap<SupportedZone, List<String>>(1)

    val FIRST_NAMES = ConcurrentHashMap<SupportedZone, List<String>>(1)

    val LAST_NAMES = ConcurrentHashMap<SupportedZone, List<String>>(1)

    val BOOKS = ConcurrentHashMap<SupportedZone, String>(1)

    val streetNames = ConcurrentHashMap<SupportedZone, List<String>>(1)

    val cities = ConcurrentHashMap<SupportedZone, List<Pair<String, String>>>(1)

    /** A copy of `SitesConfig.emailDomain`, initialized from there. */
    var emailDomain: String = "varmail.net"

    fun getRandomFirstName(region: SupportedZone, randomLong: Long): String {
        val list = FIRST_NAMES.getOrPut(region, { contentFileAsList("FirstNames_${region.countryCode}.txt") })
        return list[(Math.abs(randomLong) % list.size).toInt()]
    }

    fun getRandomLastName(region: SupportedZone, randomLong: Long): String {
        val list = LAST_NAMES.getOrPut(region, { contentFileAsList("LastNames_${region.countryCode}.txt") })
        return list[(Math.abs(randomLong) % list.size).toInt()]
    }

    fun getRandomStreetName(region: SupportedZone, seed: Long): String = when (region) {
        SupportedZone.German -> {
            val list = streetNames.getOrPut(region, { contentFileAsList("StreetNames_${region.countryCode}.txt") })
            list[(Math.abs(seed) % list.size).toInt()]
        }
        else -> RandomStringUtils.random(12, 0, 0, true, false, null, Random(seed))
    }

    fun getRandomCity(region: SupportedZone, seed: Long): String {
        if (region == SupportedZone.Generic) {
            return RandomStringUtils.random(12, 0, 0, true, false, null, Random(seed))
        }
        val list = cities.getOrPut(region, {
            contentFileAsList("CitiesZip_${region.countryCode}.csv").map {
                val t = it.split(';')
                t[0] to t[1]
            }
        })
        return list[(Math.abs(seed) % list.size).toInt()].first
    }

    fun getRandomPostalCode(region: SupportedZone, seed: Long): String {
        if (region == SupportedZone.Generic) {
            return RandomStringUtils.random(5, 0, 0, false, true, null, Random(seed))
        }
        val list = cities.getOrPut(region, {
            contentFileAsList("CitiesZip_${region.countryCode}.csv").map {
                val t = it.split(';', limit = 2)
                t[0] to t[1]
            }
        })
        return list[(Math.abs(seed) % list.size).toInt()].second
    }

    fun getRandomPhoneNumber(region: SupportedZone, seed: Long): String {
        val generator = Random(seed)
        // format: +<country-code> <region> <phone>
        return "+${generator.nextInt(1000)}" + String.format("%05d", generator.nextInt(99999)) +
                String.format("%05d", generator.nextInt(99999))
    }

    fun getRandomNoun(seed: Long, region: SupportedZone = SupportedZone.Generic): String {
        val source = "nouns_${region.countryCode}.properties"
        if (exists(source)) {
            val list = NOUNS.getOrPut(region, { contentFileAsList(source) })
            return list[(Math.abs(seed) % list.size).toInt()]
        } else {
            val maxWordLength = 500 // totally arbitrary guess
            val book = BOOKS.getOrPut(region, {
                RandomData::class.java
                    .getResourceAsStream("/contentfiles/books_${region.countryCode}.txt").reader().readText()
            })
            val rawStart =
                if (book.length > maxWordLength) Random(seed).nextInt(book.length - maxWordLength)
                else 0
            val startIndex = book.indexOfAny(" \r\n\t".toCharArray(), rawStart)
            return book.substring(startIndex, book.indexOf(" ", startIndex))
        }
    }

    fun getRandomSentence(seed: Long, region: SupportedZone = SupportedZone.Generic): String {
        val source = "sentences_${region.countryCode}.properties"
        return if (exists(source)) {
            val list = SENTENCES.getOrPut(region, { contentFileAsList(source) })
            list[(Math.abs(seed) % list.size).toInt()]
        } else {
            bookCite(seed, 200, region)
        }
    }

    fun getRandomEmail(seed: Long) =
        RandomStringUtils.random(12, 0, 0, true, false, null, Random(seed)) + "@$emailDomain"

    fun getRandomCouponCode(seed: Long): String {
        val rng = Random(seed)
        return "${getRandomString(rng.nextLong(), 4)}-${getRandomString(
            rng.nextLong(),
            4
        )}-${getRandomString(rng.nextLong(), 4)}-${getRandomString(rng.nextLong(), 4)}".toUpperCase()
    }

    private fun getRandomString(seed: Long, length: Int): String {
        return RandomStringUtils.random(length, 0, 0, true, true, null, Random(seed))
    }

    fun getRandomUri(seed: Long): String {
        return "/${RandomStringUtils.random(15, 0, 0, true, false, null, Random(seed))}"
    }

    fun bookCite(seed: Long, length: Int = 1000, region: SupportedZone = SupportedZone.Generic): String {
        val book = BOOKS.getOrPut(region, {
            RandomData::class.java
                .getResourceAsStream("/contentfiles/books_${region.countryCode}.txt").reader().readText()
        })

        val rawStart =
            if (book.length > length) Random(seed).nextInt(book.length - length)
            else 0
        val startIndex = book.indexOfAny(" \r\n\t".toCharArray(), rawStart)

        return book.substring(startIndex, Math.min(startIndex + length, book.length))
    }

    private fun exists(resourceFileName: String) =
        RandomData::class.java.getResource("/contentfiles/$resourceFileName") != null

    /**
     * Grab resource file identified by `resourceFileName` in `/contentfiles/` and convert it to a list of lines.
     */
    private fun contentFileAsList(resourceFileName: String) =
        RandomData::class.java.getResourceAsStream("/contentfiles/$resourceFileName").bufferedReader().readLines()
}
