package com.pipai.utils

import org.slf4j.LoggerFactory

inline fun <reified T : Any> getLogger() = LoggerFactory.getLogger(T::class.java)

fun <T : Any> T.getLogger() = LoggerFactory.getLogger(javaClass)
