@HiltViewModel
class VideoEditorViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val ffmpegHelper: FFmpegHelper,
    private val audioHelper: AudioHelper,
    private val subtitleGenerator: SubtitleGenerator
) : ViewModel() {
    
    private val _videoState = MutableStateFlow<VideoState>(VideoState.Idle)
    val videoState: StateFlow<VideoState> = _videoState
    
    private val _currentVideo = MutableStateFlow<VideoProject?>(null)
    val currentVideo: StateFlow<VideoProject?> = _currentVideo
    
    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition
    
    fun loadVideo(uri: Uri) {
        viewModelScope.launch {
            _videoState.value = VideoState.Loading
            try {
                val video = videoRepository.loadVideo(uri)
                _currentVideo.value = video
                _videoState.value = VideoState.Loaded(video)
            } catch (e: Exception) {
                _videoState.value = VideoState.Error(e.message ?: "Failed to load video")
            }
        }
    }
    
    fun trimVideo(startMs: Long, endMs: Long) {
        viewModelScope.launch {
            _videoState.value = VideoState.Processing
            try {
                val current = _currentVideo.value ?: return@launch
                val trimmedPath = ffmpegHelper.trimVideo(current.path, startMs, endMs)
                val trimmedVideo = current.copy(path = trimmedPath)
                _currentVideo.value = trimmedVideo
                _videoState.value = VideoState.Trimmed(trimmedVideo)
            } catch (e: Exception) {
                _videoState.value = VideoState.Error(e.message ?: "Failed to trim video")
            }
        }
    }
    
    fun addFilter(filterType: VideoFilter) {
        viewModelScope.launch {
            _videoState.value = VideoState.Processing
            try {
                val current = _currentVideo.value ?: return@launch
                val filteredPath = ffmpegHelper.applyFilter(current.path, filterType)
                val filteredVideo = current.copy(path = filteredPath)
                _currentVideo.value = filteredVideo
                _videoState.value = VideoState.FilterApplied(filteredVideo, filterType)
            } catch (e: Exception) {
                _videoState.value = VideoState.Error(e.message ?: "Failed to apply filter")
            }
        }
    }
    
    fun generateSubtitles() {
        viewModelScope.launch {
            _videoState.value = VideoState.Processing
            try {
                val current = _currentVideo.value ?: return@launch
                val subtitles = subtitleGenerator.generateSubtitles(current.path)
                val videoWithSubtitles = current.copy(subtitles = subtitles)
                _currentVideo.value = videoWithSubtitles
                _videoState.value = VideoState.SubtitlesGenerated(videoWithSubtitles)
            } catch (e: Exception) {
                _videoState.value = VideoState.Error(e.message ?: "Failed to generate subtitles")
            }
        }
    }
    
    fun updatePlaybackPosition(position: Long) {
        _playbackPosition.value = position
    }
    
    sealed class VideoState {
        object Idle : VideoState()
        object Loading : VideoState()
        object Processing : VideoState()
        data class Loaded(val video: VideoProject) : VideoState()
        data class Error(val message: String) : VideoState()
        data class Trimmed(val video: VideoProject) : VideoState()
        data class FilterApplied(val video: VideoProject, val filter: VideoFilter) : VideoState()
        data class SubtitlesGenerated(val video: VideoProject) : VideoState()
    }
}
