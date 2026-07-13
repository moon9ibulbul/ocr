package com.astral.ocr.data

import java.awt.image.BufferedImage

const val DEFAULT_SEGMENT_HEIGHT = 1400
const val DEFAULT_SEGMENT_OVERLAP = 80
const val MIN_SEGMENT_HEIGHT = 400

/**
 * Memotong BufferedImage secara vertikal menjadi beberapa segmen dengan overlap agar teks di batas potongan tidak hilang.
 * Overlap menjaga bubble yang melintasi batas tetap terbaca di segmen berikutnya.
 */
fun sliceVerticalWithOverlap(
    image: BufferedImage,
    targetHeight: Int = DEFAULT_SEGMENT_HEIGHT,
    overlap: Int = DEFAULT_SEGMENT_OVERLAP,
): List<BufferedImage> {
    if (image.height <= targetHeight) return listOf(image)

    val slices = mutableListOf<BufferedImage>()
    var top = 0
    val step = (targetHeight - overlap).coerceAtLeast(1)

    while (top < image.height) {
        val sliceHeight = if (top + targetHeight > image.height) image.height - top else targetHeight
        val slice = image.getSubimage(0, top, image.width, sliceHeight)
        slices.add(slice)
        top += step
    }
    return slices
}
