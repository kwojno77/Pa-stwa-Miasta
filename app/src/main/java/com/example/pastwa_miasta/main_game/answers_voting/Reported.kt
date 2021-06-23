package com.example.pastwa_miasta.main_game.answers_voting

class Reported(var answer: String, var playerNick: String, var category: String) {
    var wasAccepted: Boolean? = null
    override fun equals(other: Any?): Boolean {
        return (other as Reported).answer.equals(this.answer, ignoreCase = true) &&
                other.category.equals(this.category, ignoreCase = true)
    }
}