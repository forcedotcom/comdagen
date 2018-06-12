/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.salesforce.comdagen.config.AttributeConfig.GenerationStrategy.*

/**
 * All configurable attribute need to have the format specified in this class.
 *
 * @property type chose how to interpret the attribute data
 *
 * @property searchable will the attribute be indexed (and thus searchable)
 *
 * @property generationStrategy how will the data be generated
 *
 * @property dataStore the known data portion, if any
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AttributeConfig
constructor(val type: AttributeConfig.DataType, val searchable: Boolean, val generationStrategy: AttributeConfig.GenerationStrategy,
            /**
             * Contains the value to be used for this attribute. Interpretation varies depending on the
             * [.generationStrategy].
             */
            val dataStore: Any) {
    enum class DataType(private val text: String) {
        BOOLEAN("boolean"), STRING("string"), DATE("date"), EMAIL("email");

        @JsonValue
        fun forJackson(): String {
            return name.toLowerCase()
        }

        override fun toString(): String {
            return text
        }
    }

    enum class GenerationStrategy {
        LIST, RANDOM, STATIC, COUNTER;

        @JsonValue
        fun forJackson(): String {
            return name.toLowerCase()
        }
    }

    class Counter(@JsonProperty("offset") var current: Long = 0, val increment: Long = 1) {
        operator fun next(): Long {
            current += increment
            return current
        }
    }

    companion object {

        @JsonCreator
        @JvmStatic
        fun parseConfig(@JsonProperty("type") type: DataType,
                        @JsonProperty("data") generationStrategy: GenerationStrategy,
                        @JsonProperty("staticValue") staticValue: String?,
                        @JsonProperty("counter") counterConfig: Counter?,
                        @JsonProperty("list") listConfig: List<String>?,
                        @JsonProperty("searchable") searchable: Boolean = false): AttributeConfig {
            // note that we fail with NPE when the matching data gen strategy isn't specified
            when (generationStrategy) {
                STATIC -> return AttributeConfig(type, searchable, STATIC, staticValue!!)
                COUNTER -> return AttributeConfig(type, searchable, COUNTER, counterConfig!!)
                LIST -> return AttributeConfig(type, searchable, LIST, listConfig!!)
                else -> return AttributeConfig(type, searchable, generationStrategy, Any())
            }
        }

    }
}

