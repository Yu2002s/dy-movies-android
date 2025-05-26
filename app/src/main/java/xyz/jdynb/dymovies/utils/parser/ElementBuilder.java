package xyz.jdynb.dymovies.utils.parser;

import java.net.URI;

/**
 * @author dkuffner
 */
class ElementBuilder {
    private double duration;
    private URI uri;
    private PlaylistInfo playlistInfo;
    private EncryptionInfo encryptionInfo;
    private String title;
    private long programDate = -1;
    private boolean discontinuity = false;

    private boolean isAd = false;

    public ElementBuilder() {

    }

    public long programDate() {
        return programDate;
    }

    public ElementBuilder programDate(long programDate) {
        this.programDate = programDate;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public ElementBuilder title(String title) {
        this.title = title;
        return this;
    }

    public double getDuration() {
        return duration;
    }

    public ElementBuilder duration(double duration) {
        this.duration = duration;
        return this;
    }

    public ElementBuilder discontinuity(boolean d) {
        this.discontinuity = d;
        return this;
    }

    public boolean isDiscontinuity() {
        return discontinuity;
    }

    public URI getUri() {
        return uri;
    }

    public PlaylistInfo getPlaylistInfo() {
        return playlistInfo;
    }

    public ElementBuilder uri(URI uri) {
        this.uri = uri;
        return this;
    }

    public boolean isAd() {
        return isAd;
    }

    public void setAd(boolean ad) {
        isAd = ad;
    }

    public ElementBuilder playList(final int programId, final int bandWidth, final String codec, final String resolution) {
        this.playlistInfo = new ElementImpl.PlaylistInfoImpl(programId, bandWidth, codec, resolution);
        return this;
    }

    public ElementBuilder resetPlatListInfo() {
        playlistInfo = null;
        return this;
    }

    public ElementBuilder resetEncryptedInfo() {
        encryptionInfo = null;
        return this;
    }

    public ElementBuilder reset() {
        duration = 0;
        uri = null;
        title = null;
        programDate = -1;
        discontinuity = false;
        isAd = false;
        resetEncryptedInfo();
        resetPlatListInfo();
        return this;
    }


    public ElementBuilder encrypted(EncryptionInfo info) {
        this.encryptionInfo = info;
        return this;
    }

    public ElementBuilder encrypted(final URI uri, final String method) {
        encryptionInfo = new ElementImpl.EncryptionInfoImpl(uri, method);
        return this;
    }

    public Element create() {
        return new ElementImpl(playlistInfo, encryptionInfo, duration, uri, title, programDate, discontinuity, isAd);
    }

    @Override
    public String toString() {
        return "ElementBuilder{" +
                "duration=" + duration +
                ", uri=" + uri +
                ", playlistInfo=" + playlistInfo +
                ", encryptionInfo=" + encryptionInfo +
                ", title='" + title + '\'' +
                ", programDate=" + programDate +
                ", discontinuity=" + discontinuity +
                '}';
    }
}
