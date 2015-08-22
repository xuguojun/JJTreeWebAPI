/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jjtree.utilities;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;

/**
 *
 * @author rose
 */
public class JServeletManager {
    public static JSONObject fetchFrom(HttpServletRequest request, String url)
  {
    JSONObject object = null;
    try
    {
      String serverName = request.getServerName();
      int portNumber = request.getServerPort();
      String contextPath = request.getContextPath();
      
      String accountUrl = "http://" + serverName + ":" + portNumber + contextPath + url;
      
      URL urldemo = new URL(accountUrl);
      URLConnection urlCon = urldemo.openConnection();
      BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        object = new JSONObject(inputLine);
      }
      in.close();
    }
    catch (Exception e)
    {
      System.out.println(e);
    }
    return object;
  }
}
