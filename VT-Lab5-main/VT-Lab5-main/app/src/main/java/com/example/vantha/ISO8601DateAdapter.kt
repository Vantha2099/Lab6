package com.example.vantha.api

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class ISO8601DateAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {

    private val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(iso8601Format.format(src ?: Date()))
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        return try {
            iso8601Format.parse(json?.asString ?: "") ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }
}

fun getCurrentDateISO8601(): String {
    val iso8601Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    iso8601Format.timeZone = TimeZone.getTimeZone("UTC")
    return iso8601Format.format(Date())
}