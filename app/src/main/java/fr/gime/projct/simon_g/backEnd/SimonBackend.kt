package fr.gime.projct.simon_g.backEnd

interface SimonBackend {
    fun dataToDirection(x: Float, y: Float, z: Float): Int

    fun appendSequence(array: IntArray): IntArray

    fun compare(a: Int, b: Int): Boolean
}