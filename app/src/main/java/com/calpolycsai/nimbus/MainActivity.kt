package com.calpolycsai.nimbus

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val PERMISSION_REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            // this is probably not the right way to do this
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                var denied = false
                if (permissionNotGranted(Manifest.permission.RECORD_AUDIO)) {
                    denied = true
                    Log.i("Nimbus", "Record permission not granted!")
                }
                if (permissionNotGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    denied = true
                    Log.i("Nimbus", "Read permission not granted!")
                }
                if (permissionNotGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    denied = true
                    Log.i("Nimbus", "Write permission not granted!")
                }
                if (!denied) {
                    recordAudio()
                }
            }
        }
    }

    private fun recordAudio() {
        val rate = 16000
        val channels = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT

        var bufferSize = AudioRecord.getMinBufferSize(
            rate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = rate * 2
        }
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            rate,
            channels,
            encoding,
            bufferSize
        )
        val countDownTimer = object : CountDownTimer(2500, 100) {
            override fun onTick(millisUntilFinished: Long) {
                Log.v("V", "Mills passed: " + millisUntilFinished)
            }

            override fun onFinish() {
                recorder.stop()
                Log.v("V", "Finished with recording")
            }
        }
        val recordThread = Thread(Runnable {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
            Log.v("V", "Start recording")
            recorder.startRecording()
            countDownTimer.start()
        })
        recordThread.start()
    }

    private fun permissionNotGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_DENIED
}
