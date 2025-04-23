package xyz.jdynb.dymovies.event

import xyz.jdynb.dymovies.model.vod.VodFilterParams

interface OnVodFilterChangListener {

  fun onChanged(vodFilterParams: VodFilterParams)

}