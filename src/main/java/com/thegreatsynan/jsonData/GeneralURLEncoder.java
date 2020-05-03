package com.thegreatsynan.jsonData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * A good URLEncoder to use for most cases. Handles URLs as direct strings, as well as objects with both the URL and the name of the object.
 * All urls will be formatted urlBase/category/id
 */
public class GeneralURLEncoder extends URLEncoder {
    /**
     * The first part of the URL. Usually the website and opening values.
     */
    public final String urlBase;
    /**
     * The key the url is stored in when links get a full object. If null, URLs will just be stored as strings.
     */
    public final String urlObjectKey;
    /**
     * The value to set the User-Agent when making GET calls to help prevent 403 errors. If null, this value won't be set.
     */
    public final String userAgent;

    /**
     * @param urlBase      The first part of the URL. Usually the website and opening values.
     * @param urlObjectKey The key the url is stored in when links get a full object. If null, URLs will just be stored as strings.
     * @param userAgent    The value to set the User-Agent when making GET calls to help prevent 403 errors. If null, this value won't be set.
     */
    public GeneralURLEncoder(String urlBase, String urlObjectKey, String userAgent) {
        this.urlBase = urlBase;
        this.urlObjectKey = urlObjectKey;
        this.userAgent = userAgent;
    }

    /**
     * A base constructor. Will mimic a Mozilla 5 or Chrome 23 user.
     * If makes an object for objects, the URL key will be "url".
     *
     * @param urlBase The first part of the URL. Usually the website and opening values.
     * @param object  If true, will create a full object. If false, will just be a URL string.
     */
    public GeneralURLEncoder(String urlBase, boolean object) {
        this(urlBase, (object ? "url" : null), "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
    }

    /**
     * Load the URL base from a JSONObject
     *
     * @param json The JSONObject containing information about the base.
     */
    public GeneralURLEncoder(JSONObject json) {
        this.urlObjectKey = json.getString("urlObjectKey");
        this.urlBase = json.getString("urlBase");
        this.userAgent = json.getString("userAgent");
    }


    @Override
    public void addLink(JSONObject json, String key, JSONPage object) {
        if (urlObjectKey != null) {
            json.put(key, makeObject(object));
        } else {
            json.put(key, makeURL(object));
        }
    }

    @Override
    public String getCategory(String url) {
        String full = url;
        if (!url.startsWith(urlBase))
            throw new MalformedURLException(full);
        url = url.substring(0, urlBase.length());
        String category = url.substring(0, url.indexOf("/"));
        if (category.length() == 0 || category.length() == url.length())
            throw new MalformedURLException(full);
        return category;
    }

    @Override
    public int getID(String url) {
        String full = url;
        url = url.substring(urlBase.length() + 1);
        url = url.substring(url.indexOf("/") + 1);
        try {
            return Integer.parseInt(url);
        } catch (NumberFormatException e) {
            throw new MalformedURLException(full);
        }
    }

    @Override
    public JSONObject loadJSON(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        if (userAgent != null)
            connection.setRequestProperty("User-Agent", userAgent);
        connection.connect();

        BufferedReader r = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            sb.append(line);
        }
        return new JSONObject(sb.toString());
    }

    @Override
    public URLLink loadLink(JSONObject json, String key) {
        if (json.isNull(key))
            return null;
        if (urlObjectKey != null)
            return new Linked(json.getJSONObject(key));
        else
            return new Linked(json.getString(key));
    }

    @Override
    public URLLink[] loadLinkArray(JSONArray array) {
        Linked[] out = new Linked[array.length()];
        for (int i = 0; i < out.length; i++) {
            if (array.isNull(i))
                out[i] = null;
            else if (urlObjectKey != null)
                out[i] = new Linked(array.getJSONObject(i));
            else
                out[i] = new Linked(array.getString(i));
        }
        return out;
    }

    @Override
    public JSONAPIPage makeLinkAPI() {
        if (urlObjectKey == null)
            return null;
        return new JSONAPIPage("URLLink", new JSONAPIPage.JSONAPIValue[]{}, "An object used when linking to another object.", null, null);
    }

    @Override
    public JSONArray makeLinkArray(Iterable<? extends JSONPage> objects) {
        JSONArray out = new JSONArray();
        for (JSONPage i : objects) {
            if (urlObjectKey != null) {
                out.put(i.makeURL(this));
            } else {
                out.put(makeURL(i));
            }
        }
        return out;
    }

    @Override
    public String makeURL(String category, int id) {
        return urlBase + category + "/" + id;
    }

    /**
     * Create a JSONObject for the link to the given object. Only use this if links aren't stored as raw strings.
     *
     * @param object The object to generate a link for.
     * @return A JSONObject containing the name of the object and the link to it.
     */
    public JSONObject makeObject(JSONPage object) {
        JSONObject out = new JSONObject();
        out.put("name", object.name);
        out.put(urlObjectKey, makeURL(object));
        return out;
    }

    /**
     * The URLLink for the GeneralURLBase
     */
    private class Linked extends URLLink {

        /**
         * The category the link is for.
         */
        private final String category;
        /**
         * The ID of the object
         */
        private final int id;

        private Linked(int id, String category) {
            this.id = id;
            this.category = category;
        }

        private Linked(JSONObject json) {
            if (json.isNull(urlObjectKey))
                throw new MalformedURLException(json, "Key \"" + urlObjectKey + "\" was null in the JSON.");
            String url = json.getString(urlObjectKey);
            this.id = getID(url);
            this.category = getCategory(url);
        }

        private Linked(String url) {
            this(getID(url), getCategory(url));
        }

        @Override
        public String getURL() {
            return makeURL(category, id);
        }
    }

    /**
     * Thrown when the URL can't be correctly parsed.
     */
    public class MalformedURLException extends RuntimeException {
        /**
         * The JSONObject containing the URL. If null, it's from a bad URL string.
         */
        public final JSONObject jsonObject;
        /**
         * The URL being parsed. If null, it's from a bad JSONObject.
         */
        public final String url;

        private MalformedURLException(String message, String url, JSONObject jsonObject) {
            super(message);
            this.url = url;
            this.jsonObject = jsonObject;
        }

        /**
         * An error for when a URL string is malformed.
         *
         * @param url The bad URL.
         */
        public MalformedURLException(String url) {
            this(url + " did not fit the correct format of \"" + urlBase + "<category>/<id>\"", url, null);
        }

        /**
         * An error for when a JSONObject doesn't correctly contain a URL.
         *
         * @param jsonObject The bad JSONObject.
         * @param message    The reason for the error.
         */
        public MalformedURLException(JSONObject jsonObject, String message) {
            this(message + "\n" + jsonObject.toString(2), null, jsonObject);
        }
    }
}
