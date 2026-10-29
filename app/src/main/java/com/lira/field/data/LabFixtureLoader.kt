package com.lira.field.data

import android.content.Context
import com.lira.field.core.model.*
import com.lira.field.core.network.NetworkClient
import com.squareup.moshi.Json
import java.io.InputStreamReader

data class LabFixtureRoot(
    @Json(name = "server_time") val serverTime: String? = null,
    @Json(name = "assignments") val assignments: List<AssignmentV13> = emptyList(),
    @Json(name = "calls") val calls: List<CallV13> = emptyList(),
    @Json(name = "bay_board") val bayBoard: BayBoard? = null,
    @Json(name = "interchanges") val interchanges: List<Interchange> = emptyList(),
    @Json(name = "why") val why: WhySheet? = null
)

object LabFixtureLoader {

    private var cachedRoot: LabFixtureRoot? = null

    fun load(context: Context): LabFixtureRoot {
        cachedRoot?.let { return it }
        return try {
            context.assets.open("lab_fixture_v13.json").use { inputStream ->
                val json = InputStreamReader(inputStream).readText()
                val adapter = NetworkClient.moshi.adapter(LabFixtureRoot::class.java)
                val root = adapter.fromJson(json) ?: LabFixtureRoot()
                cachedRoot = root
                root
            }
        } catch (_: Exception) {
            LabFixtureRoot()
        }
    }

    fun clearCache() {
        cachedRoot = null
    }
}

