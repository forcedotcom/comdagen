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
 * This class reesembles one content asset. [contentId], [displayName], [customBody] and
 * [description] are randomly generated.
 */
open class RandomContent(
    override val seed: Long,
    private val contentConfig: ContentConfiguration,
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

    /**
     * This implements customAttributes for libraries.
     */
    /*
     * Should I move this into the freemarkertemplate? Need nested region tags then and might loose flexibility
     */
    override val customBody: Map<SupportedZone, String>
        get() = SupportedZone.values().associateBy({ it },
            {
                " <div style=\"padding:24px 16px 0 16px; font-size:1.1em;\">\n" +
                        "                    <h1 style=\"color: rgb(86, 79, 71);\">" +
                        RandomData.getRandomNoun(seed + "header".hashCode(), it) + "</h1>\n" +
                        "                    <hr />\n                    " + "<p style=\"margin:0 0 8px 0;\">" +
                        RandomData.getRandomSentence(seed + "body".hashCode(), it) + "</p>"
            })
}

/**
 * This resembles one content asset inside a library.
 */
open class IndexedRandomContent(
    private val internId: Int,
    override val seed: Long,
    private val contentConfig: ContentConfiguration,
    override val regions: List<SupportedZone>
) : RandomContent(seed, contentConfig, regions) {


    //TODO: BUG: contentId is always null?? dont use get() here for ftlx
    override val contentId: String
        // If defined use random contentIds or ContentId_<number>
        get() = if (contentConfig.useRandomContentIds) RandomData.getRandomNoun(seed + "contentId".hashCode())
        else "ContentId_" + internId.toString()
}
