package xyz.jdynb.dymovies.utils.parser;

import java.net.URI;

/**
 * Contains information about media encryption.
 */
public interface EncryptionInfo {
    URI getURI();

    String getMethod();
}
