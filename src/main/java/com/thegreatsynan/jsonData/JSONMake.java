package com.thegreatsynan.jsonData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * A base class for objects that can be converted to JSON
 */
public abstract class JSONMake {
    public JSONMake(JSONObject json, URLEncoder base) {
    }

    public JSONMake() {
    }

    public static Boolean getBoolean(JSONObject json, String key) {
        if (json.isNull(key))
            return null;
        return json.getBoolean(key);
    }

    public static Double getDouble(JSONObject json, String key) {
        if (json.isNull(key))
            return null;
        return json.getDouble(key);
    }

    public static Float getFloat(JSONObject json, String key) {
        if (json.isNull(key))
            return null;
        return json.getFloat(key);
    }

    public static Integer getInt(JSONObject json, String key) {
        if (json.isNull(key))
            return null;
        return json.getInt(key);
    }

    public static String getString(JSONObject json, String key) {
        if (json.isNull(key))
            return null;
        return json.getString(key);
    }

    /**
     * Create an array of Doubles from a JSONArray.
     *
     * @param array A JSONArray of doubles.
     * @return An array of Doubles, can be null.
     */
    public static Double[] loadDoubleArray(JSONArray array) {
        Double[] out = new Double[array.length()];
        for (int i = 0; i < out.length; i++) {
            if (array.isNull(i))
                out[i] = null;
            else
                out[i] = array.getDouble(i);
        }
        return out;
    }

    /**
     * Create an array of Floats from a JSONArray.
     *
     * @param array A JSONArray of floats.
     * @return An array of Floats, can be null.
     */
    public static Float[] loadFloatArray(JSONArray array) {
        Float[] out = new Float[array.length()];
        for (int i = 0; i < out.length; i++) {
            if (array.isNull(i))
                out[i] = null;
            else
                out[i] = array.getFloat(i);
        }
        return out;
    }

    /**
     * Create an array of Integers from a JSONArray.
     *
     * @param array A JSONArray of integers.
     * @return An array of Integers, can be null.
     */
    public static Integer[] loadIntegerArray(JSONArray array) {
        Integer[] out = new Integer[array.length()];
        for (int i = 0; i < out.length; i++) {
            if (array.isNull(i))
                out[i] = null;
            else
                out[i] = array.getInt(i);
        }
        return out;
    }

    /**
     * Create an array of Strings from a JSONArray.
     *
     * @param array A JSONArray of Strings.
     * @return An array of Strings, can be null.
     */
    public static String[] loadStringArray(JSONArray array) {
        String[] out = new String[array.length()];
        for (int i = 0; i < out.length; i++) {
            if (array.isNull(i))
                out[i] = null;
            else
                out[i] = array.getString(i);
        }
        return out;
    }

    /**
     * Create a JSONArray from a list of objects.
     *
     * @param array A list of objects to convert to a JSONArray.
     * @param base  The URL writer.
     * @return A JSONArray containing all the objects as JSONObjects.
     */
    public static JSONArray makeArray(Iterable<JSONMake> array, URLEncoder base) {
        JSONArray out = new JSONArray();
        for (JSONMake i : array) {
            out.put(i.makeObject(base));
        }
        return out;
    }

    /**
     * Create a JSONArray from an array of objects.
     *
     * @param array An array of objects to convert to a JSONArray.
     * @param base  The URL writer.
     * @return A JSONArray containing all the objects as JSONObjects.
     */
    public static JSONArray makeArray(JSONMake[] array, URLEncoder base) {
        return makeArray(Arrays.asList(array), base);
    }

    /**
     * Make a JSONArray of simple variables.
     *
     * @param array The array of variables to save.
     * @return A JSONArray containing all the values.
     */
    public static JSONArray makeArray(Object[] array) {
        JSONArray out = new JSONArray();
        for (Object i : array) {
            out.put(i);
        }
        return out;
    }

    /**
     * Make an API JSON Page for this object.
     */
    public abstract JSONAPIPage makeAPI();

    /**
     * Convert this object into a JSONObject.
     *
     * @param base The URL writer.
     * @return A JSONObject representing this object.
     */
    public abstract JSONObject makeObject(URLEncoder base);
}
