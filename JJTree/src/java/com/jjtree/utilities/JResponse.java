/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jjtree.utilities;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author rose
 */
public class JResponse {

    public static void sendErrorMessage(int errorCode, String errorMessage, HttpServletResponse response) throws JSONException, IOException {
        JSONObject resultObject = new JSONObject();
        
        resultObject.put("errorCode", errorCode);
        resultObject.put("errorMessage", errorMessage);

        PrintWriter writer = response.getWriter();
        writer.print(resultObject);
        writer.flush();
    }
}
