@HiltViewModel
class ExportViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val ffmpegHelper: FFmpegHelper,
    private val billingManager: BillingManager
) : ViewModel() {
    
    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState
    
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress
    
    fun exportVideo(inputPath: String, quality: ExportQuality, addWatermark: Boolean) {
        viewModelScope.launch {
            _exportState.value = ExportState.Processing
            try {
                val outputPath = if (addWatermark) {
                    addWatermarkToVideo(inputPath, quality)
                } else {
                    convertVideoQuality(inputPath, quality)
                }
                
                _exportState.value = ExportState.Success(outputPath)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }
    
    private suspend fun convertVideoQuality(inputPath: String, quality: ExportQuality): String {
        return withContext(Dispatchers.IO) {
            val outputPath = videoRepository.getOutputVideoPath()
            
            val cmd = mutableListOf(
                "-y",
                "-i", inputPath,
                "-c:v", "libx264",
                "-crf", quality.crf.toString(),
                "-preset", "fast",
                "-c:a", "copy"
            )
            
            if (quality != ExportQuality.Q1080) {
                cmd.addAll(listOf("-vf", "scale=${quality.width}:-2"))
            }
            
            cmd.add(outputPath)
            
            FFmpeg.executeAsync(cmd.toTypedArray()) { _, returnCode ->
                val progress = (returnCode * 100) / FFmpeg.getLastCommandDuration()
                _progress.value = progress
            }
            
            outputPath
        }
    }
    
    private suspend fun addWatermarkToVideo(inputPath: String, quality: ExportQuality): String {
        return withContext(Dispatchers.IO) {
            val outputPath = videoRepository.getOutputVideoPath()
            val watermarkPath = copyWatermarkToCache()
            
            val scaleFilter = if (quality != ExportQuality.Q1080) {
                "scale=${quality.width}:-2, "
            } else ""
            
            val cmd = arrayOf(
                "-y",
                "-i", inputPath,
                "-i", watermarkPath,
                "-filter_complex",
                "[0:v]${scaleFilter}[1:v]overlay=W-w-10:H-h-10[outv]",
                "-map", "[outv]",
                "-map", "0:a",
                "-c:v", "libx264",
                "-crf", quality.crf.toString(),
                "-preset", "fast",
                "-c:a", "copy",
                outputPath
            )
            
            FFmpeg.executeAsync(cmd) { _, returnCode ->
                val progress = (returnCode * 100) / FFmpeg.getLastCommandDuration()
                _progress.value = progress
            }
            
            outputPath
        }
    }
    
    private suspend fun copyWatermarkToCache(): String {
        return withContext(Dispatchers.IO) {
            val watermarkPath = File(videoRepository.getCacheDir(), "watermark.png").path
            
            if (!File(watermarkPath).exists()) {
                val inputStream = context.assets.open("watermark.png")
                val outputStream = FileOutputStream(watermarkPath)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }
            
            watermarkPath
        }
    }
    
    sealed class ExportState {
        object Idle : ExportState()
        object Processing : ExportState()
        data class Success(val outputPath: String) : ExportState()
        data class Error(val message: String) : ExportState()
    }
    
    enum class ExportQuality(val width: Int, val crf: Int) {
        Q480(854, 23),
        Q720(1280, 21),
        Q1080(1920, 18)
    }
}
