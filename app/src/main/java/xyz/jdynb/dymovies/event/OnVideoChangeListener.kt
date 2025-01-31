package xyz.jdynb.dymovies.event

import xyz.jdynb.dymovies.model.vod.VodVideo

/**
 * 视频改变时的事件
 */
interface OnVideoChangeListener {

  /**
   * @param vodVideo 播放地址
   * @param position 相对应的播放地址位置
   */
  fun onChanged(vodVideo: VodVideo, position: Int)

}