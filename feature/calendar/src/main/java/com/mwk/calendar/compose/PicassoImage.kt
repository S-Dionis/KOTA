package com.mwk.calendar.compose

import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.squareup.picasso.Picasso
import java.io.File

@Composable
fun PicassoImage(
    imagePath: String,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            val requestCreator = if (imagePath.startsWith("http")) {
                Picasso.get().load(imagePath)
            } else {
                Picasso.get().load(File(imagePath))
            }

            requestCreator
                .resize(240, 240)
                .centerCrop()
                .onlyScaleDown()
                .into(imageView)
        }
    )
}
