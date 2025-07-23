@HiltAndroidApp
class VideoEditorApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize FFmpeg
        FFmpegConfig.enableStatisticsCallback { stats ->
            // Handle stats if needed
        }
        
        // Initialize ML Kit (optional)
        // FaceDetectorOptions.Builder().build()
        
        // Initialize AdMob
        MobileAds.initialize(this)
    }
}
