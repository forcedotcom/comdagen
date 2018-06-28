/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.config.AttributeConfig
import com.salesforce.comdagen.config.AttributeConfig.GenerationStrategy.RANDOM
import com.salesforce.comdagen.config.GeneratedAttributeConfig
import java.util.*

/**
 * An attribute joins the abstract "concept" of an attribute with an actual value produced out of that definition.
 * [CustomAttribute] is the most complicated implementation, others tend to be much simpler.
 */
interface Attribute {
    val definition: AttributeDefinition
    val value: String

    val id: String
        get() = definition.id

    val displayName: String
        get() = definition.displayName
}

/**
 * The "shape" of an attribute. Contains two parts: the meta information, mainly [path] and [type], and generator
 * information that tells implementations how the values for this attribute need to be generated (this defines the
 * available "value space" for this attribute).
 */
interface AttributeDefinition {
    /** Must be `object name "." id`. This is the globally unique identifier for this attribute. */
    val path: String

    /** Defines value interpretation. */
    val type: AttributeConfig.DataType

    /** Does this attribute need to be indexed? */
    val searchable: Boolean

    val generationStrategy: AttributeConfig.GenerationStrategy

    /** Implementations must document if they are using this, most probably in accordance with the set [generationStrategy]. */
    val dataStore: Any

    /** The identifier for the attribute. Must be unique for the object who has the attribute attached. */
    val id: String
        get() = path.substringAfterLast('.')

    /** Human readable name of the attribute. */
    val displayName: String
        get() = id.capitalize()
}

/**
 * A attribute configuration for randomly generated values. They are entirely determined by the concrete attribute
 * implementation, so they don't have any [dataStore].
 */
data class RandomAttributeDefinition(
    override val path: String,
    override val type: AttributeConfig.DataType,
    override val searchable: Boolean
) : AttributeDefinition {
    override val generationStrategy: AttributeConfig.GenerationStrategy
        get() = RANDOM

    override val dataStore: Any
        get() = throw IllegalAccessException("RANDOM strategy has no dataStore")

    companion object {
        /**
         * All attribute paths used to generate definitions in [fromConfig].
         *
         * We need to ensure no object has two custom attributes with the same id (see issue #76).
         * Since [Attribute.path] already contains the object name for other reasons, we can make use of that.
         * TODO move this to CustomAttribute to avoid collisions between defined and random attributes?
         */
        private val usedIds = mutableSetOf<String>()

        /**
         * A cache of all seeds that have been seen for a specific object.
         *
         * We need to remember them so we can adjust id calculation for attributes: we _must_ generate the same ids if
         * the same seed is supplied (see #101), but ids for seeds we see for the first time have to be different from
         * previously generated ones.
         */
        private val seenSeeds = mutableSetOf<String>()

        /**
         * Generate [GeneratedAttributeConfig.elementCount] custom attribute definitions, defined by the [seed].
         * Note that definitions will _not_ clash within the group defined by [extendedObj].
         */
        fun fromConfig(extendedObj: String, config: GeneratedAttributeConfig?, seed: Long): Set<AttributeDefinition> {
            if (config == null) {
                return emptySet()
            }
            val rng = Random(seed + "customAttributes".hashCode())
            val isNewSeed = seenSeeds.add("$extendedObj:$seed")
            return (1..config.elementCount.toLong()).map {
                RandomAttributeDefinition(
                    nextId(extendedObj, rng, isNewSeed),
                    AttributeConfig.DataType.values()[(Math.abs(rng.nextLong()) % AttributeConfig.DataType.values().size).toInt()],
                    it <= config.thereofSearchable
                )
            }.toSet()
        }

        private fun nextId(extendedObj: String, rng: Random, namesMustBeUnique: Boolean): String {
            var candidate: String = "" // KT-23763
            do {
                candidate = "$extendedObj.${cleanAttributeName(RandomData.getRandomNoun(rng.nextLong()))}"
            } while (namesMustBeUnique && !usedIds.add(candidate))
            return candidate
        }

        private fun cleanAttributeName(name: String): String = name.replace('.', '_').trim()
    }
}

data class StandardAttributeDefinition(
    override val path: String,
    override val type: AttributeConfig.DataType,
    override val searchable: Boolean,
    override val generationStrategy: AttributeConfig.GenerationStrategy,
    override val dataStore: Any
) : AttributeDefinition
