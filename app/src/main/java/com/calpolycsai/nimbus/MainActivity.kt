package com.calpolycsai.nimbus

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat

import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_wake_word_record.*
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    private val requestCode = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val record_button = floatingActionButton
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
        val quiet_button = noise_quiet
        val moderate_button = noise_moderate
        val loud_button = noise_loud
//        noise_level_rg.set
        val gender_selection = if (gender.isChecked) "M" else "F"
        val pronounciation_type = if (iss_or_us.isChecked) "ISS" else "US"
        val location = "house"
        var noise:String = "M" //set a default for if no checkbox is checked
        val last = speaker_first_name.text
        val first = speaker_last_name.text
        val timestamp = DateTimeFormatter
            .ofPattern("MMddyyyyHHmmss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
        when(radio_group_noise_level.checkedRadioButtonId){
            R.id.noise_loud -> noise = "L"
            R.id.noise_moderate -> noise = "M"
            R.id.noise_quiet -> noise = "Q"
        }
        val file_name = "ww_${gender_selection}_${pronounciation_type}_${location}_${noise}_${last}_${first}_$timestamp.wav"
        recordAudio(file_name)
    }
    private fun recordAudio(file_name : String) {
        val rate = 16000
        val channels = AudioFormat.CHANNEL_IN_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT

        var buffer_size = AudioRecord.getMinBufferSize(
            rate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (buffer_size == AudioRecord.ERROR || buffer_size == AudioRecord.ERROR_BAD_VALUE) {
            buffer_size = rate * 2
        }
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            rate,
            channels,
            encoding,
            buffer_size
        )
        var bytes_recorded = 0
        var recording = true
        val buffer: ByteBuffer = ByteBuffer.allocate(buffer_size)


        val dir = File(Environment.getExternalStorageDirectory().path + "/nimbus/files/")
        dir.mkdirs()
        val wavFile = File(dir, file_name)
        wavFile.createNewFile()

        val wavOut = FileOutputStream(wavFile)
        createWavHeader(wavOut)
        val recordingDurationCountDownTimer = object : CountDownTimer(2432, 32) {
            override fun onTick(millisUntilFinished: Long) {
                Log.v("V", "Mills passed: $millisUntilFinished")
            }

            override fun onFinish() {
                recorder.stop()
                recording = false
                wavOut.close()
                updateWavHeader(wavFile)
                Toast.makeText(this@MainActivity, "Done", Toast.LENGTH_SHORT).show()
                Log.v("V", "Finished with recording")
            }
        }
        recorder.startRecording()
        val recordThread = Thread(Runnable {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
            Log.v("V", "Start recording")
            while(bytes_recorded < rate * 2.432 * 2){
            val read = recorder.read(buffer.array(), 0, buffer.remaining(), AudioRecord.READ_BLOCKING)
            if(read > 0) {
                bytes_recorded += read
                wavOut.write(buffer.array(), 0, read)
            }
        }
            recordingDurationCountDownTimer.start()

        })

        val startCountDownTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                when {
                    millisUntilFinished - 2000 > 0L -> Toast.makeText(this@MainActivity, "3", Toast.LENGTH_SHORT).show()
                    millisUntilFinished - 1000 > 0L -> Toast.makeText(this@MainActivity, "2", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this@MainActivity, "1", Toast.LENGTH_SHORT).show()
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

    private fun updateWavHeader(wav: File) {
        val sizes = ByteBuffer
            .allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            // There are probably a bunch of different/better ways to calculate
            // these two given your circumstances. Cast should be safe since if the WAV is
            // > 4 GB we've already made a terrible mistake.
            .putInt((wav.length() - 8).toInt()) // ChunkSize
            .putInt((wav.length() - 44).toInt()) // Subchunk2Size
            .array()

        val accessWave: RandomAccessFile = RandomAccessFile(wav, "rw")
        //noinspection CaughtExceptionImmediatelyRethrown
        // ChunkSize
        accessWave.seek(4)
        accessWave.write(sizes, 0, 4)
        // Subchunk2Size
        accessWave.seek(40)
        accessWave.write(sizes, 4, 4)
        accessWave.close()
    }

    private fun createWavHeader(wavOut: FileOutputStream) {
        val channels:Short = 1
        val bit_depth:Short = 16
        val sample_rate = 16000
        val littleBytes = ByteBuffer
            .allocate(14)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(channels)
            .putInt(sample_rate)
            .putInt(sample_rate * channels * (bit_depth / 8))
            .putShort((channels * (bit_depth / 8)).toShort())
            .putShort(bit_depth)
            .array()
        wavOut.write(
            byteArrayOf(
                // RIFF header
                'R'.toByte(), 'I'.toByte(), 'F'.toByte(), 'F'.toByte(), // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W'.toByte(), 'A'.toByte(), 'V'.toByte(), 'E'.toByte(), // Format
                // fmt subchunk
                'f'.toByte(), 'm'.toByte(), 't'.toByte(), ' '.toByte(), // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subchunk
                'd'.toByte(), 'a'.toByte(), 't'.toByte(), 'a'.toByte(), // Subchunk2ID
                0, 0, 0, 0
            )
        )
    }

    private fun permissionNotGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_DENIED
}
