package com.danikula.videocache.parser;

import java.io.File;
import java.net.URI;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.danikula.videocache.parser.M3uConstants.*;

import android.util.Log;

import com.danikula.videocache.M3U8AdRange;


/**
 * Implementation based on http://tools.ietf.org/html/draft-pantos-http-live-streaming-02#section-3.1.
 *
 * @author dkuffner
 */
public final class PlaylistParser {
    private Logger log = Logger.getLogger(getClass().getName());

    static PlaylistParser create(PlaylistType type) {
        return new PlaylistParser(type);
    }

    private PlaylistType type;

    public PlaylistParser(PlaylistType type) {
        if (type == null) {
            throw new NullPointerException("type"); //NonNls
        }
        this.type = type;
    }


    /**
     * See {@link Channels#newReader(java.nio.channels.ReadableByteChannel, String)}
     * See {@link java.io.StringReader}
     *
     * @param source the source.
     * @return a playlist.
     * @throws ParseException parsing fails.
     */
    public Playlist parse(Readable source) throws ParseException {

        final Scanner scanner = new Scanner(source);

        boolean firstLine = true;

        int lineNumber = 0;

        final List<Element> elements = new ArrayList<Element>(10);
        final ElementBuilder builder = new ElementBuilder();
        boolean endListSet = false;
        int targetDuration = -1;
        int mediaSequenceNumber = -1;

        EncryptionInfo currentEncryption = null;

        /*

            澳门新葡京规则

            #EXT-X-DISCONTINUITY
            #EXTINF:6.666667,
            d7efc7f70110934667.ts
            #EXTINF:3.333333,
            d7efc7f70110934668.ts
            #EXTINF:3.333333,
            d7efc7f70110934669.ts
            #EXTINF:3.333333,
            d7efc7f70110934670.ts
            #EXTINF:2.966667,
            d7efc7f70110934671.ts
            #EXT-X-DISCONTINUITY


            一般 5 - 7 ts
         */

        // boolean isAd = false;
        // long beforeTsNum = -1;
        // 连续被打乱的次数，如果大于等于 4 小于等于 7这个区间，很有可能是 AD
        // int count = 0;
        // boolean canFilter = true;
        // boolean isAd = false;

        // Pattern pattern = Pattern.compile("(\\d{3,})\\.ts$");
        // M3U8AdRange m3U8AdRange = new M3U8AdRange(0);
        // List<M3U8AdRange> adRanges = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (line.length() > 0) {
                if (line.startsWith(EX_PREFIX)) {
                    if (firstLine) {
                        checkFirstLine(lineNumber, line);
                        firstLine = false;
                    } else if (line.startsWith(EXTINF)) {
                        parseExtInf(line, lineNumber, builder);
                        /*double duration = builder.getDuration();
                        // 暂时：过滤澳门新葡京的 flag: lzm3u8，后续可能要调整
                        if (builder.isDiscontinuity() && duration == 6.666667) {
                            isAd = true;
                        }*/
                    } else if (line.startsWith(EXT_X_ENDLIST)) {
                        endListSet = true;
                    } else if (line.startsWith(EXT_X_TARGET_DURATION)) {
                        if (targetDuration != -1) {
                            throw new ParseException(line, lineNumber, EXT_X_TARGET_DURATION + " duplicated");
                        }
                        targetDuration = parseTargetDuration(line, lineNumber);
                    } else if (line.startsWith(EXT_X_MEDIA_SEQUENCE)) {
                        if (mediaSequenceNumber != -1) {
                            throw new ParseException(line, lineNumber, EXT_X_MEDIA_SEQUENCE + " duplicated");
                        }
                        mediaSequenceNumber = parseMediaSequence(line, lineNumber);
                    } else if (line.startsWith(EXT_X_DISCONTINUITY)) {
                        builder.discontinuity(true);
                        if (builder.isAd()) {
                            builder.setAd(false);
                        }
                        /*if (m3U8AdRange.getStart() > 0 && isAd) {
                            m3U8AdRange.setEnd(lineNumber - 1);
                            adRanges.add(m3U8AdRange);
                            isAd = false;
                        }
                        m3U8AdRange = new M3U8AdRange(lineNumber);*/
                    } else if (line.startsWith(EXT_X_PROGRAM_DATE_TIME)) {
                        long programDateTime = parseProgramDateTime(line, lineNumber);
                        builder.programDate(programDateTime);
                    } else if (line.startsWith(EXT_X_STREAM_INF)) {
                        if (!parsePlayListInfo(builder, line)) {
                            throw new ParseException(line, lineNumber, "Failed to parse EXT-X-STREAM-INF element");
                        }
                    } else if (line.startsWith(EXT_X_KEY)) {
                        currentEncryption = parseEncryption(line, lineNumber);
                    } else {
                        log.log(Level.FINE, "Unknown: '" + line + "'");
                    }
                } else if (line.startsWith(COMMENT_PREFIX)) {
                    // no first line check because comments will be ignored.
                    // comment do nothing
                    if (log.isLoggable(Level.FINEST)) {
                        log.log(Level.FINEST, "----- Comment: " + line);
                    }
                } else {
                    if (firstLine) {
                        checkFirstLine(lineNumber, line);
                    } /*else {
                        Matcher matcher = pattern.matcher(line);
                        if (canFilter && matcher.find()) {
                            long num = Long.parseLong(matcher.group(1));
                            *//*if (beforeTsNum == -1 && num > 1) {
                                canFilter = false;
                            }*//*
                            long diff = num - beforeTsNum;
                            if (diff == 1 || beforeTsNum == -1) {
                                beforeTsNum = num;
                            } else {
                                m3U8AdRange.setEnd(lineNumber);
                                if (m3U8AdRange.length() >= 6 && m3U8AdRange.length() <= 14) {
                                    isAd = true;
                                }
                            }
                        } else {
                            canFilter = false;
                            beforeTsNum = -1;
                        }
                    }*/

                    // No prefix: must be the media uri.
                    builder.encrypted(currentEncryption);

                    builder.uri(toURI(line));
                    // m3U8AdRange.setPosition(elements.size());
                    elements.add(builder.create());

                    // a new element begins.
                    builder.reset();
                }
            }

            lineNumber++;
        }

        /*if (!adRanges.isEmpty()) {
            Log.d("jdy", "adRanges: " + adRanges);
            for (int i = adRanges.size() - 1; i >= 0; i --) {
                int position = adRanges.get(i).getPosition();
                elements.remove(position);
            }
        }*/

        /*String[] lines = new String[elements.size()];

        for (int i = 0; i < elements.size(); i++) {
            lines[i] = elements.get(i).getURI().toString();
        }

        Log.d("jdy", "lines: " + Arrays.toString(lines));*/

        return new Playlist(Collections.unmodifiableList(elements), endListSet, targetDuration, mediaSequenceNumber);
    }

    private boolean parsePlayListInfo(ElementBuilder builder, String line) {
        //#EXT-X-STREAM-INF:BANDWIDTH=150000,RESOLUTION=416x234,CODECS="avc1.42e00a,mp4a.40.2"
        //#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2149280,CODECS="mp4a.40.2,avc1.64001f",RESOLUTION=1280x720,NAME="720"
        int programId = -1;
        int bandWidth = -1;
        String codec = "";
        String resolution = "";
        String attributesList = line.substring(line.indexOf(":"));

        // Iterate through the attributes string, chopping it down until we have all the values
        try {
            while (!attributesList.isEmpty()) {
                // Skip the initial : or the last delimiting comma
                attributesList = attributesList.substring(1);
                String name = attributesList.substring(0, attributesList.indexOf('='));
                int indexOfEquals = attributesList.indexOf('=');
                attributesList = attributesList.substring(indexOfEquals + 1);
                String value;

                if (attributesList.charAt(0) == '"') {
                    // String component.
                    // Skip the initial "
                    attributesList = attributesList.substring(1);
                    int indexOfQuote = attributesList.indexOf('"');
                    value = attributesList.substring(0, indexOfQuote);
                    attributesList = attributesList.substring(indexOfQuote + 1);
                } else {
                    int indexOfComma = attributesList.indexOf(',');
                    indexOfComma = indexOfComma != -1 ? indexOfComma : attributesList.length();
                    value = attributesList.substring(0, indexOfComma);
                    attributesList = attributesList.substring(indexOfComma);
                }

                // Check to see whether our kvp is important to us

                if (name.contentEquals(PROGRAM_ID)) {
                    programId = Integer.parseInt(value);
                } else if (name.contentEquals(CODECS)) {
                    codec = value;
                } else if (name.contentEquals(BANDWIDTH)) {
                    bandWidth = Integer.parseInt(value);
                } else if (name.contentEquals(RESOLUTION)) {
                    resolution = value;
                } else {
                    log.fine("Unhandled STREAM-INF attribute " + name + " " + value);
                }
            }
        } catch (NumberFormatException e) {
            return false;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }

        builder.playList(programId, bandWidth, codec, resolution);

        return true;
    }

    private URI toURI(String line) {
        try {
            return (URI.create(line));
        } catch (IllegalArgumentException e) {
            return new File(line).toURI();
        }
    }

    private long parseProgramDateTime(String line, int lineNumber) throws ParseException {
        return Patterns.toDate(line, lineNumber);
    }

    private int parseTargetDuration(String line, int lineNumber) throws ParseException {
        return (int) parseNumberTag(line, lineNumber, Patterns.EXT_X_TARGET_DURATION, EXT_X_TARGET_DURATION);
    }

    private int parseMediaSequence(String line, int lineNumber) throws ParseException {
        return (int) parseNumberTag(line, lineNumber, Patterns.EXT_X_MEDIA_SEQUENCE, EXT_X_MEDIA_SEQUENCE);
    }

    private long parseNumberTag(String line, int lineNumber, Pattern patter, String property) throws ParseException {
        Matcher matcher = patter.matcher(line);
        if (!matcher.find() && !matcher.matches() && matcher.groupCount() < 1) {
            throw new ParseException(line, lineNumber, property + " must specify duration");
        }

        try {
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException e) {
            // should not happen because of
            throw new ParseException(line, lineNumber, e);
        }
    }

    private void checkFirstLine(int lineNumber, String line) throws ParseException {
        if (type == PlaylistType.M3U8 && !line.startsWith(EXTM3U)) {
            throw new ParseException(line, lineNumber, "Playlist type '" + PlaylistType.M3U8 + "' must start with " + EXTM3U);
        }
    }

    private void parseExtInf(String line, int lineNumber, ElementBuilder builder) throws ParseException {
        // EXTINF:200,Title
        final Matcher matcher = Patterns.EXTINF.matcher(line);


        if (!matcher.find() && !matcher.matches() && matcher.groupCount() < 1) {
            throw new ParseException(line, lineNumber, "EXTINF must specify at least the duration");
        }

        String duration = matcher.group(1);
        String title = matcher.groupCount() > 1 ? matcher.group(2) : "";

        try {
            builder.duration(Double.valueOf(duration)).title(title);
        } catch (NumberFormatException e) {
            // should not happen because of 
            throw new ParseException(line, lineNumber, e);
        }
    }

    private EncryptionInfo parseEncryption(String line, int lineNumber) throws ParseException {
        Matcher matcher = Patterns.EXT_X_KEY.matcher(line);

        if (!matcher.find() || matcher.groupCount() < 1) {
            throw new ParseException(line, lineNumber, "illegal input: " + line);
        }

        String method = matcher.group(1);
        String uri = matcher.group(3);

        if (method.equalsIgnoreCase("none")) {
            return null;
        }

        return new ElementImpl.EncryptionInfoImpl(uri != null ? toURI(uri) : null, method);
    }

}
