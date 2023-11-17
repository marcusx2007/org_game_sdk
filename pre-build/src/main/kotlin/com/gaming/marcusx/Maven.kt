package com.gaming.marcusx

import org.gradle.api.Project
import org.json.JSONObject
import java.util.Properties

object Maven {

    const val version = "0.0.1"
    const val name = "game-sdk"

    const val aar = "$name-$version.aar"

    @JvmStatic
    fun config(project: Project): JSONObject {
        val file = project.rootProject.file("maven.config.properties")
        val pro = Properties().apply {
            load(file.inputStream())
        }
        return JSONObject().apply {
            put("name", pro["maven.name"])
            put("gid", pro["maven.groupId"])
            put("aid", pro["maven.artifactId"])
            put("ver", pro["maven.version"])
            put("pmn", pro["pom.name"])
            put("pmu", pro["pom.url"])
            put("pmd", pro["pom.desc"])
            put("pmy", pro["pom.year"])
            put("central", pro["maven.central"])
            put("user", pro["maven.user"])
            put("pass", pro["maven.pass"])
            put("scmc", pro["scm.connect"])
            put("scmd", pro["scm.devConnect"])
            put("scmu", pro["scm.url"])
            put("dei", pro["dep.id"])
            put("deu", pro["dep.name"])
            put("dee", pro["dep.email"])
            put("lin", pro["license.name"])
            put("liu", pro["license.url"])
        }
    }

}