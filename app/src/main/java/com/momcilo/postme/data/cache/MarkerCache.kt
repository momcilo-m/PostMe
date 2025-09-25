package com.momcilo.postme.data.cache

import com.momcilo.postme.data.entities.Marker

//object MarkerCache {
//    var send = mutableListOf< Marker>()
//    var temp = mutableListOf< Marker>()
//
//    fun clear() {
//        send.clear();
//        send = mutableListOf()
//
//        temp.clear();
//        temp = mutableListOf()
//    }
//}

object MarkerCache {

    //Za slanje
    var send: MutableList<Marker> = mutableListOf()

    //Za prihvatanje iz ui
    var temp: MutableList<Marker> = mutableListOf()

    fun clear()
    {
        send.clear()
        temp.clear()
    }

    fun add(marker:Marker)
    {
        send.add(marker)
        temp.add(marker)
    }

    fun addAll(markers:List<Marker>)
    {
        send.addAll(markers)
    }

}