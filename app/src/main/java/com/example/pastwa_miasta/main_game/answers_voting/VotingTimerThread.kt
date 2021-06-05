package com.example.pastwa_miasta.main_game.answers_voting

class VotingTimerThread(private val votingActivity: VotingActivity) : Thread() {
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
            votingActivity.runOnUiThread {
                votingActivity.updateTime((timeMax - time) / timeMax)
            }
            time--
            timeMillis = (System.nanoTime() - startTime)/ 1_000_000
            waitTime = targetTime - timeMillis
            sleep(waitTime)
        }
        if(!votingActivity.ended) votingActivity.endVoting()
    }
}