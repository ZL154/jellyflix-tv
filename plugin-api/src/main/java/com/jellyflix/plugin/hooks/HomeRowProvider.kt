package com.jellyflix.plugin.hooks

import com.jellyflix.plugin.PluginContext

/**
 * Plugins can add arbitrary rows to the home screen. Rows are rendered exactly
 * like first-party rows — a title plus a horizontal list of [Card]s, each with
 * a poster image URL and an action payload the plugin receives when clicked.
 */
interface HomeRowProvider {

    data class Card(
        val id: String,
        val title: String,
        val posterUrl: String?,
        val action: String,
    )

    data class Row(
        val id: String,
        val title: String,
        val cards: List<Card>,
        val priority: Int = 500,
    )

    fun provideRows(ctx: PluginContext): List<Row>
    fun onCardClick(ctx: PluginContext, rowId: String, action: String) {}
}
