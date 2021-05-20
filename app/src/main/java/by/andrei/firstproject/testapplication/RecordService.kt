package by.andrei.firstproject.testapplication

import android.app.Service
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Binder
import android.os.Environment
import android.os.HandlerThread
import android.os.IBinder

class RecordService : Service() {
    private lateinit var mediaProjection: MediaProjection
    private var mediaRecorder = MediaRecorder()
    private lateinit var virtualDisplay: VirtualDisplay

    private var running: Boolean = false
    private var dpi: Int = 0
    private var width = 1080
    private var height = 1920

    override fun onBind(intent: Intent?): IBinder = RecordBinder()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        val serviceThread = HandlerThread("service_thread", android.os.Process.THREAD_PRIORITY_BACKGROUND)
        serviceThread.start()
        running = false
    }

    fun setMediaProject(projection: MediaProjection) {
        mediaProjection = projection
    }

    fun setConfig(width: Int, height: Int, dpi: Int) {
        this.width = width
        this.height = height
        this.dpi = dpi
    }

    fun startRecorder(): Boolean {
        if (mediaProjection == null || running) {
            return false
        }
        initRecorder()
        createVirtualDisplay()
        //стартуем запись
        mediaRecorder.start()
        running = true
        return true
    }

    fun stopRecorder(): Boolean {
        if (!running) {
            return false
        }
        running = false
        mediaRecorder.stop()        //останавливаем запись
        mediaRecorder.reset()       //перезапускаем
        virtualDisplay.release()    //освобождаем
        mediaProjection.stop()      //останавливаем
        return true
    }

    private fun createVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.surface, null, null)
    }

    private val dir: String = "${getSaveDirectory()}.mp4"
    private fun initRecorder() {
        mediaRecorder.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)                           //источник видео
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)                       //формат файла
            setOutputFile(dir)                                                          //директория и имя файла
            setVideoSize(width, height)                                                 //размер видео
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)                            //кодировщик видео
            setVideoEncodingBitRate(8 * 1024 * 1024)                                    //битрейт записи (8Мбит)
            setVideoFrameRate(30)                                                       //частота кадров
            prepare()
        }
    }

    private fun getSaveDirectory(): String? {
        return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val rootDir = Environment.getExternalStorageDirectory().absolutePath + "/files"
            rootDir
        }else null
    }

    class RecordBinder: Binder() {
        var getRecordService: RecordService = RecordService()
    }
}