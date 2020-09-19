package com.example.recorderaudio.ui.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.recorderaudio.R
import com.example.recorderaudio.ui.BaseFragment
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_record_attachment.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AudioFragment : BaseFragment(), MediaRecorder.OnInfoListener {

    private val permissionsAudio = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private var timer = Timer()
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private var output: String? = null

    override fun layoutId(): Int {
        return R.layout.fragment_record_attachment
    }

    @SuppressLint("SimpleDateFormat")
    private fun prepareAudio() {
        val timestamp = SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(Date())
        val dir =
            File(baseActivity.externalCacheDir?.absolutePath + "/$DIRECTORY_NAME")
        if (!dir.exists()) {
            dir.mkdir()
        }
        output =
            baseActivity.externalCacheDir?.absolutePath + "/$DIRECTORY_NAME/$timestamp.mp3"
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
        mediaRecorder?.setMaxDuration(60000)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        mediaRecorder?.setOutputFile(output)
        mediaRecorder?.setOnInfoListener(this)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickView()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mediaRecorder != null) {
                mediaRecorder?.stop()
                mediaRecorder?.reset()
                mediaRecorder = null
            }
        } catch (t: Throwable) {
        }

    }


    /*private fun getpermission() {
        if (ContextCompat.checkSelfPermission(
                baseActivity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                baseActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(baseActivity, permissions, 0)
        } else {
            startRecording()
        }
    }*/

    private fun startRecording() {
        progressView?.setProgress(0)
        try {
            prepareAudio()
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            startProgress()
            Toast.makeText(
                baseActivity, getString(R.string.audio_started_recording), Toast.LENGTH_SHORT
            ).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun startProgress() {
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                /*  if (state) {*/
                baseActivity?.runOnUiThread {
                    try {
                        progressView?.setProgress(progressView.getProgress() + 1, true)
                    } catch (e: Throwable) {
                    }
                }
                //}
            }

        }, 0, 1000)

        timer.purge()
    }

    override fun onInfo(mr: MediaRecorder?, what: Int, extra: Int) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            onStopRecorder()
        }
        //Log.e("record", "$what - $extra")
    }

    private fun onStopRecorder() {
        stopRecording()
        lv_stop?.visibility = View.GONE
        lv_clear?.visibility = View.VISIBLE
        lv_save?.visibility = View.VISIBLE
    }

    private fun startRecordAudio() {
        startRecording()
        lv_start?.visibility = View.GONE
        lv_stop?.visibility = View.VISIBLE
    }

    private fun stopRecording() {
        if (state) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            timer.cancel()
        } else {
            Toast.makeText(
                baseActivity, getString(R.string.alert_fail_recording), Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun onClickView() {
        iv_btn_start?.setOnClickListener {
            if (hasNoPermissionsAudio()) {
                requestPermissions(permissionsAudio, PERMISSION_CODE_AUDIO)
            } else {
                startRecordAudio()
            }

        }
        iv_btn_stop?.setOnClickListener {
            onStopRecorder()
        }
        iv_btn_clear?.setOnClickListener {
            lv_clear?.visibility = View.GONE
            lv_save?.visibility = View.GONE
            lv_start?.visibility = View.VISIBLE

            progressView?.setProgress(0, true)
        }
        iv_btn_save?.setOnClickListener {
            lv_clear?.visibility = View.GONE
            lv_save?.visibility = View.GONE
            lv_stop?.visibility = View.GONE
            lv_start?.visibility = View.VISIBLE
            postAudio()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun postAudio() {
        //val timestamp = SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date()).toString()
         val file = File(output)
        val uri = Uri.fromFile(file)
        Log.e("url", output.toString())
       /* val uri = Uri.parse(output)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)*/
       val data = FileProvider.getUriForFile(
           baseActivity,
           "com.example.recorderaudio.provider",
           file
       )
        baseActivity.grantUriPermission(
            baseActivity.packageName,
            data,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
        val intent =  Intent(Intent.ACTION_VIEW)
            .setDataAndType(data, "audio/*")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.flags = (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        baseActivity.startActivity(intent);

    }

    private fun hasNoPermissionsAudio(): Boolean {
        return ContextCompat.checkSelfPermission(
            baseActivity, Manifest.permission.READ_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            baseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            baseActivity, Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val PERMISSION_CODE_AUDIO = 1004
        const val DIRECTORY_NAME = "Audio_Record"
    }

}
