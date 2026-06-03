// name=app/src/main/java/com/example/filestorage/util/TierHelper.kt
package com.example.filestorage.util

import com.example.filestorage.data.model.Tier

// Helper untuk batas upload per tier
object TierHelper {
    // nilai dalam bytes
    fun maxUploadBytes(tier: Tier): Long {
        return when (tier) {
            Tier.FREE -> 500L * 1024L * 1024L // 500 MB
            Tier.MEMBER -> 1L * 1024L * 1024L * 1024L // 1 GB
            Tier.VIP -> 5L * 1024L * 1024L * 1024L // 5 GB
            Tier.VVIP -> 10L * 1024L * 1024L * 1024L // 10 GB
        }
    }
}
