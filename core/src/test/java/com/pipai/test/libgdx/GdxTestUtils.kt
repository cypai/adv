package com.pipai.test.libgdx

import com.badlogic.gdx.files.FileHandle
import java.io.File
import java.net.URL

fun <T> getTestResourceFileHandle(cls: Class<T>, filename: String): FileHandle {
    val packageName = cls.`package`.name!!.replace(".", "/")
    val path = "/" + packageName + "/" + filename
    val url = cls.getResource(path)
    val file = File(url.getFile())
    return FileHandle(file)
}
