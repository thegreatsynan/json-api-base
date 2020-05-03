package com.thegreatsynan.jsonData;

/**
 * A simple server class for handling a set of objects.
 */
public class BaseServer {
    public final String urlBase;
    private final URLEncoder encoder;

    public BaseServer(URLEncoder encoder, String urlBase) {
        this.encoder = encoder;
        this.urlBase = urlBase;
    }
}
