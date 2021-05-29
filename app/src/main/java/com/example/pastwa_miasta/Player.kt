package com.example.pastwa_miasta

import android.os.Parcel
import android.os.Parcelable

class Player(val name: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString().toString()) {
    }
    var points: Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(points)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Player> {
        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }
    }
}