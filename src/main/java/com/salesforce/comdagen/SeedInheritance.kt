/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.salesforce.InvalidSpecificationException
import org.slf4j.LoggerFactory
import java.util.*

/**
 * A deserializer that can be used on a hierarchical structure to "inherit" a value from parents.
 *
 * In our simple case, we will use it to annotate `initialSeed` values. It will save the "current" seed value
 * and apply it when it's missing.
 *
 * Note that specifying `initialSeed` _after_ the sub-configurations that would normally inherit this seed is an error
 * and will result in an exception. For simplicity, we don't check whether that specification would have an effect
 * (so we might end up not accepting a configuration that is fine).
 *
 * See https://stackoverflow.com/q/49946079/785663 for the public parts of this.
 */
class SeedInheritance : JsonDeserializer<Long>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Long {
        val currentContextName = p.parsingContext.pathAsPointer().toString()
                // cut the last "initalSeed variable name
                .replaceAfterLast("/", "")
        val configSeed = p.longValue

        val seedStack = initStackIfNeeded(ctxt)
        if (seedStack.isNotEmpty()) {
            val prevName = seedStack.peek().first
            // list context is bugged and needs extra care: https://github.com/FasterXML/jackson-dataformats-text/issues/83
            // we drop everything with the old index (since we know it can't be reached anymore)
            // so it's okay to push the incorrect 0 indexed value(s)
            if (p.parsingContext.parent.inArray() && p.parsingContext.parent.currentIndex == 0
                    && (collectionName(prevName) == collectionName(currentContextName))) {
                val base = collectionName(currentContextName)
                while (seedStack.isNotEmpty() && seedStack.peek().first.startsWith("$base/0/")) seedStack.pop()
            }
        }

        // need to re-check stack state - it may have been modified above
        if (seedStack.isNotEmpty() && seedStack.peek().first.startsWith(currentContextName)) {
            throw InvalidSpecificationException("Must specify initialSeed before it is used")
        }

        seedStack.push(currentContextName to configSeed)

        LOGGER.debug("Existing: $currentContextName, $configSeed")

        return configSeed
    }

    override fun getNullValue(ctxt: DeserializationContext): Long {
        val seedStack = initStackIfNeeded(ctxt)

        // here, the variable part (compare [deserialize]) is missing and the context is just the object name
        val currentContextName = ctxt.parser.parsingContext.pathAsPointer().toString()
        // find a seed on stack that has the same path
        while (seedStack.isNotEmpty() && !currentContextName.startsWith(seedStack.peek().first)) {
            seedStack.pop()
        }

        val configSeed =
                if (seedStack.isNotEmpty()) seedStack.peek().second
                else ctxt.getAttribute(siteSeedName) as Long?
                        ?: throw InvalidSpecificationException("Can not infer initialSeed and no default was given")

        // record current seed on the stack so potential children can find it
        seedStack.push(currentContextName to configSeed)

        LOGGER.debug("Defaulted: $currentContextName, $configSeed")

        return configSeed
    }

    private fun initStackIfNeeded(ctxt: DeserializationContext): Deque<Pair<String, Long>> {
        if (ctxt.getAttribute(seedStackName) == null) {
            ctxt.setAttribute(seedStackName, ArrayDeque<Pair<String, Long>>())
        }
        return ctxt.getAttribute(seedStackName) as Deque<Pair<String, Long>>
    }

    private fun collectionName(contextPath: String): String {
        // find _last_ "/\\d+/"
        val lastIdx = ".*/(\\d+)/.*?".toRegex()
        val match = lastIdx.matchEntire(contextPath)
        return if (match != null) contextPath.substring(0, match.groups[1]!!.range.start - 1) else contextPath
    }

    companion object {
        const val seedStackName = "seed"
        /**
         * Users _may_ specify a context attribute with this name to use as a default, when no other value can be inferred.
         *
         * When it's specified, it _must_ be a [Long].
         */
        const val siteSeedName = "siteSeed"

        private val LOGGER = LoggerFactory.getLogger(SeedInheritance::class.java)
    }
}