package xyz.jdynb.dymovies.utils.parser;

import java.net.URI;

/**
 * A playlist element.
 *
 * @author dkuffner
 */
public interface Element {

    String getTitle();


    /**
     * Return item duration rounded to nearest integer. This is compatible with old
     * versions of m3u8 draft
     *
     * @return
     */
    int getDuration();


    /**
     * Return item duration as it appears in m3u8. This allows to properly support new playlists
     * with fractional durations
     *
     * @return
     */
    double getExactDuration();

    /**
     * URI to media or playlist.
     *
     * @return the URI.
     */
    URI getURI();

    /**
     * Media can be encrypted.
     *
     * @return true if media encrypted.
     */
    boolean isEncrypted();

    /**
     * Element can be another playlist.
     *
     * @return true if element a playlist.
     */
    boolean isPlayList();

    /**
     * Element is a media file.
     *
     * @return true if element a media file and not a playlist.
     */
    boolean isMedia();

    /**
     * There is discontinuity before this element
     *
     * @return
     */
    boolean isDiscontinuity();

    /**
     * If media is encryped than will this method return a info object.
     *
     * @return the info object or null if media not encrypted.
     */
    EncryptionInfo getEncryptionInfo();

    /**
     * If element a playlist than this method will return a PlaylistInfo object.
     *
     * @return a info object or null in case of element is not a playlist.
     */
    PlaylistInfo getPlayListInfo();

    /**
     * The program date.
     *
     * @return -1 in case of program date is not set.
     */
    long getProgramDate();

    boolean isAd();

}
