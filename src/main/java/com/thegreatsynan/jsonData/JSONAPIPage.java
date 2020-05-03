package com.thegreatsynan.jsonData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * An API description for an object.
 */
public class JSONAPIPage extends JSONMake {
    /**
     * The category name of the object, used in URLs. If it is null, this is a simple object that can not be loaded as a page
     */
    public final String category;
    /**
     * The API documentation explaining the object.
     */
    public final String details;
    /**
     * The class this one will always be inside of. If null, it won't always be in just one class, if any.
     */
    public final String inside;
    /**
     * The full name of the object
     */
    public final String object;
    /**
     * An array of values in the object
     */
    public final JSONAPIValue[] values;

    /**
     * @param object   The full name of the object
     * @param values   An array of values in the object
     * @param details  The API documentation explaining the object.
     * @param category The category name of the object, used in URLs. If it is null, this is a simple object that can not be loaded as a page
     * @param inside   The class this one will always be inside of. If null, it won't always be in just one class, if any.
     */
    public JSONAPIPage(String object, JSONAPIValue[] values, String details, String category, String inside) {
        this.object = object;
        this.values = values;
        this.details = details;
        this.category = category;
        this.inside = inside;
    }

    /**
     * Create from a JSONObject.
     *
     * @param json The JSONObject with all the data.
     * @param base The URL Decoder.
     */
    public JSONAPIPage(JSONObject json, URLEncoder base) {
        this.object = JSONMake.getString(json, "object");
        this.values = JSONAPIValue.create(json.getJSONArray("values"), base);
        this.details = JSONMake.getString(json, "details");
        this.category = JSONMake.getString(json, "category");
        this.inside = JSONMake.getString(json, "inside");
    }

    public static JSONAPIPage[] create(JSONArray array, URLEncoder base) {
        JSONAPIPage[] out = new JSONAPIPage[array.length()];
        for (int i = 0; i < out.length; i++) {
            out[i] = new JSONAPIPage(array.getJSONObject(i), base);
        }
        return out;
    }

    @Override
    public JSONAPIPage makeAPI() {
        ArrayList<JSONAPIValue> vars = new ArrayList<>();
        vars.add(new JSONAPIValue("object", "string", false, "The full name of the object"));
        vars.add(new JSONAPIValue("values", "JSONAPIValue", true, "An array of values in the object"));
        vars.add(new JSONAPIValue("details", "string", false, "The API documentation explaining the object."));
        vars.add(new JSONAPIValue("category", "string", false, "The category name of the object, used in URLs. If it is null, this is a simple object that can not be loaded as a page"));
        vars.add(new JSONAPIValue("inside", "string", false, "The class this one will always be inside of. If null, it won't always be in just one class, if any."));
        return new JSONAPIPage(this.getClass().getSimpleName(), vars.toArray(new JSONAPIValue[vars.size()]), "An API description for an object.", null, null);
    }

    @Override
    public JSONObject makeObject(URLEncoder base) {
        JSONObject out = new JSONObject();
        out.put("object", object);
        out.put("values", JSONMake.makeArray(values, base));
        out.put("details", details);
        out.put("category", category);
        out.put("inside", inside);
        return out;
    }

    /**
     * A single API value.
     */
    public static class JSONAPIValue extends JSONMake {
        /**
         * If true, this value is an array.
         */
        public final Boolean array;
        /**
         * The API documentation explaining the value.
         */
        public final String detail;
        /**
         * The name of the key.
         */
        public final String key;
        /**
         * The type of value.
         */
        public final String type;

        /**
         * @param key    The name of the key.
         * @param type   The type of value.
         * @param array  If true, this value is an array.
         * @param detail The API documentation explaining the value.
         */
        public JSONAPIValue(String key, String type, Boolean array, String detail) {
            this.key = key;
            this.type = type;
            this.array = array;
            this.detail = detail;
        }

        /**
         * Create from a JSONObject.
         *
         * @param json The JSONObject with all the data.
         * @param base The URL Decoder.
         */
        public JSONAPIValue(JSONObject json, URLEncoder base) {
            this.key = JSONMake.getString(json, "key");
            this.type = JSONMake.getString(json, "type");
            this.array = JSONMake.getBoolean(json, "array");
            this.detail = JSONMake.getString(json, "detail");
        }

        public static JSONAPIValue[] create(JSONArray array, URLEncoder base) {
            JSONAPIValue[] out = new JSONAPIValue[array.length()];
            for (int i = 0; i < out.length; i++) {
                out[i] = new JSONAPIValue(array.getJSONObject(i), base);
            }
            return out;
        }

        @Override
        public JSONAPIPage makeAPI() {
            ArrayList<JSONAPIValue> vars = new ArrayList<>();
            vars.add(new JSONAPIValue("key", "string", false, "The name of the key."));
            vars.add(new JSONAPIValue("type", "string", false, "The type of value."));
            vars.add(new JSONAPIValue("array", "boolean", false, "If true, this value is an array."));
            vars.add(new JSONAPIValue("detail", "string", false, "The API documentation explaining the value."));
            return new JSONAPIPage(this.getClass().getSimpleName(), vars.toArray(new JSONAPIValue[vars.size()]), "A single API value.", null, "JSONAPIPage");
        }

        @Override
        public JSONObject makeObject(URLEncoder base) {
            JSONObject out = new JSONObject();
            out.put("key", key);
            out.put("type", type);
            out.put("array", array);
            out.put("detail", detail);
            return out;
        }

    }
}