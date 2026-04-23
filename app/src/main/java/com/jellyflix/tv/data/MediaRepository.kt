package com.jellyflix.tv.data

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.api.client.extensions.userViewsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.BaseItemKind
import org.jellyfin.sdk.model.api.ItemFields
import org.jellyfin.sdk.model.api.ItemSortBy
import org.jellyfin.sdk.model.api.SortOrder

@Singleton
class MediaRepository @Inject constructor(
    private val client: JellyfinClient,
    private val session: SessionStore,
) {

    suspend fun currentUserId(): UUID {
        val s = session.session.first()
        return UUID.fromString(requireNotNull(s.userId) { "Not authenticated" })
    }

    suspend fun libraries(): List<BaseItemDto> {
        val api = client.api()
        val uid = currentUserId()
        return api.userViewsApi.getUserViews(userId = uid).content.items.orEmpty()
    }

    suspend fun continueWatching(limit: Int = 20): List<BaseItemDto> {
        val api = client.api()
        val uid = currentUserId()
        return api.itemsApi.getResumeItems(
            userId = uid,
            limit = limit,
            fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.OVERVIEW),
            mediaTypes = listOf("Video"),
            enableImages = true,
        ).content.items.orEmpty()
    }

    suspend fun nextUp(limit: Int = 20): List<BaseItemDto> {
        val api = client.api()
        val uid = currentUserId()
        return api.itemsApi.getNextUp(
            userId = uid,
            limit = limit,
            fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.OVERVIEW),
        ).content.items.orEmpty()
    }

    suspend fun latestInLibrary(libraryId: UUID, limit: Int = 30): List<BaseItemDto> {
        val api = client.api()
        val uid = currentUserId()
        return api.userLibraryApi.getLatestMedia(
            userId = uid,
            parentId = libraryId,
            limit = limit,
            fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO),
        ).content
    }

    suspend fun itemsIn(
        libraryId: UUID,
        start: Int = 0,
        limit: Int = 60,
        kinds: List<BaseItemKind> = listOf(BaseItemKind.MOVIE, BaseItemKind.SERIES),
    ): List<BaseItemDto> {
        val api = client.api()
        val uid = currentUserId()
        return api.itemsApi.getItems(
            userId = uid,
            parentId = libraryId,
            startIndex = start,
            limit = limit,
            includeItemTypes = kinds,
            recursive = true,
            sortBy = listOf(ItemSortBy.SORT_NAME),
            sortOrder = listOf(SortOrder.ASCENDING),
            fields = listOf(ItemFields.PRIMARY_IMAGE_ASPECT_RATIO, ItemFields.OVERVIEW),
        ).content.items.orEmpty()
    }

    suspend fun item(id: UUID): BaseItemDto {
        val api = client.api()
        return api.userLibraryApi.getItem(userId = currentUserId(), itemId = id).content
    }
}

