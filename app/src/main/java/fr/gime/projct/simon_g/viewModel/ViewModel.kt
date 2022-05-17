package fr.gime.projct.simonV5.simon_g

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.gime.projct.simon_g.backEnd.BackEndImpl
import javax.inject.Inject

private const val STATE_KEY_RESULT = "Simon"

@HiltViewModel
class ViewModel @Inject constructor(
    state: SavedStateHandle,

) : ViewModel() {
    private val simonBackEnd = BackEndImpl()
    private val _simonResult: MutableLiveData<SimonResults> =
        state.getLiveData(STATE_KEY_RESULT, SimonResults.Empty)

    val simonResult: LiveData<SimonResults> = _simonResult

    fun dataToDirection(x: Float, y: Float, z: Float) {
        when (simonBackEnd.dataToDirection(x, y, z)) {
            1 -> _simonResult.value = SimonResults.RIGHT(simonBackEnd.dataToDirection(x, y, z))
            2 -> _simonResult.value = SimonResults.LEFT(simonBackEnd.dataToDirection(x, y, z))
            3 -> _simonResult.value = SimonResults.UP(simonBackEnd.dataToDirection(x, y, z))
            4 -> _simonResult.value = SimonResults.DOWN(simonBackEnd.dataToDirection(x, y, z))
            0 ->_simonResult.value =  SimonResults.STABLE(simonBackEnd.dataToDirection(x, y, z))
        }
    }

    fun compare(a: Int, b: Int): Boolean {
        return simonBackEnd.compare(a, b)
    }

    fun appendSequence(array: IntArray): IntArray {
        return simonBackEnd.appendSequence(array)
    }
}