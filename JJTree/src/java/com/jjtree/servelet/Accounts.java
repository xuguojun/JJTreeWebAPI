/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jjtree.servelet;

import com.jjtree.utilities.JConstant;
import com.jjtree.utilities.JConverter;
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
public class Accounts extends HttpServlet {

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

        String pathInfo = request.getPathInfo();
        String[] path = pathInfo.split("/");
        int userID = Integer.parseInt(path[1]);

        try {
            // Register JDBC driver
            Class.forName(JConstant.JDBC_DRIVER);

            // Open a connection
            conn = DriverManager.getConnection(JConstant.DB_URL, JConstant.USER, JConstant.PASSWORD);

            // Execute SQL query
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM JUser WHERE userID = " + userID;
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                int accountID = rs.getInt("userID");

                String email = rs.getString("email");
                String mobile = rs.getString("mobile");
                String password = rs.getString("password");
                String name = rs.getString("name");
                String avatarURL = rs.getString("avatarURL");

                JSONObject account = new JSONObject();

                account.put("accountID", accountID);
                account.put("email", email);
                account.put("mobile", mobile);
                account.put("password", password);
                account.put("name", name);
                account.put("avatarURL", avatarURL);

                PrintWriter writer = response.getWriter();
                writer.print(account);
                writer.flush();
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

        JSONObject jsonObject = JConverter.convert(request);
        if (jsonObject == null) {
            return;
        }

        String account = null;
        String password = null;
        String name = null;
        try {
            account = jsonObject.getString("account");
            name = jsonObject.getString("name");
            password = jsonObject.getString("password");

            if (account == null || password == null || name == null) {
                return;
            }
        } catch (JSONException ex) {
            Logger.getLogger(Accounts.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {

            // Register JDBC driver
            Class.forName(JConstant.JDBC_DRIVER);

            // Open a connection
            conn = DriverManager.getConnection(JConstant.DB_URL, JConstant.USER, JConstant.PASSWORD);

            // Execute SQL query
            stmt = conn.createStatement();

            String sql = "SELECT MAX(userID) FROM JUser";
            ResultSet rs = stmt.executeQuery(sql);

            int nextUserID = 0;
            // Extract data from result set
            while (rs.next()) {
                nextUserID = rs.getInt(1) + 1;
            }

            String insertSql = null;
            if (JString.isEmail(account)) {
                insertSql = "INSERT INTO JUser(email, password, name, userID) VALUES ('" + account + "', '" + password + "', '" + name + "', " + nextUserID + ")";
            }

            if (JString.isPhoneNumber(account)) {
                insertSql = "INSERT INTO JUser(mobile, password, name, userID) VALUES ('" + account + "', '" + password + "', '" + name + "', " + nextUserID + ")";
            }

            Statement stmt2 = conn.createStatement();
            stmt2.executeUpdate(insertSql);

            String accountUrl = "/accounts/" + nextUserID;

            JSONObject accountObject = JServeletManager.fetchFrom(request, accountUrl);

            PrintWriter writer = response.getWriter();
            writer.print(accountObject);
            writer.flush();

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
