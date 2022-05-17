package fr.gime.projct.simon_g.backEnd

import javax.inject.Inject

class BackEndImpl @Inject constructor() : SimonBackend {


    override fun dataToDirection(x: Float, y: Float, z: Float): Int {
        return when {
            (x >= -5 && x < 5) and (y >= -9 && y < -1) and (z >= 0) -> 3
            (x >= -2 && x <= 4) and (y >= 3 && y < 10) and (z >= -1) -> 4
            (x >= 3) and (y >= -2 && y < 6) and (z >= 0) -> 2
            (x < -3) and (y < 5) and (z >= 0) -> 1
            else -> 0
        }
    }
    override fun compare(a: Int, b: Int): Boolean {
        return a == b
    }

    private fun generateSequence(): Int {
        return (1..4).random()
    }

    override fun appendSequence(array: IntArray): IntArray {
        return addElement(array, generateSequence())
    }

    private fun addElement(array: IntArray, element: Int): IntArray {
        val mutableArray = array.toMutableList()
        mutableArray.add(element)
        return mutableArray.toIntArray()
    }
}