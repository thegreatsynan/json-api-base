package com.thegreatsynan.jsonData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A series of functions to convert JSON api files to actual class files.
 */
public class ClassFromJSONAPI {

    /**
     * A list of values that can be replaced. This allows shorthand int and bool as values. Add more if you want to add anything.
     */
    private static final HashMap<String, String> alternates = new HashMap<>();

    /**
     * Add a shorthand alternate value. This will replace it when loading the JSON values.
     *
     * @param value       The shorthand name for the value.
     * @param replacement The full name of the value this represents.
     */
    public static void addAlternate(String value, String replacement) {
        alternates.put(value, replacement);
    }

    /**
     * Convert a folder of APIs to classes and sample JSONs.
     * Note that int will be replaced with integer, and bool with boolean
     *
     * @param arg 0 is the folder to look in and save the exports; 1 is the class package
     * @throws IOException If there is an error reading the files, saving the files, or a class wasn't listed in the API.
     */
    public static void main(String[] arg) throws IOException {
        addAlternate("int", "integer");
        addAlternate("bool", "boolean");
        makeClasses(arg[0], arg[1], new URL(), 2);
    }

    /**
     * Create a series of classes and sample JSON files from the give APIs in a folder.
     *
     * @param folder      The folder to look for API files in.
     * @param pack        The package name all the classes should be in.
     * @param JSONIndents How many indents to put into the sample files.
     * @throws IOException If there is an error reading the files, saving the files, or a class wasn't listed in the API.
     */
    public static void makeClasses(String folder, String pack, URLEncoder base, int JSONIndents) throws IOException {
        ArrayList<JSONAPIPage> list = readFiles(folder, base);
        ClassCreator cc = new ClassCreator(list.toArray(new JSONAPIPage[0]), pack);
        String js = folder + "\\blankJSON";
        System.out.println("Making folders " + folder + "\\blankJSON: " + new File(js).mkdirs());
        folder = folder + "\\src\\" + pack.replace("", "\\");
        System.out.println("Making folders " + folder + ": " + new File(folder).mkdirs());
        ArrayList<String> names = cc.getClassNames();
        ArrayList<String> classes = cc.makeClasses();
        ArrayList<String> json = cc.blankJSON(base, JSONIndents);
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i) + ".java");
            try (PrintWriter out = new PrintWriter(folder + "\\" + names.get(i) + ".java")) {
                out.println(classes.get(i));
            }
            System.out.println(names.get(i) + ".json");
            try (PrintWriter out = new PrintWriter(js + "\\" + names.get(i) + ".json")) {
                out.println(json.get(i));
            }
        }
    }

    private static JSONAPIPage read(File file, URLEncoder base) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String json;
        try {
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }
            json = sb.toString();
        } finally {
            reader.close();
        }
        JSONObject j = new JSONObject(json);
        JSONArray a = j.getJSONArray("values");
        for (int i = 0; i < a.length(); i++) {
            JSONObject js = a.getJSONObject(i);
            String sr = js.getString("type");
            js.put("type", alternates.getOrDefault(sr, sr));
        }
        j.put("values", a);
        return new JSONAPIPage(j, base);
    }

    private static ArrayList<JSONAPIPage> readFiles(String path, URLEncoder base) throws IOException {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        ArrayList<JSONAPIPage> out = new ArrayList<>();
        if (listOfFiles != null) {
            for (File i : listOfFiles) {
                if (i.isFile())
                    out.add(read(i, base));
            }
        }
        return out;
    }

    private static class Link extends URLEncoder.URLLink {

        private final String URL;

        private Link(String url) {
            URL = url;
        }

        @Override
        public String getURL() {
            return URL;
        }
    }

    private static class URL extends URLEncoder {
        @Override
        public void addLink(JSONObject json, String key, JSONPage object) {
            json.put(key, makeURL(object.getCategory(), object.id));
        }

        @Override
        public String getCategory(String url) {
            return url.substring(0, url.indexOf("/"));
        }

        @Override
        public int getID(String url) {
            return Integer.parseInt(url.substring(url.indexOf("/")));
        }

        @Override
        public JSONObject loadJSON(String url) {
            return null;
        }

        @Override
        public URLLink loadLink(JSONObject json, String key) {
            return new Link(json.getString(key));
        }

        @Override
        public URLLink[] loadLinkArray(JSONArray array) {
            URLLink[] out = new URLLink[array.length()];
            for (int i = 0; i < out.length; i++) {
                out[i] = new Link(array.getString(i));
            }
            return out;
        }

        @Override
        public JSONAPIPage makeLinkAPI() {
            return null;
        }

        @Override
        public JSONArray makeLinkArray(Iterable<? extends JSONPage> objects) {
            JSONArray out = new JSONArray();
            for (JSONPage i : objects)
                out.put(makeURL(i.getCategory(), i.id));
            return out;
        }

        @Override
        public String makeURL(String category, int id) {
            return category + "/" + id;
        }
    }
}
