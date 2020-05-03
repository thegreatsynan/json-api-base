package com.thegreatsynan.jsonData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * An object that can be returned as a full page of JSON data.
 */
public abstract class JSONPage extends JSONMake {
    /**
     * The database of all the loaded objects so far.
     */
    private static HashMap<String, HashMap<Integer, JSONPage>> LOADED = new HashMap<>();
    /**
     * The id of the object.
     */
    public final int id;
    /**
     * The code name of the object.
     */
    public final String name;

    /**
     * @param name The code name of the object.
     * @param id   The partial url used to fetch the object.
     */
    public JSONPage(String name, int id) {
        this.name = name;
        this.id = id;
        String cat = getCategory();
        if (id >= 0) {
            if (!LOADED.containsKey(cat)) {
                LOADED.put(cat, new HashMap<>());
            }
            LOADED.get(cat).put(id, this);
        }
    }

    /**
     * Create from a JSONObject.
     *
     * @param json The JSONObject with all the data.
     * @param base The URL Decoder.
     */
    public JSONPage(JSONObject json, URLEncoder base) {
        this(json.getString("name"), json.getInt("id"));
    }

    /**
     * Create and load an object based on the given JSONObject
     *
     * @param category The type of object to load.
     * @param json     The JSONObject to load the info from.
     * @param base     The URL reader.
     * @return The loaded object.
     */
    static JSONPage createObject(String category, JSONObject json, URLEncoder base) {
        return null;
    }

    /**
     * Get an array of loaded objects. Values will be saved as null if one can't be loaded.
     *
     * @param category The category the objects are part of. Should match getCategory().
     * @param url      The IDs of the objects.
     * @param base     The URL reader.
     * @return An array of loaded objects.
     */
    public static JSONPage[] get(String category, int[] url, URLEncoder base) {
        JSONPage[] out = new JSONPage[url.length];
        for (int i = 0; i < url.length; i++)
            out[i] = get(category, url[i], base);
        return out;
    }

    /**
     * Get a loaded object or try to load one. Will return null if one can't be found or loaded.
     *
     * @param category The category the object is part of. Should match getCategory().
     * @param url      The partial url of the object.
     * @param base     The URL reader.
     * @return The loaded object. If it can't be found, returns null.
     */
    public static JSONPage get(String category, int url, URLEncoder base) {
        JSONPage obj;
        try {
            obj = LOADED.get(category).get(url);
        } catch (NullPointerException e) {
            obj = null;
        }
        if (obj != null)
            return obj;
        try {
            return createObject(category, base.loadJSON(category, url), base);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Create an array of IDs from the given array.
     *
     * @param array The JSONArray of links.
     * @param base  The base
     * @return
     */
    public static int[] loadIDArray(JSONArray array, URLEncoder base) {
        URLEncoder.URLLink[] links = base.loadLinkArray(array);
        int[] out = new int[array.length()];
        for (int i = 0; i < out.length; i++) {
            out[i] = base.getID(links[i]);
        }
        return out;
    }

    /**
     * Create an array of URL objects for the given list.
     *
     * @param array The list of object to get the URLs for.
     * @param base  The URL writer.
     * @return A JSONArray containing all the names and URLs as objects.
     */
    public static JSONArray makeURLArray(JSONPage[] array, URLEncoder base) {
        return makeURLArray(Arrays.asList(array), base);
    }

    /**
     * Create an array of URL objects for the given list.
     *
     * @param array The list of object to get the URLs for.
     * @param base  The URL writer.
     * @return A JSONArray containing all the names and URLs as objects.
     */
    public static JSONArray makeURLArray(Iterable<JSONPage> array, URLEncoder base) {
        JSONArray out = new JSONArray();
        for (JSONPage i : array) {
            out.put(i.makeURL(base));
        }
        return out;
    }

    /**
     * Get the category this type of object is in. This is used in URLs and databases.
     *
     * @return The name of the category.
     */
    public abstract String getCategory();

    @Override
    public JSONObject makeObject(URLEncoder base) {
        JSONObject out = new JSONObject();
        out.put("name", name);
        out.put("id", id);
        return out;
    }

    /**
     * Make a URL object for this object
     *
     * @param base The URL writer.
     * @return A String of the full URL.
     */
    public final String makeURL(URLEncoder base) {
        return base.makeURL(this);
    }
}
