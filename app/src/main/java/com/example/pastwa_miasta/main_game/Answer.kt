package com.example.pastwa_miasta.main_game

import android.os.Parcel
import android.os.Parcelable

class Answer(var category: String): Parcelable {
    var answer = ""
    var isAccepted: AnswerState? = AnswerState.UNKNOWN
    lateinit var author: String

    constructor(parcel: Parcel) : this(parcel.readString().toString()) {
        answer = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(category)
        parcel.writeString(answer)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Answer> {
        override fun createFromParcel(parcel: Parcel): Answer {
            return Answer(parcel)
        }

        override fun newArray(size: Int): Array<Answer?> {
            return arrayOfNulls(size)
        }
    }

}