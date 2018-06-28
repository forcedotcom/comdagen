package com.salesforce.comdagen

import org.w3c.dom.ls.LSInput
import org.w3c.dom.ls.LSResourceResolver

class ResourceResolver : LSResourceResolver {
    override fun resolveResource(
        type: String?,
        namespaceURI: String?,
        publicId: String?,
        systemId: String?,
        baseURI: String?
    ): LSInput {
        val resourceAsStream = this.javaClass.classLoader.getResourceAsStream("schema/$systemId")
        return Input(publicId, systemId, resourceAsStream)
    }
}