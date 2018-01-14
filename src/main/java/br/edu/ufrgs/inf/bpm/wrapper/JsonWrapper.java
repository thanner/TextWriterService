package br.edu.ufrgs.inf.bpm.wrapper;

import com.google.gson.Gson;

public class JsonWrapper {

    private static Gson gson = new Gson();

    public static <T> T getObject(String json, Class<T> classType) {
        return gson.fromJson(json, classType);
    }

    public static <T> String getJson(T object) {
        return gson.toJson(object);
    }

}
