/*
 *  Copyright (c) 2018, salesforce.com, inc.
 *  All rights reserved.
 *  SPDX-License-Identifier: BSD-3-Clause
 *  For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.comdagen.model

import com.salesforce.comdagen.RandomData
import com.salesforce.comdagen.SupportedZone
import com.salesforce.comdagen.config.ContentConfiguration


/**
 * Abstract content class which requires a seed and a contentConfig.
 */
abstract class AbstractContent(
    open val seed: Long,
    private val contentConfig: ContentConfiguration
) {
    open val attributeId: String get() = contentConfig.attributeId
    open val contentId: String
        get() = contentConfig.contentId ?: "unnamed-content-id"
    open val displayName: Map<SupportedZone, String>
        get() = mapOf(SupportedZone.Generic to "unnamed-content-name")

    open val description: Map<SupportedZone, String>
        get() = mapOf(SupportedZone.Generic to "unnamed-content-description")

    val onlineFlag: Boolean get() = contentConfig.onlineFlag

    val searchable: Boolean get() = contentConfig.searchableFlag

    val folderId: String? get() = contentConfig.classificationFolder

    val importMode: Boolean? get() = contentConfig.importModeDelete

    val classificationFolder: String? get() = contentConfig.classificationFolder

    open val customBody: Map<SupportedZone, String>
        get() = mapOf(SupportedZone.Generic to "unnamed-content-body")
}

/**
 * This class resembles one content asset. [contentId], [displayName], [customBody] and
 * [description] are randomly generated.
 */
open class RandomContent(
    override val seed: Long,
    contentConfig: ContentConfiguration,
    open val regions: List<SupportedZone>
) : AbstractContent(seed, contentConfig) {

    override val contentId: String
        get() = RandomData.getRandomNoun(seed + "contentId".hashCode())

    override val displayName: Map<SupportedZone, String>
        get() = regions.associateBy(
            { it },
            { RandomData.getRandomNoun(seed + "displayName".hashCode(), it) })

    override val description: Map<SupportedZone, String>
        get() = regions.associateBy(
            { it },
            { RandomData.getRandomSentence(seed + "description".hashCode(), it) })

    /*
     * Should I move this into the freemarker template?
     * Pros: Easier to see and edit without diving into the sourcecode
     * Cons: Could get complicated with more specialised templates; no easy access to additional vars;
     * limited in computation; many different types of content assets could get very confusing
     */
    override val customBody: Map<SupportedZone, String>
        get() = SupportedZone.values().associateBy({ it },
            {
                /**
                 * This is a simple generated content assets consisting of a random noun as header,
                 * a random sentence and different styles.
                 * The template looks similar to this:
                 * <div style="$generalStyle">
                 *     <h1 style="$headerStyle">$header</h1>
                 *     <hr />
                 *     <p style="$paragraphStyle">$paragraph</p>
                 * </div>
                 */
                val headerStyle = "color: rgb(86, 79, 71);"
                val generalStyle = "padding:24px 16px 0 16px; font-size:1.1em;"
                val paragraphStyle = "margin:0 0 8px 0;"
                val header = RandomData.getRandomNoun(
                    seed + "header".hashCode(),
                    it
                )
                val paragraph = RandomData.getRandomSentence(seed + "body".hashCode(), it)

                // HTML template
                "&lt;div style=&quot;$generalStyle&quot;&gt;\n" +
                        "\t&lt;h1 style=&quot;$headerStyle&quot;&gt;" +
                        header + "&lt;/h1&gt;\n" + "\t&lt;hr /&gt;\n" +
                        "\t&lt;p style=&quot;$paragraphStyle&quot;&gt;" +
                        paragraph + "&lt;/p&gt;\n" +
                        "\t&lt;/div&gt;"
            })
}

/**
 * This class resembles a random content asset, specifically a [RandomContent], that has a index mixed in so that
 * the [contentId] is represented in the fashion of ContentId_<internId>. The seed is a generated pseudo random number.
 */
open class IndexedRandomContent(
    private val internId: Int,
    override val seed: Long,
    private val contentConfig: ContentConfiguration,
    override val regions: List<SupportedZone>
) : RandomContent(seed, contentConfig, regions) {

    override val contentId: String
        // If defined use random contentIds or ContentId_<number>
        get() = if (contentConfig.useRandomContentIds) RandomData.getRandomNoun(seed + "contentId".hashCode())
        else "ContentId_" + internId.toString()
}
