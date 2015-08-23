/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jjtree.servelet;

import com.jjtree.utilities.JConstant;
import com.jjtree.utilities.JConverter;
import com.jjtree.utilities.JResponse;
import com.jjtree.utilities.JServeletManager;
import com.jjtree.utilities.JString;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author rose
 */
public class Passwords extends HttpServlet {
    
    private Connection conn;
    private Statement stmt;

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
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        processRequest(request, response);
        
        JSONObject jsonObject = JConverter.convert(request);
        if (jsonObject == null) {
            return;
        }
        
        String account = null;
        String oldPassword = null;
        String newPassword = null;
        
        boolean isEmail = false;
        try {
            account = jsonObject.getString("account");
            oldPassword = jsonObject.getString("oldPassword");
            newPassword = jsonObject.getString("newPassword");
            
            if (JString.isEmail(account)) {
                isEmail = true;
            }
            
            if (JString.isPhoneNumber(account)) {
                isEmail = false;
            }
        } catch (JSONException ex) {
            Logger.getLogger(Passwords.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            // Register JDBC driver
            Class.forName(JConstant.JDBC_DRIVER);

            // Open a connection
            conn = DriverManager.getConnection(JConstant.DB_URL, JConstant.USER, JConstant.PASSWORD);

            // Execute SQL query
            stmt = conn.createStatement();
            
            String sql = null;
            if (isEmail) {
                sql = "SELECT password, userID FROM JUser WHERE email ='" + account + "'";
            } else {
                sql = "SELECT password, userID FROM JUser WHERE mobile ='" + account + "'";
            }
            
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String password = rs.getString(1);
                int accountID = rs.getInt(2);
                
                if (password.equals(oldPassword)) {
                    if (isEmail) {
                        sql = "UPDATE JUser SET password = '" + newPassword + "' WHERE email = '" + account + "'";
                    } else {
                        sql = "UPDATE JUser SET password = '" + newPassword + "' WHERE mobile = '" + account + "'";
                    }
                    
                    stmt.executeUpdate(sql);
                    
                    String accountUrl = "/accounts/" + accountID;
                    JSONObject accountObject = JServeletManager.fetchFrom(request, accountUrl);
                    JResponse.sendJson(response, accountObject);
                } else {
                    JResponse.sendErrorMessage(1, "Old password incorrect.", response);
                }
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
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
