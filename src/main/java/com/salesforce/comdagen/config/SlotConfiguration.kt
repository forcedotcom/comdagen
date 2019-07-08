package com.salesforce.comdagen.config


import com.salesforce.comdagen.RenderConfig

/**
 * Configuration for slots.xml files. This currently is only used for template and output file specification. Therefore
 * it does not back a yaml config.
 */
class SlotConfiguration : RenderConfig {

    override val elementCount: Int = 2

    override val initialSeed: Long = 1234

    override val outputFilePattern: String = "slots.xml"
    override val outputDir: String = "sites"
    override val templateName: String = "slots.ftlx"
}
