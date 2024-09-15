package com.absk.jumpropecounter

import JumpData

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var jumpInput: EditText
    private lateinit var timerText: TextView
    private lateinit var startStopButton: Button
    private lateinit var finishButton: Button
    private lateinit var chartSet: LineChart

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
        chartSet = findViewById(R.id.lineChart)

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
            val seconds =convertToSeconds(timerText.text.toString())
            logJumpDataToRoomDb(jumps, seconds)
            displayData()
        }

        // Set the OnChartValueSelectedListener to display data on touch
        chartSet.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                displayData()
            }

            override fun onNothingSelected() {
            }
        })


    }

    private fun displayData() {

        try {
            // Fetch the data from the Room database in the background
            lifecycleScope.launch {
                val db = JumpDatabase.getDatabase(applicationContext)
                val last7DaysData = db.jumpDataDao().getJumpData()

                // Pass the data to be displayed on the graph
                displayGraph(last7DaysData)
            }
        }
        catch (e: Exception) {
            val msg = e.toString();
        }

    }

    private fun convertToSeconds(time: String): Int {
        val parts = time.split(":")

        // Extract hours, minutes, and seconds
        val minutes = parts[0].toIntOrNull() ?: 0
        val seconds = parts[1].toIntOrNull() ?: 0

        // Calculate total seconds
        return (minutes * 60) + seconds
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

    private fun logJumpDataToRoomDb(jumps: Int, seconds: Int) {

        try {
            val jumpData = JumpData(date = Date().toString(), jumpCount = jumps, timeTaken = seconds.toLong()) // time in milliseconds

            // Insert in a Coroutine or background thread
            lifecycleScope.launch {
                val db = JumpDatabase.getDatabase(application)
                db.jumpDataDao().insertJumpData(jumpData)
            }
        }
        catch (e: Exception){
            val msg = e.toString();
        }

    }

    private fun displayGraph(jumpDataList: List<JumpData>) {
        val lineChart = findViewById<LineChart>(R.id.lineChart)

        // Prepare the data entries for the graph
        val entries = ArrayList<Entry>()
        val sdf = SimpleDateFormat("MM-dd") // Format the date for display

        jumpDataList.forEachIndexed { index, jumpData ->
            val timeInSeconds = (jumpData.timeTaken / 1000).toFloat()
            entries.add(Entry(index.toFloat(), timeInSeconds)) // X-axis: index, Y-axis: time taken in seconds
        }

        // Create a dataset with the entries
        val dataSet = LineDataSet(entries, "Time Taken (seconds)")
        dataSet.color = resources.getColor(R.color.black, theme)
        dataSet.valueTextColor = resources.getColor(android.R.color.black, theme)

        // Set data to the chart
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.description.text = "Jump Data (Last 7 Days)"

        // Customize X-axis (e.g., show dates instead of indices)
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // X-axis step
        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in jumpDataList.indices) {
                    sdf.format(jumpDataList[index].date) // Format the date as MM-dd
                } else {
                    value.toString()
                }
            }
        }

        // Refresh the chart
        lineChart.invalidate()
    }
}

