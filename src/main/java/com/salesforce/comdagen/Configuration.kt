/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen

import com.salesforce.comdagen.config.AttributeConfig
import com.salesforce.comdagen.config.GeneratedAttributeConfig
import com.salesforce.comdagen.model.CustomAttribute

/**
 * Minimal supported attributes on user-facing configuration classes.
 *
 * This is what code can rely on to be in _every_ configuration class. Typically, implementations would use
 * `@JsonProperty` or rely on property detection by Jackson to read these from their respective config files.
 */
interface Configuration {

    /**
     * How many elements will be generated?
     *
     * Note that some configuration files generate more than one type of objects. Those should state explicitly what
     * kind of object this count refers to.
     */
    val elementCount: Int

    /**
     * User specified starting value for random number generation.
     *
     * If the user does _not_ specify a seed value it will be "inherited" from it's parent configuration (the one
     * including this configuration). Note that while `SiteConfiguration` doesn't technically include configuration
     * objects, it does by reference and thus will pass it's seed value.
     *
     * See `Site` for how we load configuration files.
     */
    val initialSeed: Long
}

/**
 * Indicate that the implementation supports custom attributes (both user-specified and automatically generated).
 */
interface ExtendableObjectConfig : Configuration {
    /**
     * User specified extension attributes.
     *
     * The name of the attribute is given as key, the specification as value.
     */
    val customAttributes: Map<String, AttributeConfig>?

    /**
     * Defines automatically generated attributes.
     */
    val generatedAttributes: GeneratedAttributeConfig?
}

/**
 * A configuration that has information on how to render an XML file.
 */
interface RenderConfig : Configuration {
    /**
     * The file name that generated output will be written to.
     *
     * The pattern can contain a `${i}` string, which will be replaced by the cardinality of the currently generated
     * file (see [getFileName]).
     */
    val outputFilePattern: String
    /**
     * The output directory for generated files.
     */
    val outputDir: String

    /**
     * Where to get the template for the output file.
     */
    val templateName: String

    fun getFileName(index: Int = 0): String {
        var fileName = outputFilePattern
        if (outputFilePattern.contains("\${i}")) {
            if (index > 0) {
                fileName = fileName.replace("\${i}", index.toString())
            } else {
                fileName = fileName.replace("\${i}", "")
            }
        }

        return fileName
    }

}

inline fun <reified T : ExtendableObjectConfig> T.attributeDefinitions(objName: String = T::class.java.name) =
    CustomAttribute.getCustomAttributeDefinitions(
        objName,
        this.initialSeed,
        this.customAttributes,
        this.generatedAttributes
    )

