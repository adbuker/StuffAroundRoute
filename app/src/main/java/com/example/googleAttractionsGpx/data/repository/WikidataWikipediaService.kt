package com.example.googleAttractionsGpx.data.repository

import com.example.googleAttractionsGpx.domain.models.Coordinates
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import org.json.JSONObject

class WikidataWikipediaService(
    private val systemLanguage: String = Locale.getDefault().language,
    private val nominatimService: NominatimService = NominatimService()
) {

    fun findSpeciesItemUrlByScientificName(scientificName: String): String? {
        if (scientificName.isBlank()) return null

        return try {
            val sparqlQuery = """
            SELECT ?item WHERE {
              ?item wdt:P225 "${escapeSparqlString(scientificName)}";
                    wdt:P105 wd:Q7432.
              SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
            }
            """.trimIndent()

            val json = fetchWikidataJson(sparqlQuery, "GoogleAttractionsGpx/1.0 (Wikidata species lookup)")
            val bindings = json.getJSONObject("results").getJSONArray("bindings")
            if (bindings.length() == 0) {
                null
            } else {
                bindings.getJSONObject(0).getJSONObject("item").getString("value")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getWikipediaLinksForItem(itemUrl: String): String {
        if (itemUrl.isBlank()) return ""

        return try {
            val sparqlQuery = """
            SELECT (GROUP_CONCAT(DISTINCT ?sitelink; separator=", ") AS ?sitelinks)
            WHERE {
              BIND(<$itemUrl> AS ?item)
              ?sitelink schema:about ?item .
              ?sitelink schema:isPartOf ?wiki .
              FILTER(CONTAINS(STR(?wiki), "wikipedia.org"))
            }
            """.trimIndent()

            val json = fetchWikidataJson(sparqlQuery, "GoogleAttractionsGpx/1.0 (Wikipedia links lookup)")
            val bindings = json.getJSONObject("results").getJSONArray("bindings")
            if (bindings.length() == 0) {
                ""
            } else {
                bindings.getJSONObject(0).optJSONObject("sitelinks")?.optString("value", "") ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getFormattedWikipediaLinksForItem(itemUrl: String, coordinates: Coordinates): String {
        return formatWikipediaLinks(getWikipediaLinksForItem(itemUrl), getCountryLanguages(coordinates))
    }

    fun getFormattedWikipediaLinksForScientificName(scientificName: String, coordinates: Coordinates): String {
        val itemUrl = findSpeciesItemUrlByScientificName(scientificName) ?: return ""
        return getFormattedWikipediaLinksForItem(itemUrl, coordinates)
    }

    fun formatWikipediaLinksForCoordinates(wikipediaLinks: String, coordinates: Coordinates): String {
        return formatWikipediaLinks(wikipediaLinks, getCountryLanguages(coordinates))
    }

    fun getCountryLanguages(coordinates: Coordinates): List<String> {
        return try {
            nominatimService.getCountryLanguages(coordinates)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun formatWikipediaLinks(wikipediaLinks: String, countryLanguages: List<String>): String {
        if (wikipediaLinks.isEmpty()) return ""

        val links = wikipediaLinks.split(", ").map { it.trim() }.filter { it.isNotEmpty() }
        val sortedLinks = links.sortedWith { link1, link2 ->
            val isSystemLang1 = link1.contains("${systemLanguage}.wikipedia.org")
            val isSystemLang2 = link2.contains("${systemLanguage}.wikipedia.org")

            val isCountryLang1 = countryLanguages.any { link1.contains("${it}.wikipedia.org") }
            val isCountryLang2 = countryLanguages.any { link2.contains("${it}.wikipedia.org") }

            when {
                isSystemLang1 && !isSystemLang2 -> -1
                !isSystemLang1 && isSystemLang2 -> 1
                isCountryLang1 && !isCountryLang2 -> -1
                !isCountryLang1 && isCountryLang2 -> 1
                else -> 0
            }
        }

        return sortedLinks.joinToString("\n\n")
    }

    private fun fetchWikidataJson(sparqlQuery: String, userAgent: String): JSONObject {
        val encodedQuery = URLEncoder.encode(sparqlQuery, "UTF-8")
        val url = "https://query.wikidata.org/sparql?query=$encodedQuery&format=json"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", userAgent)
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        return JSONObject(response)
    }

    private fun escapeSparqlString(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
    }
}
