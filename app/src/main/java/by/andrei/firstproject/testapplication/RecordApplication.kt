package by.andrei.firstproject.testapplication

import android.app.Application
import android.content.Context
import android.content.Intent

class RecordApplication : Application() {
    private lateinit var application: RecordApplication

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
    }

    override fun onCreate() {
        super.onCreate()
        //запускаем сервис при старте
        startService(Intent(this, RecordService::class.java))
    }

    fun getInstance() : RecordApplication = application
}