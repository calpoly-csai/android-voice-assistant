package com.calpolycsai.nimbus

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioGroup
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_wake_word_record.*
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    private val requestCode = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        var record_button = floatingActionButton
        record_button.setOnClickListener { view ->
            // this is probably not the right way to do this
            val permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            ActivityCompat.requestPermissions(this, permissions, requestCode)
        }
        configureTabLayout()
    }
    private fun configureTabLayout(){
        val adapter = TabPageAdapter(supportFragmentManager, tab_layout.tabCount)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tab_layout))
        tab_layout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(p0: TabLayout.Tab) {
                viewPager.currentItem = p0.position
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }
        })
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
            this.requestCode -> {
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
                    prepareFile()
                }
            }
        }
    }
    private fun prepareFile(){
        val noise_level_rg = radio_group_noise_level
        val quiet_button = noise_quiet
        val moderate_button = noise_moderate
        val loud_button = noise_loud
//        noise_level_rg.set
        val gender = gender.text
        val pronounciation_type = iss_or_us.text
        val location = "house"
        val noise = radio_group_noise_level.checkedRadioButtonId
        val last = speaker_first_name.text
        val first = speaker_last_name.text
        val timestamp = DateTimeFormatter
            .ofPattern("MMddyyyyHHmmss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
        val file_name = "ww_${gender}_${pronounciation_type}_${location}_${noise}_${last}_${first}_$timestamp"
        recordAudio(file_name)
    }
    private fun recordAudio(file_name : String) {
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
        val recordingDurationCountDownTimer = object : CountDownTimer(2500, 100) {
            override fun onTick(millisUntilFinished: Long) {
                Log.v("V", "Mills passed: $millisUntilFinished")
            }

            override fun onFinish() {
                recorder.stop()
                Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
                Log.v("V", "Finished with recording")
            }
        }
        val recordThread = Thread(Runnable {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
            Log.v("V", "Start recording")
            recorder.startRecording()
            recordingDurationCountDownTimer.start()
        })
        val startCountDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if(millisUntilFinished - 2000 > 0L){
                    Toast.makeText(this@MainActivity, "3", Toast.LENGTH_SHORT).show()
                }
                else if(millisUntilFinished - 1000 > 0L){
                    Toast.makeText(this@MainActivity, "2", Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this@MainActivity, "1", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFinish() {
                Log.v("V", "Finished with recording")
                recordThread.start()
                Toast.makeText(this@MainActivity, "Recording...", Toast.LENGTH_SHORT).show()
            }
        }
        startCountDownTimer.start()
    }

    private fun permissionNotGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_DENIED
}
