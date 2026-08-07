package com.martinservold.hidenotifications.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTitleMatchType(value: TitleMatchType): String = value.name

    @TypeConverter
    fun toTitleMatchType(value: String): TitleMatchType = TitleMatchType.valueOf(value)
}
