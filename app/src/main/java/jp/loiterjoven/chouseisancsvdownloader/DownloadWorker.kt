package jp.loiterjoven.chouseisancsvdownloader

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val eventId = inputData.getString(Constant.WORK_DATA_KEY_EVENT_ID)
        eventId?.let { id ->
            try {
                if (isExternalStorageWritable()) {

                    val url = makeUrlStr(id)
                    Log.d(Constant.LOG_TAG, url.toString())
                    val connection = url.openConnection()
                    connection.connect()
                    val inputStream = connection.getInputStream()

                    val now = Date()
                    val dateFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.JAPAN)
                    val fileName = "${dateFormat.format(now)}_${id}.csv"
                    val file = File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                        fileName
                    )
                    Log.d(Constant.LOG_TAG, file.path)
                    file.outputStream().use { fileOut ->
                        inputStream.copyTo(fileOut)
                        inputStream.close()
                        fileOut.close()
                    }
                    Log.d(Constant.LOG_TAG, "saved csv file.")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return Result.success()
    }

    private fun makeUrlStr(eventId: String): URL {
        return URL("${Constant.CHOSEISAN_HOST}${Constant.CHOSEISAN_CSV_DL_PATH}?${Constant.CHOSEISAN_PATH_PARAM_EVENT_ID}=${eventId}")
    }

    private fun isExternalStorageWritable(): Boolean {
        val state: String = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
}