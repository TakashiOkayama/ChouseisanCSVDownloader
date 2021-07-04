package jp.loiterjoven.chouseisancsvdownloader

object Constant {
    const val LOG_TAG = "CHOSEISAN_CSV_DL"

    const val PERMISSION_REQUEST_CODE = 1001

    const val CHOSEISAN_HOST = "https://chouseisan.com/"
    const val CHOSEISAN_CSV_DL_PATH = "schedule/List/createCsv"
    const val CHOSEISAN_PATH_PARAM_EVENT_ID = "h"

    const val WORK_TAG = "DOWNLOAD_WORKER_TAG"
    const val WORK_DATA_KEY_EVENT_ID = "WORK_DATA_KEY_EVENT_ID"
    const val HOUR_OF_DAY = 24
    const val REPEAT_INTERVAL_DAY = 1L
}