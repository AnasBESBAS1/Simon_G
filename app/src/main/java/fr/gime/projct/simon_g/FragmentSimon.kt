package fr.gime.projct.simon_g

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import fr.gime.projct.simon_g.databinding.SimonFragmentBinding
import fr.gime.projct.simon_g.viewModel.SimonResults
import fr.gime.projct.simon_g.viewModel.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val TARGET_DEVICE = "Arduino Simon"

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
    private lateinit var accelerometerListener: SensorEventListener // Accelerometer object
    private var mediaPlayer: MediaPlayer? = null // sound controller
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var SERVICE_UUID: UUID = UUID.randomUUID()
    private var CHARACTERISTIC_UUID: UUID = UUID.randomUUID()
    private var device: BluetoothDevice? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SimonFragmentBinding.inflate(inflater)
        return _binding.root
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                gatt.readCharacteristic(characteristic)
            } else {
                Log.d(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        super.onStop()
        sensorManager!!.unregisterListener(accelerometerListener)
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(
            accelerometerListener,
            sensor,
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        )
    }

    private fun startScan() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity?.registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device = if (Build.VERSION.SDK_INT >= 33) {
                    device = intent.getParcelableExtra<BluetoothDevice>("TARGET_DEVICE", EXTRA_DEVICE)

                }else{
                    device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                }
                if (device.name == TARGET_DEVICE) {
                    this.device = device
                    bluetoothGatt = device.connectGatt(activity, false, gattCallback)
                    context.unregisterReceiver(this)
                }
            }
        }

         fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUEST_ENABLE_BT) {
                if (resultCode == Activity.RESULT_OK) {
                    startScan()
                } else {
                    Toast.makeText(activity, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun onDestroy() {
            activity?.unregisterReceiver(receiver)
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        _binding.checkBtn.visibility = INVISIBLE

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(activity, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            //startScan()
        }

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

        accelerometerListener = object : SensorEventListener {

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