package com.thegreatsynan.jsonData;

import com.thegreatsynan.jsonData.JSONAPIPage.JSONAPIValue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class ClassCreator {
    public static String[] defaultPageVars = {"name", "id"};
    public static String[] simpleValues = {"Boolean", "Integer", "Float", "Double", "String"};
    public final JSONAPIPage[] apis;
    public final String pack;

    public ClassCreator(JSONAPIPage[] apis, String pack) {
        this.apis = apis;
        this.pack = pack;
    }

    private static String capitalizeFirst(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private static boolean containsValue(JSONAPIPage api, String value) {
        for (JSONAPIValue i : api.values) {
            if (i.key.equals(value))
                return true;
        }
        return false;
    }

    private static boolean doVar(JSONAPIPage api, JSONAPIValue value) {
        if (isPage(api)) {
            for (String i : defaultPageVars) {
                if (value.key.equals(i))
                    return false;
            }
        }
        return true;
    }

    public static ArrayList<String> getClassNames(JSONAPIPage[] apis) {
        ArrayList<String> out = new ArrayList<>();
        for (JSONAPIPage i : apis) {
            if (i.inside == null)
                out.add(i.object);
        }
        return out;
    }

    public static boolean isPage(JSONAPIPage api) {
        return api.category != null;
    }

    private static boolean isSimpleValue(JSONAPIValue value) {
        for (String i : simpleValues) {
            if (i.toLowerCase().equals(value.type.toLowerCase()))
                return true;
        }
        return false;
    }

    public ArrayList<String> blankJSON(URLEncoder base, int indents) {
        ArrayList<String> out = new ArrayList<>();
        for (JSONAPIPage i : apis) {
            if (i.inside == null)
                out.add(blankJSON(i, base).toString(indents));
        }
        return out;
    }

    public JSONObject blankJSON(JSONAPIPage api, URLEncoder base) {
        JSONObject out = new JSONObject();
        for (JSONAPIValue i : api.values) {
            if (i.array) {
                JSONArray ar = new JSONArray();
                if (isSimpleValue(i)) {
                    for (int j = 0; j < 3; j++)
                        ar.put("<" + i.type + j + ">");
                } else if (isPage(i)) {
                    for (int j = 0; j < 3; j++)
                        ar.put(new JSONObject().put("url", base.makeURL(getCategory(i.type), 99990 + j)).put("name", "<" + i.type + j + ">"));
                } else {
                    for (JSONAPIPage j : apis) {
                        if (j.object.equals(i.type)) {
                            for (int k = 0; k < 3; k++)
                                ar.put(blankJSON(j, base));
                            break;
                        }
                    }
                }
                out.put(i.key, ar);
            } else if (isSimpleValue(i))
                out.put(i.key, "<" + i.type + ">");
            else if (isPage(i))
                out.put(i.key, new JSONObject().put("url", base.makeURL(getCategory(i.type), 9999)).put("name", "<" + i.type + ">"));
            else {
                for (JSONAPIPage j : apis) {
                    if (j.object.equals(i.type)) {
                        out.put(i.key, blankJSON(j, base));
                        break;
                    }
                }
            }
        }
        return out;
    }

    private String getCategory(String object) {
        for (JSONAPIPage i : apis) {
            if (i.object.equals(object))
                return i.category;
        }
        throw new RuntimeException(object + " does not have a loaded API");
    }

    public ArrayList<String> getClassNames() {
        return getClassNames(apis);
    }

    private boolean isPage(JSONAPIValue value) {
        String obj = value.type;
        for (String i : simpleValues) {
            if (i.toLowerCase().equals(obj.toLowerCase()))
                return false;
        }
        for (JSONAPIPage i : apis) {
            if (i.object.equals(obj))
                return (i.category != null);
        }
        throw new RuntimeException(obj + " does not have a loaded API");
    }

    private String makeAPIMaker(JSONAPIPage api) {
        StringBuilder out = new StringBuilder("@Override\npublic JSONAPIPage makeAPI() {\n" +
                tab("ArrayList<JSONAPIValue> vars = new ArrayList<>();\n"));
        for (JSONAPIValue i : api.values)
            out.append(tab("vars.add(new JSONAPIValue(\"" + i.key + "\", \"" + i.type + "\", " + (i.array ? "true" : "false") + ", \"" + i.detail + "\"));\n"));
        return out + tab("return new JSONAPIPage(this.getClass().getSimpleName(), vars.toArray(new JSONAPIValue[vars.size()]), \"" + api.details + "\", " +
                (api.category == null ? "null" : "\"" + api.category + "\"") + ", " +
                (api.inside == null ? "null" : "\"" + api.inside + "\"") + ");\n") + "}";
    }

    private String makeCategory(JSONAPIPage api) {
        if (isPage(api))
            return "@Override\npublic String getCategory() {\n" +
                    tab("return \"" + api.category + "\";\n") +
                    "}";
        return "";
    }

    public String makeClass(JSONAPIPage api) {
        System.out.println("Making " + api.object);
        String out = (api.inside == null ? "import org.json.JSONObject;\n" +
                "import com.thegreatsynan.jsonData.*;\n" +
                "import com.thegreatsynan.jsonData.JSONAPIPage.JSONAPIValue;\n" : "") +
                "/**\n * " + api.details + "\n */\npublic " + (api.inside != null ? "static " : "") + "class " + api.object + " extends JSON" + (isPage(api) ? "Page" : "Make") + " {\n";
        out += tab(makeVariables(api));
        out += tab(makeConstructor(api));
        out += tab(makeJSONConstructor(api));
        out += tab(makeGetters(api));
        out += tab(makeCategory(api));
        out += tab(makeAPIMaker(api));
        out += tab(makeObjectMaker(api));
        out += tab(makeGroupCreate(api));
        out += tab(makeInternals(api));
        if (api.inside == null) {
            if (out.contains("ArrayList"))
                out = "import java.util.ArrayList;\n" + out;
            if (out.contains("JSONArray"))
                out = "import org.json.JSONArray;\n" + out;
        }
        return (api.inside == null ? "package " + pack + ";\n\n" : "") + out + "}";
    }

    public ArrayList<String> makeClasses() {
        return makeClasses(apis);
    }

    public ArrayList<String> makeClasses(JSONAPIPage[] apis) {
        ArrayList<String> out = new ArrayList<>();
        for (JSONAPIPage i : apis) {
            if (i.inside == null)
                out.add(makeClass(i));
        }
        return out;
    }

    private String makeConstructor(JSONAPIPage api) {
        StringBuilder out = new StringBuilder("/**\n");
        for (JSONAPIValue i : api.values)
            out.append("* @param ").append(i.key).append(" ").append(i.detail).append("\n");
        out.append("*/\npublic ").append(api.object).append("(").append(makeConstructorArgs(api)).append(") {\n").append(isPage(api) ? tab("super(" + (containsValue(api, "name") ? "name" : "null") + ", id);\n") : "");
        for (JSONAPIValue i : api.values) {
            if (doVar(api, i))
                out.append(tab("this." + i.key + " = " + i.key + ";"));
        }
        return out + "}";
    }

    private String makeConstructorArgs(JSONAPIPage api) {
        String[] list = new String[api.values.length];
        for (int i = 0; i < list.length; i++)
            list[i] = makeVarType(api.values[i]) + (api.values[i].array ? "[]" : "") + " " + api.values[i].key;
        return String.join(", ", list);
    }

    private String makeGetter(JSONAPIValue value) {
        if (!isPage(value))
            return "";
        return "/**\n * Get " + value.detail + "\n * @param base The URL reader.\n * @return " + value.detail + "\n */\n" +
                " public " + value.type + (value.array ? "[]" : "") + " get" + capitalizeFirst(value.key) + "(URLEncoder base) {\n" +
                tab("return (" + value.type + (value.array ? "[]" : "") + ") JSONPage.get(\"" + getCategory(value.type) + "\", " + (value.key.equals("base") ? "this." : "") + value.key + ", base);\n") +
                "}";
    }

    private String makeGetters(JSONAPIPage api) {
        StringBuilder out = new StringBuilder();
        for (JSONAPIValue i : api.values)
            out.append(makeGetter(i));
        return out.toString();
    }

    private String makeGroupCreate(JSONAPIPage api) {
        if (isPage(api))
            return "";
        return "public static " + api.object + "[] create(JSONArray array, URLEncoder base) {\n" +
                tab(api.object + "[] out = new " + api.object + "[array.length()];\n" +
                        "for(int i = 0; i < out.length; i++){\n" +
                        tab("out[i] = new " + api.object + "(array.getJSONObject(i), base);") +
                        "}\nreturn out;"
                ) + "}";
    }

    private String makeInternals(JSONAPIPage api) {
        StringBuilder out = new StringBuilder();
        for (JSONAPIPage i : apis) {
            if (i.inside != null && i.inside.equals(api.object))
                out.append(makeClass(i)).append("\n");
        }
        return out.toString();
    }

    private String makeJSONConstructor(JSONAPIPage api) {
        StringBuilder out = new StringBuilder("/**\n * Create from a JSONObject.\n * @param json The JSONObject with all the data.\n * @param base The URL Decoder.\n" +
                "*/\npublic " + api.object + "(JSONObject json, URLEncoder base){\n");
        if (isPage(api))
            out.append(tab("super(json, base);\n"));
        for (JSONAPIValue i : api.values) {
            if (doVar(api, i)) {
                out.append(tab("this." + i.key + " = " + makeJSONGetter(i) + ";\n"));
            }
        }
        return out + "}";
    }

    private String makeJSONGetter(JSONAPIValue value) {
        if (value.array) {
            if (isPage(value))
                return "base.getIDs(json.getJSONArray(\"" + value.key + "\"))";
            for (String i : simpleValues) {
                if (i.toLowerCase().equals(value.type.toLowerCase()))
                    return "JSONMake.load" + i + "Array(json.getJSONArray(\"" + value.key + "\"))";
            }
            return value.type + ".create(json.getJSONArray(\"" + value.key + "\"), base)";
        }
        if (isPage(value))
            return "base.getID(json, \"" + value.key + "\")";
        for (String i : simpleValues) {
            if (i.toLowerCase().equals(value.type.toLowerCase())) {
                if (i.equals("Integer"))
                    i = "Int";
                return "JSONMake.get" + i + "(json, \"" + value.key + "\")";
            }
        }
        return "new " + value.type + "(json, base)";
    }

    private String makeJSONSaver(JSONAPIValue value) {
        if (value.array) {
            if (isPage(value))
                return "out.put(\"" + value.key + "\", JSONPage.makeURLArray(get" + capitalizeFirst(value.key) + "(base), base));\n";
            return "out.put(\"" + value.key + "\", JSONMake.makeArray(" + value.key + (isSimpleValue(value) ? "" : ", base") + "));\n";
        } else if (isPage(value))
            return "base.addLink(out, \"" + value.key + "\", get" + capitalizeFirst(value.key) + "(base));\n";
        else if (isSimpleValue(value))
            return "out.put(\"" + value.key + "\", " + value.key + ");\n";
        return "out.put(\"" + value.key + "\", " + value.key + ".makeObject(base));\n";
    }

    private String makeObjectMaker(JSONAPIPage api) {
        StringBuilder out = new StringBuilder("@Override\npublic JSONObject makeObject(URLEncoder base) {\n" +
                tab("JSONObject out = " + (isPage(api) ? "super.makeObject(base)" : "new JSONObject()") + ";\n"));
        for (JSONAPIValue i : api.values) {
            if (doVar(api, i)) {
                out.append(tab(makeJSONSaver(i)));
            }
        }
        return out + "return out;\n}";
    }

    private String makeVarType(JSONAPIValue value) {
        if (isPage(value))
            return "int";
        for (String i : simpleValues) {
            if (i.toLowerCase().equals(value.type))
                return i;
        }
        return value.type;
    }

    private String makeVariable(JSONAPIValue value) {
        return "/**\n * " + (isPage(value) ? "The id value for " : "") + value.detail + "\n */\n" +
                (isPage(value) ? "private" : "public") + " final " +
                makeVarType(value) + (value.array ? "[]" : "") + " " + value.key + ";\n";
    }

    private String makeVariables(JSONAPIPage api) {
        StringBuilder out = new StringBuilder();
        for (JSONAPIValue i : api.values) {
            if (doVar(api, i))
                out.append(makeVariable(i));
        }
        return out.toString();
    }

    private String tab(String string) {
        String[] list = string.split("\n");
        for (int i = 0; i < list.length; i++) {
            String s = "    ";
            list[i] = s + list[i];
        }
        return String.join("\n", list) + "\n";
    }

}
