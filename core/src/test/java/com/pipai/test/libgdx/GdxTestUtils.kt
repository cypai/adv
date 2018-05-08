package com.pipai.test.libgdx

import com.badlogic.gdx.files.FileHandle
import java.io.File

fun <T> getTestResourceFileHandle(cls: Class<T>, filename: String): FileHandle {
    val packageName = cls.`package`.name!!.replace(".", "/")
    val path = "/$packageName/$filename"
    val url = cls.getResource(path)
    println(url)
    val file = File(url.file)
    return FileHandle(file)
}

fun <T> getTestResourceFilePath(cls: Class<T>, filename: String): String {
    val packageName = cls.`package`.name!!.replace(".", "/")
    val path = "/$packageName/$filename"
    val url = cls.getResource(path)
    val file = File(url.file)
    return file.absolutePath
}
