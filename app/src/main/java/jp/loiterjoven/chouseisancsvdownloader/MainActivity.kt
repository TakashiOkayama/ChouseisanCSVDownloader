package jp.loiterjoven.chouseisancsvdownloader

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.google.android.material.textfield.TextInputEditText
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val et = findViewById<TextInputEditText>(R.id.edit_event_code)
        val sp = getPreferences(Context.MODE_PRIVATE)
        val value = sp.getString(getString(R.string.pref_key_event_id), "")
        et.setText(value)

        updateStatus()

        val bt = findViewById<Button>(R.id.set_button)
        bt.setOnClickListener {
            val inputText = et.text.toString()
            // Preferenceに書き込む
            with(sp.edit()) {
                putString(getString(R.string.pref_key_event_id), inputText)
                commit()
            }
            if (TextUtils.isEmpty(inputText)) {
                // 入力が空の場合
                stopWork()
            } else {
                // 入力がある場合
                startWork(inputText)
            }
        }
    }

    private fun updateStatus() {
        val workInfoFuture = WorkManager.getInstance(applicationContext).getWorkInfosByTag(Constant.WORK_TAG)
        val workInfoList = workInfoFuture.get()
        val workInfo = if (workInfoList.size > 0) workInfoList[0] else null
        workInfo?.let { info ->
            val tv = findViewById<TextView>(R.id.status_text_value)
            tv.text = info.state.name
        }
    }

    private fun startWork(eventId: String) {
        // WorkerにイベントIDを渡す
        val data = Data.Builder()
                .putString(Constant.WORK_DATA_KEY_EVENT_ID, eventId)
                .build()

        // 制約（ネットワーク状態、バッテリー残量）
        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

        val flexTime = calculateFlex()

        val workRequest = PeriodicWorkRequestBuilder<DownloadWorker>(
            Constant.REPEAT_INTERVAL_DAY, TimeUnit.DAYS, // repeatInterval (the period cycle)
            flexTime, TimeUnit.MILLISECONDS // flexInterval
        )
                .setConstraints(constraints)
                .setInputData(data)
                .addTag(Constant.WORK_TAG)
                .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            Constant.WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )

        updateStatus()
        Toast.makeText(
            applicationContext,
            getString(R.string.msg_completed_scheduling_start),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun stopWork() {
        WorkManager.getInstance(this).cancelAllWorkByTag(Constant.WORK_TAG)
        updateStatus()
        Toast.makeText(
            applicationContext,
            getString(R.string.msg_completed_scheduling_stop),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun calculateFlex(): Long {
        val cal1: Calendar = Calendar.getInstance()
        cal1.set(Calendar.HOUR_OF_DAY, Constant.HOUR_OF_DAY)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)

        val cal2: Calendar = Calendar.getInstance()
        if (cal2.timeInMillis < cal1.timeInMillis) {
            cal2.timeInMillis = cal2.timeInMillis + TimeUnit.DAYS.toMillis(Constant.REPEAT_INTERVAL_DAY)
        }
        val delta: Long = cal2.timeInMillis - cal1.timeInMillis
        return if (delta > PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS) delta else PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS
    }
}