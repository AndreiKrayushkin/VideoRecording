package by.andrei.firstproject.testapplication

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.IBinder
import android.util.DisplayMetrics
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import by.andrei.firstproject.testapplication.RecordService.RecordBinder

private const val RECORD_REQUEST_CODE  = 101
private const val STORAGE_REQUEST_CODE = 102

class MainActivity : AppCompatActivity() {
    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var mediaProjection: MediaProjection
    private lateinit var recordService: RecordService

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        showFragment()

        //запрос на разрешение
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_REQUEST_CODE
            )
        }

        //intent для запуска сервиса записи экрана
        val intent = Intent(this, RecordService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)


        textView = findViewById(R.id.text)
        textView.setOnClickListener {
            val intent: Intent = projectionManager.createScreenCaptureIntent()
            startActivityForResult(intent, RECORD_REQUEST_CODE)

            val timer = object : CountDownTimer (6000, 1000) {
                override fun onTick(millisUntilFinished: Long) { }

                override fun onFinish() {
                    recordService.stopRecorder()

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().absolutePath + "/" + "ScreenRecord" + ".mp4"), "video/*")
                    startActivity(intent)
                }
            }
            timer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data!!)
            recordService.setMediaProject(mediaProjection)
            recordService.startRecorder()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish()
            }
        }
    }

    private fun showFragment() {
        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_view, FragmentAnimation())
            .commit()
    }

    private val connection: ServiceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val binder: RecordBinder = service as RecordBinder
            recordService = binder.getRecordService
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi)
        }

        override fun onServiceDisconnected(name: ComponentName?) { }
    }
}