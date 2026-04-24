package com.jellyflix.tv.di

import android.content.Context
import com.jellyflix.tv.data.BrandingRepository
import com.jellyflix.tv.data.JellyfinClient
import com.jellyflix.tv.data.MediaRepository
import com.jellyflix.tv.data.ServerInfoRepository
import com.jellyflix.tv.data.SessionStore
import com.jellyflix.tv.data.SettingsStore
import com.jellyflix.tv.playback.StreamUrlResolver
import com.jellyflix.tv.plugin.PluginHost
import com.jellyflix.tv.plugin.PluginManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideSessionStore(@ApplicationContext ctx: Context): SessionStore = SessionStore(ctx)

    @Provides @Singleton
    fun provideSettingsStore(@ApplicationContext ctx: Context): SettingsStore = SettingsStore(ctx)

    @Provides @Singleton
    fun provideJellyfinClient(@ApplicationContext ctx: Context, store: SessionStore): JellyfinClient =
        JellyfinClient(ctx, store)

    @Provides @Singleton
    fun provideMediaRepo(client: JellyfinClient, store: SessionStore): MediaRepository =
        MediaRepository(client, store)

    @Provides @Singleton
    fun providePluginManager(@ApplicationContext ctx: Context): PluginManager = PluginManager(ctx)

    @Provides @Singleton
    fun providePluginHost(@ApplicationContext ctx: Context, mgr: PluginManager): PluginHost =
        PluginHost(ctx, mgr)

    @Provides @Singleton
    fun provideServerInfoRepo(client: JellyfinClient): ServerInfoRepository = ServerInfoRepository(client)

    @Provides @Singleton
    fun provideBrandingRepo(client: JellyfinClient, store: SessionStore): BrandingRepository =
        BrandingRepository(client, store)

    @Provides @Singleton
    fun provideStreamResolver(
        @ApplicationContext ctx: Context,
        client: JellyfinClient,
        store: SessionStore,
    ): StreamUrlResolver = StreamUrlResolver(ctx, client, store)
}
