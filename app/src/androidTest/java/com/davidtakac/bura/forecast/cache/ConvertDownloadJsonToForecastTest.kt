/*
 * Copyright 2024 David Takač
 *
 * This file is part of Bura.
 *
 * Bura is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Bura is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Bura. If not, see <https://www.gnu.org/licenses/>.
 */

package com.davidtakac.bura.forecast.cache

import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.davidtakac.bura.forecast.download.convertDownloadJsonToForecast
import com.davidtakac.bura.forecast.parameters.temperature.TemperaturePeriod
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Test
import java.time.ZoneId

@SmallTest
class ConvertDownloadJsonToForecastTest {
    /**
     * This test exercises the caching logic bug discovered in issue #127. It seems that persisting
     * the ZDT (with toString or DateTimeFormatter.ISO_ZONED_DATE_TIME) and then parsing it produces
     * incorrect results around the DST switch on older devices. I recreated the error on 26 and 30,
     * not sure about newer versions.
     *
     * This Java issue looks like a match: https://bugs.java.com/bugdatabase/JDK-8066982.
     * The cause is likely outdated Java on these older devices where the issue has not been patched.
     *
     * Our bug has been fixed by persisting the epoch second and zone ID separately, and then
     * reconstructing them into a ZDT when read from the cache. To recreate the error, revert commit
     * f850d406 and re-run the test on an emulator that satisfies the SdkSuppress annotation.
     */
    @Test
    @SdkSuppress(minSdkVersion = 26, maxSdkVersion = 30)
    fun downloading_caching_and_reading_from_cache_preserves_timezone_info(): Unit = runBlocking {
        val downloadedJson = InstrumentationRegistry
            .getInstrumentation()
            .context.assets
            .open("rome_api_response_on_oct_2026_dst_switch.json")
            .bufferedReader().use { it.readText() }
            .let { JSONObject(it) }
        val downloadedForecast = convertDownloadJsonToForecast(downloadedJson, ZoneId.of("Europe/Rome"))

        val persistedJson = convertForecastToCacheJson(downloadedForecast)
        val persistedForecast = convertCacheJsonToForecast(persistedJson)

        TemperaturePeriod(persistedForecast.temperature)
    }
}