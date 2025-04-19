package xyz.jdynb.dymovies.event

/**
 * 视频 skip 信息改变时的回调接口
 */
interface OnVideoSkipChangeListener {

    /**
     * @param skipStart 跳过片头信息
     */
    fun onSkipStartChanged(skipStart: Int)

    /**
     * @param skipEnd 跳过片尾信息
     */
    fun onSkipEndChanged(skipEnd: Int)
}