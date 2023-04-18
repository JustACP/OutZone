package com.outzone.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtill {

    public static final String charset  = "UTF-8";
    public static final String JsonType  = "application/json";
    public static String  renderString(HttpServletResponse response, String toWriteString){
        try{
            response.setStatus(200);
            response.setContentType(JsonType);
            response.setCharacterEncoding(charset);
            response.getWriter().write(toWriteString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
