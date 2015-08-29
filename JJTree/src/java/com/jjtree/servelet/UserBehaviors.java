/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jjtree.servelet;

import com.jjtree.utilities.JConstant;
import com.jjtree.utilities.JConverter;
import com.jjtree.utilities.JResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author rose
 */
public class UserBehaviors extends HttpServlet {
    
    static final String BEHAVIOR_WATCH = "watch";
    static final String BEHAVIOR_REWARD_USER = "reward_author";
    static final String BEHAVIOR_REWARD_ARTICLE = "reward_article";
    static final String BEHAVIOR_COLLECT = "collect";
    static final String BEHAVIOR_COMMENT = "comment";
    static final String BEHAVIOR_SHARE = "share";
    static final String BEHAVIOR_CREATE_ARTICLE = "create_article";
    static final String BEHAVIOR_MARK_AS_USEFUL = "mark_as_useful";
    static final String BEHAVIOR_MARK_AS_USELESS = "mark_as_useless";
    static final String BEHAVIOR_READ = "read";
    static final String BEHAVIOR_EDIT = "edit";
    
    static final String[] BEHAVIORS = {BEHAVIOR_WATCH, BEHAVIOR_REWARD_USER, BEHAVIOR_REWARD_ARTICLE, BEHAVIOR_COLLECT, BEHAVIOR_COMMENT, BEHAVIOR_SHARE, BEHAVIOR_CREATE_ARTICLE, BEHAVIOR_MARK_AS_USEFUL, BEHAVIOR_MARK_AS_USELESS, BEHAVIOR_READ, BEHAVIOR_EDIT};
    
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
        
        JSONObject jsonObject = JConverter.convert(request);
        if (jsonObject == null) {
            return;
        }

        try {
            int subjectID = jsonObject.getInt("subjectID");
            String predicate = jsonObject.getString("predicate");
            int objectID = jsonObject.getInt("objectID");
            String note = jsonObject.getString("note");// 
            
            if (!Arrays.asList(BEHAVIORS).contains(predicate)){
                JResponse.sendErrorMessage(1, "invalid predicate!", response);
                return;
            }

            try {
                Class.forName(JConstant.JDBC_DRIVER);
                conn = DriverManager.getConnection(JConstant.DB_URL, JConstant.USER, JConstant.PASSWORD);
                stmt = conn.createStatement();

                String sql = "INSERT INTO JUserBehaviors(subjectID, predicate, objectID, note) VALUES (" + subjectID + ", '" + predicate + "', " + objectID + ", '" + note + "')";
                stmt.executeUpdate(sql);

                JResponse.sendErrorMessage(0, "excute command success!", response);

                // Clean-up environment
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

        } catch (JSONException ex) {
            Logger.getLogger(Articles.class.getName()).log(Level.SEVERE, null, ex);
        }
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
