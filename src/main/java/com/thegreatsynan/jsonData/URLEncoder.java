package com.thegreatsynan.jsonData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Extend this class to make a class that will be able to decode/encode urls for objects and download/load JSON data.
 */
public abstract class URLEncoder {

    /**
     * Add a link for an object to a JSONObject under the given key.
     *
     * @param json   The JSONObject that this will be saved to.
     * @param key    The key to save it under.
     * @param object The object being saved.
     */
    public abstract void addLink(JSONObject json, String key, JSONPage object);


    /**
     * Get the category of the object from the given URL.
     *
     * @param url The full URL of the object.
     * @return The category the object is in.
     */
    public abstract String getCategory(String url);

    /**
     * Get the category of the object.
     *
     * @param link The object containing the link.
     * @return The category the object is in.
     */
    public final String getCategory(URLLink link) {
        return getCategory(link.getURL());
    }

    /**
     * Get the unique ID for the object.
     *
     * @param link The object containing the link.
     * @return The ID for the object.
     */
    public final int getID(URLLink link) {
        return getID(link.getURL());
    }

    /**
     * Get the ID link to an object from the given key in the given JSONObject.
     *
     * @param json The JSONObject to look in.
     * @param key  The key the link is kept under.
     * @return The ID of the object.
     */
    public final int getID(JSONObject json, String key) {
        return getID(loadLink(json, key));
    }

    /**
     * Get the unique ID for the object fro the given URL.
     *
     * @param url The full URL of the object
     * @return The ID for the object.
     */
    public abstract int getID(String url);

    /**
     * Make an array of IDs from the given JSONArray.
     *
     * @param array The JSONArray containing all the links.
     * @return An array of integers containing the IDs of the objects.
     */
    public final int[] getIDs(JSONArray array) {
        URLLink[] links = loadLinkArray(array);
        int[] out = new int[links.length];
        for (int i = 0; i < links.length; i++)
            out[i] = getID(links[i]);
        return out;
    }

    /**
     * Load a JSON object from the given URL.
     *
     * @param url The full URL to load the object from.
     * @return The JSONObject for converting.
     * @throws IOException If the JSON could not be loaded.
     */
    public abstract JSONObject loadJSON(String url) throws IOException;

    /**
     * Load a JSON object from the given information.
     *
     * @param category The category the object is in.
     * @param id       The id of the object.
     * @return The JSONObject for converting.
     * @throws IOException If the JSON could not be loaded.
     */
    public final JSONObject loadJSON(String category, int id) throws IOException {
        return loadJSON(makeURL(category, id));
    }

    /**
     * Make a URLLink object from inside the given JSONObject and key
     *
     * @param json The JSONObject containing all the information.
     * @return A URLLink object for the given key.
     */
    public abstract URLLink loadLink(JSONObject json, String key);

    /**
     * Make an array of URLLinks out of the given JSONArray
     *
     * @param array The JSONArray containing all the links.
     * @return An array of URLLinks from the array.
     */
    public abstract URLLink[] loadLinkArray(JSONArray array);

    /**
     * Create an API for how the links are stored in JSON.
     *
     * @return An API page explaining the link. Will return null if it isn't stored as an object, but just a value.
     */
    public abstract JSONAPIPage makeLinkAPI();

    /**
     * Create a JSONArray of links/link objects for the list of objects.
     *
     * @param objects The Iterable set of objects that have unique links.
     * @return A JSONArray containing links to all the objects.
     */
    public abstract JSONArray makeLinkArray(Iterable<? extends JSONPage> objects);

    /**
     * Make a URL for an object from the given information.
     *
     * @param category The category the object is in.
     * @param id       The id of the object.
     * @return The full URL to the object.
     */
    public abstract String makeURL(String category, int id);

    /**
     * Make a URL String for the given object.
     *
     * @param object The object to generate a URL for.
     * @return The full URL to the object.
     */
    public String makeURL(JSONPage object) {
        return makeURL(object.getCategory(), object.id);
    }

    /**
     * A class that dictates how to save and read URL links.
     */
    public static abstract class URLLink {

        /**
         * Get the String URL to the file.
         *
         * @return The string URL for the link.
         */
        public abstract String getURL();
    }
}
