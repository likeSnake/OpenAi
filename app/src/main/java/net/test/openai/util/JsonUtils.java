package net.test.openai.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class JsonUtils {
    public static String toJson(Object obj) {
        Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .create();
        return gson.toJson(obj);
    }
}
