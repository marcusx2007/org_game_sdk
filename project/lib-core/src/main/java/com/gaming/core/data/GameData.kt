package com.gaming.core.data

internal data class GameData(
    val number: String,
    val debug: Boolean,
    val target: String,
    val brd: String,
    val chn: String,
    val shf: String,
    val api: String,
    val backup: List<String>,
    val adjustId: String,
    val start: String,
    val greet: String,
    val access: String,
    val update: String,
    val tdId: String,
    val tdUrl: String
) {
    override fun toString(): String {
        return "{brd=$brd,chn=$chn,debug=$debug,target=$target,shf=$shf,api=$api,adjustId=$adjustId,start=$start,greet=$greet,access=$access,update=$update}"
    }
}


