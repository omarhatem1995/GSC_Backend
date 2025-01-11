package com.gsc.gsc.utilities;

import javax.servlet.http.HttpServletRequest;

public class Utilities {

    public static int getLangId(HttpServletRequest request) {
        String lang = request.getHeader("Accept-language");
        return (lang != null && lang.contains("ar")) ? 2 : 1;
    }
    public static int getLangId(String lang) {
        return (lang != null && lang.contains("ar")) ? 2 : 1;
    }
}