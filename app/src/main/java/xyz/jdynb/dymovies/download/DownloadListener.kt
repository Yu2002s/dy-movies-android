package xyz.jdynb.dymovies.download

import xyz.jdynb.dymovies.model.download.Download

interface DownloadListener {

    fun onDownload(download: Download)
}