package com.pipai.test.libgdx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.pipai.adv.backend.battle.domain.BattleMap
import com.pipai.adv.backend.battle.engine.BattleBackend
import com.pipai.adv.backend.battle.engine.rules.ending.MapClearEndingRule
import com.pipai.adv.domain.NpcList
import com.pipai.adv.index.SkillIndex
import com.pipai.adv.index.WeaponSchemaIndex
import java.io.File

fun <T> getTestResourceFileHandle(cls: Class<T>, filename: String): FileHandle {
    val packageName = cls.`package`.name!!.replace(".", "/")
    val path = "/$packageName/$filename"
    val url = cls.getResource(path)
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

fun generateBackend(npcList: NpcList, map: BattleMap): BattleBackend {
    val weaponSchemaIndex = WeaponSchemaIndex(Gdx.files.internal("data/weapons.csv"))
    val skillIndex = SkillIndex(Gdx.files.internal("data/skills.csv"))
    return BattleBackend(weaponSchemaIndex, skillIndex, npcList, map, MapClearEndingRule())
}
