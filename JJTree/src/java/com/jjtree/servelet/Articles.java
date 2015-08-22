/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jjtree.servelet;

import com.jjtree.utilities.JConstant;
import com.jjtree.utilities.JServeletManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author rose
 */
@WebServlet(name = "Articles", urlPatterns = {"/articles"})
public class Articles extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        
    }

    private Connection conn;
    private Statement stmt;
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        
        String pathInfo = request.getPathInfo();
        String[] path = pathInfo.split("/");
        int valueID = Integer.parseInt(path[1]);

        try {
            // Register JDBC driver
            Class.forName(JConstant.JDBC_DRIVER);

            // Open a connection
            conn = DriverManager.getConnection(JConstant.DB_URL, JConstant.USER, JConstant.PASSWORD);

            // Execute SQL query
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM JArticle WHERE articleID = " + valueID;
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                int articleID = rs.getInt("articleID");
                int userID = rs.getInt("userID");

                String title = rs.getString("title");
                String createdAt = rs.getString("createdAt");
                String updatedAt = rs.getString("updatedAt");
                
                int usefulValue = rs.getInt("usefulValue");
                int uselessValue = rs.getInt("uselessValue");
                int viewCount = rs.getInt("viewCount");

                String accountUrl = "/accounts/" + userID;
                JSONObject author = JServeletManager.fetchFrom(request, accountUrl);
                JSONObject article = new JSONObject();

                article.put("articleID", articleID);
                article.put("userID", userID);
                
                article.put("title", title);
                article.put("createdAt", createdAt);
                article.put("updatedAt", updatedAt);
                
                article.put("usefulValue", usefulValue);
                article.put("uselessValue", uselessValue);
                article.put("viewCount", viewCount);
                
                PrintWriter writer = response.getWriter();
                
                article.put("author", author);
                writer.print(article);
            }

            // Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        } //end try
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
