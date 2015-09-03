/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jjtree.servelet;

import com.jjtree.utilities.JConstant;
import com.jjtree.utilities.JResponse;
import com.jjtree.utilities.JServeletManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author rose
 */
public class Fans extends HttpServlet {

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
        response.setContentType("application/json;charset=UTF-8");

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

        int accountID = Integer.parseInt(request.getParameter("accountID"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        int pageIndex = Integer.parseInt(request.getParameter("pageIndex"));

        try {
            Class.forName(JConstant.JDBC_DRIVER);
            conn = DriverManager.getConnection(JConstant.DB_URL, JConstant.USER, JConstant.PASSWORD);
            stmt = conn.createStatement();

            String sql = "SELECT DISTINCT subjectID FROM JUserBehaviors WHERE objectID = " + accountID + " AND predicate = 'watch' OFFSET " + pageSize * pageIndex + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";;

            ResultSet rs = stmt.executeQuery(sql);

            JSONArray fans = new JSONArray();
            while (rs.next()) {
                int _accountID = rs.getInt("subjectID");

                String accountUrl = "/accounts/" + _accountID;
                JSONObject accountObject = JServeletManager.fetchFrom(request, accountUrl);

                fans.put(accountObject);
            }

            JSONObject fansObject = new JSONObject();
            fansObject.put("fans", fans);

            JResponse.sendJson(response, fansObject);

            // Clean-up environment
            rs.close();;
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
