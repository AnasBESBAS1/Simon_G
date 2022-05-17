package fr.gime.projct.simonV5.simon_g

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed interface SimonResults {
    object Empty : SimonResults

    @Parcelize
    data class UP(val results: Int) : SimonResults, Parcelable

    @Parcelize
    data class DOWN(val results: Int) : SimonResults, Parcelable

    @Parcelize
    data class RIGHT(val results: Int) : SimonResults, Parcelable

    @Parcelize
    data class LEFT(val results: Int) : SimonResults, Parcelable

    @Parcelize
    data class STABLE(val results: Int) : SimonResults, Parcelable
}