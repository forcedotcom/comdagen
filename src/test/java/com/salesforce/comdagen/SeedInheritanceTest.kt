/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.salesforce.InvalidSpecificationException
import org.junit.Test
import kotlin.test.fail

class SeedInheritanceTest {

    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true)
        .addMixIn(Configuration::class.java, SeedInheritanceMixin::class.java)
        .readerFor(ConfList::class.java)

    class Conf(override val elementCount: Int, override val initialSeed: Long) : Configuration
    class ConfList(override val elementCount: Int, override val initialSeed: Long, val subConf: List<Conf>?) :
        Configuration

    @Test
    fun `test ListInheritance With Default`() {
        val testString = """
ConfList:
  elementCount: 2
  initialSeed: 1111
  subConf:
    - elementCount: 1
    - elementCount: 1
"""
        val actual = mapper.readValue<ConfList>(testString)

        assert(actual.subConf != null && actual.subConf.size == 2)
        assert(actual.subConf!![0].initialSeed == 1111L)
        assert(actual.subConf[1].initialSeed == 1111L)
    }

    @Test
    fun `test ListInheritance With Override`() {
        val testString = """
ConfList:
  elementCount: 2
  initialSeed: 1111
  subConf:
    - elementCount: 1
    - elementCount: 1
      initialSeed: 2222
"""
        val actual = mapper.readValue<ConfList>(testString)

        assert(actual.subConf != null && actual.subConf.size == 2)
        assert(actual.subConf!![0].initialSeed == 1111L)
        assert(actual.subConf[1].initialSeed == 2222L)
    }

    @Test
    fun `test Inheritance Fail On Missing Value`() {
        val testString = """
ConfList:
  elementCount: 2
  subConf:
    - elementCount: 1
    - elementCount: 1
      initialSeed: 2222
"""
        expectWrappedInvalidConfigurationExceptionFor(mapper, testString)
    }

    @Test
    fun `test No Inheritance Ok`() {
        val testString = """
ConfList:
  elementCount: 2
  initialSeed: 0000
  subConf:
    - elementCount: 1
      initialSeed: 1111
    - elementCount: 1
      initialSeed: 2222
"""
        val actual = mapper.readValue<ConfList>(testString)
        assert(actual.subConf != null && actual.subConf.size == 2)
        assert(actual.subConf!![0].initialSeed == 1111L)
        assert(actual.subConf[1].initialSeed == 2222L)
    }

    // we can't use `@Test(expected)` for this as the expected exception is wrapped
    @Test
    fun `test Specification After Inheritance Errors`() {
        val testString = """
ConfList:
  elementCount: 2
  subConf:
    - elementCount: 1
    - elementCount: 1
      initialSeed: 2222
  initialSeed: 0000
"""
        val defaultingMapper = mapper.withAttribute(SeedInheritance.siteSeedName, -1L)
        expectWrappedInvalidConfigurationExceptionFor(defaultingMapper, testString)
    }

    private fun expectWrappedInvalidConfigurationExceptionFor(mapper: ObjectReader, testString: String) {
        val result = try {
            mapper.readValue<ConfList>(testString)
        } catch (e: JsonMappingException) {
            e
        }
        when (result) {
            is Throwable -> assert(
                result.cause is InvalidSpecificationException,
                { "Expected: ${InvalidSpecificationException::class.simpleName}, got: $result" })
            else -> fail("Expected ${InvalidSpecificationException::class.simpleName}, got $result")
        }
    }
}