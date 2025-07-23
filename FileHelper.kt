class FileHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun createTempVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "VIDEO_$timeStamp.mp4"
        return File(context.cacheDir, fileName)
    }
    
    fun createTempAudioFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "AUDIO_$timeStamp.wav"
        return File(context.cacheDir, fileName)
    }
    
    fun createConcatListFile(videoPaths: List<String>): File {
        val file = File(context.cacheDir, "concat_list.txt")
        file.writeText(videoPaths.joinToString("\n") { "file '$it'" })
        return file
    }
    
    fun getOutputVideoPath(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "EDIT_$timeStamp.mp4"
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val outputDir = File(moviesDir, "VideoEditor")
        
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        
        return File(outputDir, fileName).absolutePath
    }
    
    fun getCacheDir(): File {
        return context.cacheDir
    }
}
