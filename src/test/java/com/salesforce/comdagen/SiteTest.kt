package com.salesforce.comdagen

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.salesforce.comdagen.config.SiteConfiguration
import com.salesforce.comdagen.config.SitesConfig
import com.salesforce.comdagen.generator.SiteGenerator
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertNull

class SiteTest {
    @Rule @JvmField val testFolder = TemporaryFolder()

    @Test(expected = IllegalArgumentException::class)
    fun `no site specified errors`() {
        SitesConfig(elementCount = 0, defaults = null)
    }

    @Test
    fun `random single site created`() {
        val config = SitesConfig(elementCount = 1, initialSeed = 123, defaults = SiteConfiguration("random"))
        val generator = SiteGenerator(config, testFolder.root /* nothing in here, but we also have no config files specified */)

        val sites = generator.objects.toList()
        assertThat(sites, hasSize(equalTo(1)))
        assertThat(sites.first().name, equalTo("random"))
        assertNull(sites.first().catalogGenerator)
    }

    @Test
    fun `can generate multiple random sites`() {
        val config = SitesConfig(elementCount = 3, initialSeed = 123, defaults = SiteConfiguration("random"))
        val generator = SiteGenerator(config, testFolder.root)

        val sites = generator.objects.toList()
        assertThat(sites, hasSize(equalTo(3)))
        assert(sites.all { it.name == "random" })
        // ensure we're looking at three different sites
        //assert(sites.map { it.seed }.toHashSet(), hasSize(equalTo(3)))
    }

    @Test
    fun `nonrandom site created, no defaults`() {
        val config = SitesConfig(elementCount = 1, initialSeed = 123, sites = listOf(SiteConfiguration("First")), defaults = null)
        val generator = SiteGenerator(config, testFolder.root)

        val sites = generator.objects.toList()
        assertThat(sites, hasSize(equalTo(1)))
        assertThat(sites.first().name, equalTo("First"))
    }

    @Test
    fun `nonrandom site created, with defaults`() {
        val config = SitesConfig(elementCount = 1, initialSeed = 123, sites = listOf(SiteConfiguration("First")),
                defaults = SiteConfiguration("Doesn't matter", "My site"))
        val generator = SiteGenerator(config, testFolder.root)

        val sites = generator.objects.toList()
        assertThat(sites, hasSize(equalTo(1)))
        assertThat(sites.first().name, equalTo("First"))
        assertThat(sites.first().description, equalTo("My site"))
    }

    @Test
    fun `mixed scenario, random and nonrandom sites`() {
        val config = SitesConfig(elementCount = 2, initialSeed = 123, sites = listOf(SiteConfiguration("First")),
                defaults = SiteConfiguration("random", "My site"))

        val generator = SiteGenerator(config, testFolder.root)

        val sites = generator.objects.toList()
        assertThat(sites, hasSize(equalTo(2)))
        assert(sites.any { it.name == "First" })
        assert(sites.all { it.description == "My site" })
    }
}
