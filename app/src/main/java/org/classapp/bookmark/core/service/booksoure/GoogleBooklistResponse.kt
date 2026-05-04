package org.classapp.bookmark.core.service.booksoure


data class GoogleBooklistResponse(
    val items: List<VolumeItem>?
) {
    fun getFirstID(): String? {
        return items?.firstOrNull()?.id
    }
}

data class VolumeItem(
    val id: String?,
)

