/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.config.AttributeConfig
import com.salesforce.comdagen.config.AttributeConfig.DataType
import com.salesforce.comdagen.config.AttributeConfig.GenerationStrategy.*
import com.salesforce.comdagen.config.GeneratedAttributeConfig
import org.apache.commons.lang3.RandomStringUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * This file defines how custom attributes are implemented. They are a bit complicated because they are defined in the
 * context of another object, but are so similar that we don't support a "Product-Customattribute" and a different
 * "Category-Customattribute".
 *
 * To avoid duplication, there are three different classes involved in an actual custom attribute on a specific product:
 * 1. The configuration, defined in [AttributeConfig]
 * 2. An abstract definition, defined [AttributeDefinition]
 * 3. The actual value holder, as [CustomAttribute]
 *
 * A definition is the center piece - add a seed value to it and it can generate a value for the attribute it defines.
 */

data class CustomAttribute(
    override val definition: AttributeDefinition,
    val seed: Long
) : Attribute {
    constructor(path: String, config: AttributeConfig, seed: Long)
            : this(
        StandardAttributeDefinition(
            path,
            config.type,
            config.searchable,
            config.generationStrategy,
            config.dataStore
        ), seed
    )

    override val value: String
        get() = when (definition.generationStrategy) {
            STATIC -> definition.dataStore.toString()
            COUNTER -> (definition.dataStore as AttributeConfig.Counter).next().toString()
            RANDOM -> when (definition.type) {
                DataType.BOOLEAN -> Random(seed).nextBoolean().toString()
                DataType.DATE -> maxDate.minusDays(Random(seed).nextInt(maxDays).toLong()).toString()
                DataType.STRING -> RandomStringUtils.random(12, 0, 0, true, true, null, Random(seed))
                DataType.EMAIL -> RandomData.getRandomEmail(seed)
            }
            LIST -> {
                val possibleValues = definition.dataStore as List<*>
                possibleValues[Random(seed).nextInt(possibleValues.size)].toString()
            }
        }

    companion object {
        val minDate: LocalDate = LocalDate.ofEpochDay(0) // all generated dates will be after the epoch
        val maxDate = LocalDate.of(2017, 1, 1)
        val maxDays: Int = ChronoUnit.DAYS.between(minDate, maxDate).toInt()

        fun getCustomAttributeDefinitions(
            extendedObj: String, seed: Long, customAttributeConfigs: Map<String, AttributeConfig>?,
            generatedAttributeConfig: GeneratedAttributeConfig?
        ): Set<AttributeDefinition> {
            // predefined custom attributes
            val predefinedAttributes = customAttributeConfigs?.map {
                StandardAttributeDefinition(
                    "$extendedObj.${it.key}",
                    it.value.type, it.value.searchable, it.value.generationStrategy, it.value.dataStore
                )
            }?.toSet()
                    ?: emptySet()

            // generated custom attributes
            val generatedAttributesConfig =
                RandomAttributeDefinition.fromConfig(extendedObj, generatedAttributeConfig, seed)
            return predefinedAttributes + generatedAttributesConfig
        }
    }
}