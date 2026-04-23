package com.jellyflix.tv.data

import org.jellyfin.sdk.model.api.BaseItemDto

/**
 * Builds image URLs for items. Avoids depending on a connected ApiClient so
 * it can be called from composables without suspending.
 */
object ImageUrls {
    @Volatile var baseUrl: String? = null

    fun primary(item: BaseItemDto, maxWidth: Int = 480): String? {
        val base = baseUrl ?: return null
        val id = item.id
        val tag = item.imageTags?.get("Primary") ?: return null
        return "$base/Items/$id/Images/Primary?maxWidth=$maxWidth&quality=90&tag=$tag"
    }

    fun backdrop(item: BaseItemDto, maxWidth: Int = 1920): String? {
        val base = baseUrl ?: return null
        val id = item.id
        val tag = item.backdropImageTags?.firstOrNull() ?: return null
        return "$base/Items/$id/Images/Backdrop?maxWidth=$maxWidth&quality=85&tag=$tag"
    }
}
