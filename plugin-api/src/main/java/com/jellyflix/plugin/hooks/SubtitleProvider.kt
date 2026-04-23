package com.jellyflix.plugin.hooks

import com.jellyflix.plugin.PluginContext
import java.util.UUID

/**
 * Implement to add an external subtitle source (OpenSubtitles, SubDL, A.Db, etc).
 * Returned URLs must resolve to SRT/VTT/ASS content.
 */
interface SubtitleProvider {
    data class Candidate(
        val language: String,
        val label: String,
        val url: String,
        val format: String = "vtt",
        val hearingImpaired: Boolean = false,
        val rating: Float = 0f,
    )

    suspend fun search(ctx: PluginContext, itemId: UUID, title: String, year: Int?, language: String?): List<Candidate>
}
