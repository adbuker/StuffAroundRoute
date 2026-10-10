package com.example.googleAttractionsGpx

import com.example.googleAttractionsGpx.data.repository.buildGpxContent
import com.example.googleAttractionsGpx.domain.models.Coordinates
import com.example.googleAttractionsGpx.domain.models.PointData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GpxContentTest {

    private val point = PointData(
        coordinates = Coordinates(55.75, 37.61),
        name = "Test point",
        description = "Test description"
    )

    @Test
    fun buildGpxContent_addsTrackDescriptionToMetadata() {
        val gpx = buildGpxContent(
            listOf(point),
            trackDescription = "Time intervals: Morning 8:00-10:59, Evening 17:00-19:59"
        )

        assertTrue(gpx.contains("<metadata>"))
        assertTrue(gpx.contains("<desc>Time intervals: Morning 8:00-10:59, Evening 17:00-19:59</desc>"))
        assertTrue(gpx.indexOf("<metadata>") < gpx.indexOf("<wpt"))
    }

    @Test
    fun buildGpxContent_omitsMetadataWhenTrackDescriptionIsNull() {
        val gpx = buildGpxContent(listOf(point))

        assertFalse(gpx.contains("<metadata>"))
    }

    @Test
    fun buildGpxContent_omitsMetadataWhenTrackDescriptionIsBlank() {
        val gpx = buildGpxContent(listOf(point), trackDescription = "  ")

        assertFalse(gpx.contains("<metadata>"))
    }
}
