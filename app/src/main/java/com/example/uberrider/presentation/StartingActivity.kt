package com.example.uberrider.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.uberrider.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starting)
    }
}