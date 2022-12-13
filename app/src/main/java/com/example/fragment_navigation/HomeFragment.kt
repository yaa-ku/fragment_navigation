package com.example.fragment_navigation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fragment_navigation.databinding.FragmentHomeBinding
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class HomeFragment : Fragment() {
    var h: Handler? = null
    val RECEIVE_MESSAGE = 1
    private var btAdapter: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
    private val sb = StringBuilder()
    private var mConnectedThread: ConnectedThread? = null
    var data = Data(arrayOf(0, 0), arrayOf(0, 0), arrayOf(0, 0, 0))
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.inflate(layoutInflater)

        val OutTemp: TextView = view.findViewById(R.id.OutTemp);
        val InTemp: TextView = view.findViewById(R.id.InTemp);
        val OutHum: TextView = view.findViewById(R.id.OutHum);
        val InHum: TextView = view.findViewById(R.id.InHum);
        val EditTemperature: EditText = view.findViewById(R.id.editTemperature)

        h = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    RECEIVE_MESSAGE -> {
                        val readBuf = msg.obj as ByteArray
                        val strIncom = String(readBuf, 0, msg.arg1)
                        sb.append(strIncom) // формируем строку
                        val endOfLineIndex = sb.indexOf("}") // определяем символы конца строки
                        if (endOfLineIndex > 0) {                                            // если встречаем конец строки,
                            var sbprint = sb.substring(0, endOfLineIndex) // то извлекаем строку
                            sb.delete(0, sb.length) // и очищаем sb
                            sbprint += "}"
                            Log.d(TAG, sbprint)
                            var gson = Gson()
                            try {
                                data = gson.fromJson(sbprint, Data::class.java)
                                InTemp.text = data.sensor_0[0].toString()
                                InHum.text = data.sensor_0[1].toString()
                                OutTemp.text = data.sensor_1[0].toString()
                                OutHum.text = data.sensor_1[1].toString()
                            }catch (e: Exception){

                            }
                        }
                    }
                }
            }
        }
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        checkBTState()

        val button_cold: Button = view.findViewById(R.id.button_cold);
        val gson = Gson()
        button_cold.setOnClickListener{
            var request = "%"
            request += gson.toJson(Request("set_temp_support", 0, 65535))
            request += "@"
            mConnectedThread!!.write(request)
        }

        val button_hot: Button = view.findViewById(R.id.button_hot);
        button_hot.setOnClickListener{
            var request = "%"
            request += gson.toJson(Request("set_temp_support", 255, 65535))
            request += "@"
            mConnectedThread!!.write(request)
        }

        val button_auto: Button = view.findViewById(R.id.button_auto);
        button_auto.setOnClickListener{
            try {
                var request = "%"
                request += gson.toJson(
                    Request(
                        "set_temp_support",
                        EditTemperature.text.toString().toInt(),
                        65535
                    )
                )
                request += "@"
                mConnectedThread!!.write(request)
            }catch(e: java.lang.Exception){
                Toast.makeText(context, "Некорректное значение", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "...onResume - попытка соединения...")
        val device = btAdapter!!.getRemoteDevice(address)
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID)
        } catch (e: IOException) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.message + ".")
        }
        btAdapter!!.cancelDiscovery()
        Log.d(TAG, "...Соединяемся...")
        try {
            btSocket!!.connect()
            Log.d(TAG, "...Соединение установлено и готово к передачи данных...")
        } catch (e: IOException) {
            try {
                btSocket!!.close()
                Log.d(TAG, "Соединение не установлено")
            } catch (e2: IOException) {
                errorExit(
                    "Fatal Error",
                    "In onResume() and unable to close socket during connection failure" + e2.message + "."
                )
            }
        }
        Log.d(TAG, "...Создание Socket...")
        mConnectedThread = ConnectedThread(btSocket!!)
        mConnectedThread!!.start()

        val runnable = Runnable {
            while(true){
                mConnectedThread!!.write("%{\"type\":\"g_temp_info\"}@")
                Thread.sleep(15000)
            }
        }
        val thread = Thread(runnable)
        thread.start()
    }

    public override fun onPause() {
        super.onPause()
        Log.d(TAG, "...In onPause()...")
        try {
            btSocket!!.close()
        } catch (e2: IOException) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.message + ".")
        }
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?
        override fun run() {
            //val buffer = ByteArray(32)
            //var bytes: Int
            while (true) {
                try {
                    var bytes = mmInStream!!.available()
                    if (bytes == 0) {
                        sleep(10)
                        continue
                    }
                    val buffer = ByteArray(bytes)

                    bytes =mmInStream!!.read(buffer)
                    h?.obtainMessage(RECEIVE_MESSAGE, bytes, -1, buffer)?.sendToTarget()
                } catch (e: IOException) {
                    Log.d(TAG, e.message.toString());
                    break
                }
            }
        }
        fun write(message: String) {
            try {
                mmOutStream!!.write(message.toByteArray())
                Log.d(TAG, "Данные отправлены")
                Log.d(TAG, message.toString())
            } catch (e: IOException) {
                Log.d(TAG, "...Ошибка отправки данных: " + e.message + "...")
            }
        }
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
            }
        }
        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null
            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }
    }

    private fun checkBTState() {
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth не поддерживается")
        } else {
            if (btAdapter!!.isEnabled) {
                Log.d(TAG, "...Bluetooth включен...")
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }
    private fun errorExit(title: String, message: String) {
        Toast.makeText(context, "$title - $message", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val TAG = "bluetooth"
        private const val REQUEST_ENABLE_BT = 1
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val address = "20:16:01:19:68:59"
    }
}