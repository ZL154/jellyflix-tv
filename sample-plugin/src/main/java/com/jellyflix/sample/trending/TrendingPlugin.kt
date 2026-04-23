package com.jellyflix.sample.trending

import com.jellyflix.plugin.Plugin
import com.jellyflix.plugin.PluginContext
import com.jellyflix.plugin.hooks.HomeRowProvider

/**
 * Minimal example: adds a single "Trending this week" row to the home screen
 * using static content. A real plugin would fetch titles from an external API
 * (TMDB, Trakt, etc) and cache them.
 */
class TrendingPlugin : Plugin, HomeRowProvider {

    override fun onCreate(ctx: PluginContext) {
        ctx.log("TrendingPlugin", "loaded")
    }

    override fun provideRows(ctx: PluginContext): List<HomeRowProvider.Row> = listOf(
        HomeRowProvider.Row(
            id = "trending-weekly",
            title = "Trending this week",
            priority = 450,
            cards = listOf(
                HomeRowProvider.Card("tt1", "Dune: Part Two", null, "search:dune part two"),
                HomeRowProvider.Card("tt2", "The Bear", null, "search:the bear"),
                HomeRowProvider.Card("tt3", "Shōgun", null, "search:shogun"),
            ),
        )
    )

    override fun onCardClick(ctx: PluginContext, rowId: String, action: String) {
        ctx.log("TrendingPlugin", "card clicked: $rowId / $action")
    }
}
