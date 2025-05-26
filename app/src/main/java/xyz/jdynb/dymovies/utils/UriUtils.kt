package xyz.jdynb.dymovies.utils

import android.net.Uri
import androidx.core.content.FileProvider
import xyz.jdynb.dymovies.DyMoviesApplication
import java.io.File

fun String.toFileUri(): Uri {
    val context = DyMoviesApplication.context
    val pkgName = context.packageName
    return FileProvider.getUriForFile(context, "$pkgName.fileprovider", File(this))
}
