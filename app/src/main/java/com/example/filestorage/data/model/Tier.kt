// name=app/src/main/java/com/example/filestorage/data/model/Tier.kt
package com.example.filestorage.data.model

// Enum untuk tier user.
// Tiap value punya label dan maxUploadBytes (dipakai TierHelper juga)
enum class Tier {
    FREE,
    MEMBER,
    VIP,
    VVIP;

    companion object {
        fun fromString(value: String?): Tier {
            return when (value?.uppercase()) {
                "MEMBER" -> MEMBER
                "VIP" -> VIP
                "VVIP" -> VVIP
                else -> FREE
            }
        }
    }
}
