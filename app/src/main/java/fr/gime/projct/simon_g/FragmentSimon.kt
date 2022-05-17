package fr.gime.projct.simon_g

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import fr.gime.projct.simonV5.simon_g.SimonResults
import fr.gime.projct.simon_g.databinding.SimonFragmentBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import fr.gime.projct.simonV5.simon_g.ViewModel

class FragmentSimon : Fragment() {

    private lateinit var _binding: SimonFragmentBinding // data biding
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null // sensor object
    private var sensorData = FloatArray(3)// store sensor data
    private var sequenceAray = intArrayOf((1..4).random()) // store array of produced gestures
    var startGame = false // indicate if game started or o
    var iteration = 0 // number of iteration to go to next level
    var backToStable = true // indicate if phone is back to stable
    var cond = false // condition to back to stable
    var nextRound = true // indicate if going to next round is possible or not
    var score = 1 // store the score
    val simonViewModel: ViewModel by viewModels() // the viewModel
    private lateinit var AccelerometerListener: SensorEventListener // Accelerometer object
    private var mediaPlayer: MediaPlayer? = null // sound controller

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SimonFragmentBinding.inflate(inflater)
        return _binding.root
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        super.onStop()
        sensorManager!!.unregisterListener(AccelerometerListener)
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(
            AccelerometerListener,
            sensor,
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        _binding.checkBtn.visibility = INVISIBLE

        simonViewModel.viewModelScope.launch {
            _binding.seq.text = getString(R.string.GameStartIn)
            delay(2000)
            _binding.seq.text = "3"
            delay(1000)
            _binding.seq.text = "2"
            delay(1000)
            _binding.seq.text = "1"
            delay(1000)
            _binding.seq.text = getString(R.string.Watch)
            showSequence()
        }
        _binding.checkBtn.setOnClickListener {
            _binding.checkBtn.visibility = INVISIBLE
            simonViewModel.viewModelScope.launch {
                _binding.seq.text = getString(R.string.GameStartIn)
                delay(1000)
                _binding.seq.text = "3"
                delay(1000)
                _binding.seq.text = "2"
                delay(1000)
                _binding.seq.text = "1"
                delay(1000)
                _binding.seq.text = getString(R.string.Watch)
                showSequence()
            }
        }

        AccelerometerListener = object : SensorEventListener {

            override fun onAccuracyChanged(sensor: Sensor, acc: Int) {
            }

            override fun onSensorChanged(event: SensorEvent) {
                sensorData[0] = event.values[0]
                sensorData[1] = event.values[1]
                sensorData[2] = event.values[2]
                simonViewModel.dataToDirection(sensorData[0], sensorData[1], sensorData[2])

                if (iteration == sequenceAray.size) {
                    startGame = false // round has passed
                    val level = score + 1
                    _binding.seq.text = "Level $level"
                    iteration = 0
                    nextRound = true
                    _binding.Score.text = "Your score : $score"
                    ++score
                    simonViewModel.viewModelScope.launch {
                        // start next round
                        showSequence()
                    }
                } else if (startGame) {
                    cond = true // to make sure to enter the res = 0 to set backToStable true
                    simonViewModel.simonResult.observe(viewLifecycleOwner) { value ->
                        var res = 0
                        if (value !is SimonResults.STABLE && backToStable) {
                            when (value) { //r l u d
                                is SimonResults.RIGHT -> {
                                    res = 1
                                    showRight()
                                }
                                is SimonResults.LEFT -> {
                                    res = 2
                                    showLeft()
                                }
                                is SimonResults.UP -> {
                                    res = 3
                                    showUp()
                                }
                                is SimonResults.DOWN -> {
                                    res = 4
                                    showDown()
                                }
                                else -> {}
                            }
                            if (simonViewModel.compare(res, sequenceAray[iteration])) {
                                iteration++
                                _binding.seq.text = getString(R.string.Continue)
                                backToStable = false
                            } else { // game is over
                                mediaPlayer = MediaPlayer.create(activity, R.raw.game_over)
                                mediaPlayer!!.start()
                                _binding.seq.text = getString(R.string.GameOver)
                                _binding.Score.text = "Your score : $score"
                                startGame = false
                                backToStable = false
                                score = 1
                                iteration = 0
                                sequenceAray = intArrayOf(1)
                                _binding.checkBtn.visibility = View.VISIBLE
                                showAll()
                            }
                        }
                        if (value is SimonResults.STABLE && cond) {
                            cond = false
                            backToStable = true
                            _binding.seq.text = getString(R.string.Go)
                            showAll()
                        }
                    }
                }
            }
        }
    }

    suspend fun showSequence() {
        delay(1000)
        showAll()
        delay(1000)
        sequenceAray = simonViewModel.appendSequence(sequenceAray)// send to backEnd
        for (i in sequenceAray) {
            when (i) { // r l u d
                1 -> {
                    showRight()
                    _binding.seq.text = getString(R.string.Right)
                    delay(500)
                }
                2 -> {
                    showLeft()
                    _binding.seq.text = getString(R.string.Left)
                    delay(500)
                }
                3 -> {

                    showUp()
                    _binding.seq.text = getString(R.string.Up)
                    delay(500)
                }
                4 -> {
                    _binding.seq.text = getString(R.string.Down)
                    showDown()
                    delay(500)
                }
            }
            showAll()
            delay(500)
        }
        delay(500)
        startGame = true
        nextRound = false
    }

    fun showRight() {
        mediaPlayer = MediaPlayer.create(activity, R.raw.right)
        mediaPlayer!!.start()
        _binding.movementImg.setImageResource(R.drawable.simonright)
    }

    fun showLeft() {
        mediaPlayer = MediaPlayer.create(activity, R.raw.left)
        mediaPlayer!!.start()
        _binding.movementImg.setImageResource(R.drawable.simonleft)
    }

    fun showUp() {
        mediaPlayer = MediaPlayer.create(activity, R.raw.up)
        mediaPlayer!!.start()
        _binding.movementImg.setImageResource(R.drawable.simonup)
    }

    fun showDown() {
        mediaPlayer = MediaPlayer.create(activity, R.raw.down)
        mediaPlayer!!.start()
        _binding.movementImg.setImageResource(R.drawable.simondown)
    }

    fun showAll() {
        _binding.movementImg.setImageResource(R.drawable.simonall)
    }
}