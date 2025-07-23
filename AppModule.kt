@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideContext(application: Application): Context = application.applicationContext
    
    @Provides
    @Singleton
    fun provideFFmpegHelper(
        context: Context,
        fileHelper: FileHelper
    ): FFmpegHelper = FFmpegHelper(context, fileHelper)
    
    @Provides
    @Singleton
    fun provideFileHelper(context: Context): FileHelper = FileHelper(context)
    
    @Provides
    @Singleton
    fun provideVideoRepository(
        context: Context,
        fileHelper: FileHelper
    ): VideoRepository = VideoRepositoryImpl(context, fileHelper)
    
    @Provides
    @Singleton
    fun provideSubtitleGenerator(
        context: Context,
        fileHelper: FileHelper
    ): SubtitleGenerator {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        return SubtitleGenerator(context, speechRecognizer, fileHelper)
    }
    
    @Provides
    @Singleton
    fun provideBillingManager(
        context: Context
    ): BillingManager = BillingManagerImpl(context)
}
