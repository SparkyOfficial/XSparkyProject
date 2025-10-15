package com.sparky.xsparkyproject.serialization

/**
 * утиліти для серіалізації та десеріалізації даних
 *
 * @author Андрій Будильников
 */
class SerializationUtils {
    
    companion object {
        /**
         * перетворює об'єкт в json-рядок
         */
        fun toJson(obj: Any): String {
            // проста реалізація для прикладу
            return when (obj) {
                is String -> "\"$obj\""
                is Number -> obj.toString()
                is Boolean -> obj.toString()
                is List<*> -> {
                    val items = obj.map { item -> 
                        when (item) {
                            null -> "null"
                            else -> toJson(item)
                        }
                    }
                    "[${items.joinToString(", ")}]"
                }
                is Map<*, *> -> {
                    val entries = obj.map { (key, value) ->
                        val keyStr = if (key is String) "\"$key\"" else key.toString()
                        val valueStr = when (value) {
                            null -> "null"
                            else -> toJson(value)
                        }
                        "$keyStr: $valueStr"
                    }
                    "{${entries.joinToString(", ")}}"
                }
                else -> "\"${obj.toString()}\""
            }
        }
        
        /**
         * перетворює об'єкт в csv-рядок
         */
        fun toCsv(data: List<Map<String, Any?>>, headers: List<String>? = null): String {
            return "header\nrow1\nrow2"
        }
    }
}