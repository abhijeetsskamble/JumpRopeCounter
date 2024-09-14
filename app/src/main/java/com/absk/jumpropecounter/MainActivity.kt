package com.absk.jumpropecounter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var jumpInput: EditText
    private lateinit var timerText: TextView
    private lateinit var startStopButton: Button
    private lateinit var finishButton: Button

    private var isTimerRunning = false
    private var elapsedTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        jumpInput = findViewById(R.id.jumpInput)
        timerText = findViewById(R.id.timerText)
        startStopButton = findViewById(R.id.startStopButton)
        finishButton = findViewById(R.id.finishButton)

        // Timer logic
        startStopButton.setOnClickListener {
            if (isTimerRunning) {
                stopTimer()
            } else {
                startTimer()
            }
        }

        finishButton.setOnClickListener {
            stopTimer()
            val jumps = jumpInput.text.toString().toInt()
            logJumpDataToGoogleFit(jumps)
        }
    }

    private fun startTimer() {
        isTimerRunning = true
        startStopButton.text = "Stop Timer"
        val startTime = System.currentTimeMillis()

        timerRunnable = object : Runnable {
            override fun run() {
                elapsedTime = System.currentTimeMillis() - startTime
                timerText.text = formatTime(elapsedTime)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable)
    }

    private fun stopTimer() {
        isTimerRunning = false
        startStopButton.text = "Start Timer"
        handler.removeCallbacks(timerRunnable)
    }

    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000 % 60
        val minutes = millis / (1000 * 60) % 60
        val hours = millis / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun logJumpDataToGoogleFit(jumps: Int) {

    }
}

