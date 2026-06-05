package com.example.googleAttractionsGpx

import com.example.googleAttractionsGpx.data.repository.INaturalistGpxGenerator
import com.example.googleAttractionsGpx.data.repository.WikidataWikipediaService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WikidataWikipediaServiceTest {

    @Test
    fun formatWikipediaLinks_emptyInputReturnsEmptyString() {
        val service = WikidataWikipediaService(systemLanguage = "en")

        assertEquals("", service.formatWikipediaLinks("", listOf("ru")))
    }

    @Test
    fun formatWikipediaLinks_prioritizesSystemLanguage() {
        val service = WikidataWikipediaService(systemLanguage = "ru")
        val links = "https://en.wikipedia.org/wiki/Test, https://ru.wikipedia.org/wiki/Test"

        val formatted = service.formatWikipediaLinks(links, emptyList())

        assertEquals(
            "https://ru.wikipedia.org/wiki/Test\n\nhttps://en.wikipedia.org/wiki/Test",
            formatted
        )
    }

    @Test
    fun formatWikipediaLinks_prioritizesCountryLanguagesBeforeUnrelatedLanguages() {
        val service = WikidataWikipediaService(systemLanguage = "de")
        val links = "https://en.wikipedia.org/wiki/Test, https://fr.wikipedia.org/wiki/Test"

        val formatted = service.formatWikipediaLinks(links, listOf("fr"))

        assertEquals(
            "https://fr.wikipedia.org/wiki/Test\n\nhttps://en.wikipedia.org/wiki/Test",
            formatted
        )
    }

    @Test
    fun inaturalistDescription_appendsWikipediaLinksWhenProvided() {
        val description = INaturalistGpxGenerator.buildDescription(
            observedOn = "2026-05-19",
            observationId = 123L,
            timeDescription = "morning (2)",
            timeLabel = "Best time:",
            wikipediaLinks = "https://en.wikipedia.org/wiki/Test"
        )

        assertTrue(description.contains("https://www.inaturalist.org/observations/123"))
        assertTrue(description.contains("Wikipedia articles:\nhttps://en.wikipedia.org/wiki/Test"))
    }

    @Test
    fun inaturalistDescription_omitsWikipediaSectionWhenLinksAreBlank() {
        val description = INaturalistGpxGenerator.buildDescription(
            observedOn = "2026-05-19",
            observationId = 123L,
            timeDescription = "all day",
            timeLabel = "Best time:",
            wikipediaLinks = ""
        )

        assertFalse(description.contains("Wikipedia articles:"))
    }
}
