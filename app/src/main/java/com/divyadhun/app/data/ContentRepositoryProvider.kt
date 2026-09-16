package com.divyadhun.app.data

import android.content.Context
import com.divyadhun.app.BuildConfig

/** One app-scoped content boundary. An absent catalog URL keeps the offline local fallback. */
object ContentRepositoryProvider {
    @Volatile
    private var repository: ContentRepository? = null

    fun get(context: Context): ContentRepository {
        repository?.let { return it }
        return synchronized(this) {
            repository ?: create().also { repository = it }
        }
    }

    private fun create(): ContentRepository {
        val catalogUrl = BuildConfig.REMOTE_CATALOG_URL.trim().takeIf { it.isNotEmpty() }
        if (catalogUrl == null) return LocalContentRepository()
        return RemoteContentRepository(
            dataSource = HttpRemoteContentDataSource(catalogUrl),
            fallback = LocalContentRepository(),
            mergeFallback = true
        )
    }
}
