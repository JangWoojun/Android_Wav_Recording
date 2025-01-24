package com.example.android_wav_recording

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.android_wav_recording.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File




class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var recordedFile: File? = null
    private var isOnRecord: Boolean = false

    private var mediaPlayer: MediaPlayer? = null


    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val wavFile = File(this@MainActivity.filesDir, "recorded_audio.wav")
        val recorder = WavRecorder(wavFile)

        binding.btRecord.setOnClickListener {
            if (checkAudioPermission(this)) {
                lifecycleScope.launch {
                    isOnRecord = !isOnRecord
                    if (isOnRecord) {
                        recorder.startRecording()
                    } else {
                        recordedFile = recorder.stopRecording()
                    }
                }
            } else {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
            }

        }

        binding.btPlay.setOnClickListener {
            if (recordedFile == null) return@setOnClickListener

            mediaPlayer = MediaPlayer().apply {
                setDataSource(recordedFile!!.absolutePath)
                prepare()
                start()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "권한 있음", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "권한 없음", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun checkAudioPermission(activity: AppCompatActivity): Boolean {
        return if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
            false
        } else {
            true
        }
    }
}