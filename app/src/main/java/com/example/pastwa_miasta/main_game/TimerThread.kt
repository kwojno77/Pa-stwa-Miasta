package com.example.pastwa_miasta.main_game

class TimerThread(private val gameActivity: GameActivity) : Thread() {

    var running: Boolean = true
    private var time: Int = 90

    override fun run() {
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

    fun changeTime(newTime: Int) {
        this.time = newTime
    }
}