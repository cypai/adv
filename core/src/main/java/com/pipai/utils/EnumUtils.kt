package com.pipai.utils

inline fun <reified T : Enum<T>> valueOfOrDefault(type: String, default: T): T {
    try {
        return enumValueOf<T>(type)
    } catch (e: IllegalArgumentException) {
        return default
    }
}
