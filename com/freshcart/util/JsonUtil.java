package com.freshcart.util;

public class JsonUtil {

    public static String escape(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    public static String quote(String value) {
        return "\"" + escape(value) + "\"";
    }
}