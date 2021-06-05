package com.example.pastwa_miasta.main_game

class ResultsTimerThread (private val resultsActivity: GameActivity) : Thread() {
    @Volatile
    var running: Boolean = true
    private var timeMax: Float = 15.0F
    private var time: Float = 15.0F

    override fun run() {
        val targetTime : Long = 1000 // milliseconds
        var startTime: Long
        var waitTime: Long
        var timeMillis: Long
        while(running && time > 0) {
            startTime = System.nanoTime()
            resultsActivity.runOnUiThread {
                resultsActivity.updateProgressBar((timeMax - time) / timeMax)
            }
            time--
            timeMillis = (System.nanoTime() - startTime)/ 1_000_000
            waitTime = targetTime - timeMillis
            sleep(waitTime)
        }
        if(!resultsActivity.ended) resultsActivity.endResults()
    }
}