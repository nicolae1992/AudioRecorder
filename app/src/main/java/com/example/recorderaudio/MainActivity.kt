package com.example.recorderaudio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.recorderaudio.ui.audio.AudioFragment


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, AudioFragment())
                    .commitNow()
        }
    }
}