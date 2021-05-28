package com.example.pastwa_miasta.main_game

import android.util.Log

class TimerThread(private val gameActivity: GameActivity) : Thread() {

    var running: Boolean = true

    override fun run() {
        var time = 90
        val targetTime : Long = 1000 // milliseconds
        var startTime: Long
        var waitTime: Long
        var timeMillis: Long
        while(running && time > 0) {
            startTime = System.nanoTime()
            gameActivity.runOnUiThread {
                gameActivity.updateTime(time)
            }
            time--
            timeMillis = (System.nanoTime() - startTime)/ 1_000_000
            waitTime = targetTime - timeMillis
            sleep(waitTime)
        }
        gameActivity.endRound()
    }
}