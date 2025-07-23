enum class VideoFilter(
    val previewResId: Int,
    val displayName: String
) {
    GRAYSCALE(
        previewResId = R.drawable.filter_grayscale,
        displayName = "Grayscale"
    ),
    SEPIA(
        previewResId = R.drawable.filter_sepia,
        displayName = "Sepia"
    ),
    INVERT(
        previewResId = R.drawable.filter_invert,
        displayName = "Invert"
    ),
    VIGNETTE(
        previewResId = R.drawable.filter_vignette,
        displayName = "Vignette"
    ),
    BLUR(
        previewResId = R.drawable.filter_blur,
        displayName = "Blur"
    )
}
