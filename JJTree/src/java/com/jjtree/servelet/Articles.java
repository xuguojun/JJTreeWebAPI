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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
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
@WebServlet(name = "Articles", urlPatterns = {"/articles"})
public class Articles extends HttpServlet {

    private static final String TOP = "top";
    private static final String RECENT = "recent";
    private static final String COLLECTION = "collection";
    private static final String EDIT = "edit";
    private static final String REWARDED = "rewarded";
    private static final String REWARD = "reward";
    private static final String USEFUL = "useful";
    private static final String USELESS = "useless";
    private static final String SHARE = "share";
    private static final String FILTER_READ = "read";
    private static final String FILTER_UNREAD = "unread";
    private static final String FILTER_USEFUL = "useful";
    private static final String FILTER_USELESS = "useless";

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

        // one single article
        String pathInfo = request.getPathInfo();

        int singleArticleID = -1;
        if (pathInfo != null) {
            String[] path = pathInfo.split("/");
            singleArticleID = Integer.parseInt(path[1]);
        }

        // mutiple articles
        String category = request.getParameter("category");
        int pageSize = 0;
        int pageIndex = 0;

        if (category != null) {
            pageSize = Integer.parseInt(request.getParameter("pageSize"));
            pageIndex = Integer.parseInt(request.getParameter("pageIndex"));
        }

        // search articles
        String query = request.getParameter("q");
        if (query != null) {
            pageSize = Integer.parseInt(request.getParameter("pageSize"));
            pageIndex = Integer.parseInt(request.getParameter("pageIndex"));
        }

        try {
            // Register JDBC driver
            Class.forName(JConstant.JDBC_DRIVER);

            // Open a connection
            conn = DriverManager.getConnection(JConstant.DB_URL, JConstant.USER, JConstant.PASSWORD);

            // Execute SQL query
            stmt = conn.createStatement();
            String sql = null;

            if (singleArticleID >= 0) {
                sql = "SELECT * FROM JArticle WHERE articleID = " + singleArticleID;
            }

            if (category != null) {
                if (category.equalsIgnoreCase(TOP)) {
                    sql = "SELECT * FROM JArticle ORDER BY usefulValue DESC OFFSET " + pageSize * pageIndex + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
                }

                if (category.equalsIgnoreCase(RECENT)) {
                    sql = "SELECT * FROM JArticle ORDER BY createdAt DESC OFFSET " + pageSize * pageIndex + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
                }

                // collection
                if (category.equalsIgnoreCase(COLLECTION)
                        || category.equalsIgnoreCase(EDIT)
                        || category.equalsIgnoreCase(REWARD)
                        || category.equalsIgnoreCase(USEFUL)
                        || category.equalsIgnoreCase(USELESS)
                        || category.equalsIgnoreCase(SHARE)) {
                    sql = getSqlQuery(category, request, pageSize, pageIndex);
                }

            }

            if (query != null) {
                sql = "SELECT * FROM JArticle WHERE title IN ( SELECT title FROM JArticle WHERE UPPER(title) LIKE UPPER('%" + query + "%') ORDER BY createdAt DESC ) OFFSET " + (pageIndex * pageSize) + " ROWS FETCH NEXT " + pageSize
                        + " ROWS ONLY";
            }

            ResultSet rs = stmt.executeQuery(sql);

            JSONObject articlesObject = new JSONObject();
            JSONArray articles = new JSONArray();

            JSONArray paragraphs = new JSONArray();

            // Extract data from result set
            while (rs.next()) {
                //Retrieve by column name
                int articleID = rs.getInt("articleID");
                int authorID = rs.getInt("userID");

                boolean isPrivate = rs.getBoolean("isPrivate");
                String title = rs.getString("title");

                Timestamp createdAt = rs.getTimestamp("createdAt");
                Timestamp updatedAt = rs.getTimestamp("updatedAt");

                int usefulValue = rs.getInt("usefulValue");
                int uselessValue = rs.getInt("uselessValue");
                int viewCount = rs.getInt("viewCount");

                String accountUrl = "/accounts/" + authorID;
                JSONObject author = JServeletManager.fetchFrom(request, accountUrl);
                JSONObject article = new JSONObject();

                Statement subStatement = conn.createStatement();
                String subSql = "SELECT * FROM JParagraph WHERE articleID = " + articleID;
                ResultSet subRs = subStatement.executeQuery(subSql);

                while (subRs.next()) {
                    int paragraphID = subRs.getInt("paragraphID");
                    int position = subRs.getInt("position");

                    String type = subRs.getString("type");
                    String content = subRs.getString("content");

                    JSONObject paragraph = new JSONObject();

                    paragraph.put("paragraphID", paragraphID);
                    paragraph.put("position", position);

                    paragraph.put("type", type);
                    paragraph.put("content", content);

                    paragraphs.put(paragraph);
                }

                article.put("articleID", articleID);
                article.put("authorID", authorID);

                article.put("isPrivate", isPrivate);
                article.put("title", title);

                article.put("createdAt", createdAt);
                article.put("updatedAt", updatedAt);

                article.put("usefulValue", usefulValue);
                article.put("uselessValue", uselessValue);
                article.put("viewCount", viewCount);

                article.put("paragraphs", paragraphs);

                article.put("author", author);

                PrintWriter writer = response.getWriter();

                // single article 
                if (singleArticleID >= 0) {
                    writer.print(article);
                    writer.flush();
                }

                if (category != null || query != null) {
                    articles.put(article);
                }
            }

            if (category != null || query != null) {
                articlesObject.put("articles", articles);
                PrintWriter writer = response.getWriter();
                writer.print(articlesObject);
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

        try {
            int accountID = jsonObject.getInt("accountID");
            boolean isPrivate = jsonObject.getBoolean("isPrivate");
            String title = jsonObject.getString("title");

            try {
                Class.forName(JConstant.JDBC_DRIVER);
                conn = DriverManager.getConnection(JConstant.DB_URL, JConstant.USER, JConstant.PASSWORD);
                stmt = conn.createStatement();

                String sql = "SELECT MAX(articleID) FROM JArticle";
                ResultSet rs = stmt.executeQuery(sql);

                int nextArticleID = 0;
                while (rs.next()) {
                    nextArticleID = rs.getInt(1) + 1;
                }

                sql = "SELECT MAX(paragraphID) FROM JParagraph";
                rs = stmt.executeQuery(sql);

                int nextParagraphID = 0;
                while (rs.next()) {
                    nextParagraphID = rs.getInt(1) + 1;
                }

                sql = "INSERT INTO JArticle(title, userID, isPrivate, articleID) VALUES ('" + title + "', " + accountID + ", " + isPrivate + ", " + nextArticleID + ")";
                stmt.executeUpdate(sql);

                JSONArray paragraphs = jsonObject.getJSONArray("paragraphs");
                for (int i = 0; i < paragraphs.length(); i++) {
                    JSONObject p = paragraphs.getJSONObject(i);

                    int position = p.getInt("position");
                    String type = p.getString("type");
                    String content = p.getString("content");

                    sql = "INSERT INTO JParagraph VALUES ('" + type + "', " + position + ", '" + content + "', " + nextParagraphID + ", " + nextArticleID + ")";
                    stmt.executeUpdate(sql);
                }

                JResponse.sendErrorMessage(0, "publish article success!", response);

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

        } catch (JSONException ex) {
            Logger.getLogger(Articles.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Articles - #1 - View Count +1 Articles - #1 - Mark as Useful Articles -
     * #1 - Mark as Useless
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doPut(req, resp); //To change body of generated methods, choose Tools | Templates.

        processRequest(req, resp);

        // one single article
        String pathInfo = req.getPathInfo();

        int articleID = -1;
        if (pathInfo != null) {
            String[] path = pathInfo.split("/");
            articleID = Integer.parseInt(path[1]);
        }

        JSONObject jsonObject = JConverter.convert(req);
        if (jsonObject == null) {
            return;
        }

        // paramaters in json
        int accountID = -1;
        int viewCount = 0;
        int usefulValue = 0;
        int uselessValue = 0;
        try {

            if (jsonObject.has("accountID")) {
                accountID = jsonObject.getInt("accountID");
            }

            if (jsonObject.has("viewCount")) {
                viewCount = jsonObject.getInt("viewCount");
            }

            if (jsonObject.has("usefulValue")) {
                usefulValue = jsonObject.getInt("usefulValue");
            }

            if (jsonObject.has("uselessValue")) {
                uselessValue = jsonObject.getInt("uselessValue");
            }
        } catch (JSONException ex) {
            Logger.getLogger(Articles.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            // Register JDBC driver
            Class.forName(JConstant.JDBC_DRIVER);

            // Open a connection
            conn = DriverManager.getConnection(JConstant.DB_URL, JConstant.USER, JConstant.PASSWORD);

            // Execute SQL query
            stmt = conn.createStatement();

            String sql = null;
            if (viewCount > 0) {
                sql = "UPDATE JArticle SET viewCount = viewCount + 1 WHERE articleID = " + articleID;
            }

            if (usefulValue > 0) {
                sql = "UPDATE JArticle SET usefulValue = usefulValue + 1 WHERE articleID = " + articleID;
            }

            if (uselessValue > 0) {
                sql = "UPDATE JArticle SET uselessValue = uselessValue + 1 WHERE articleID = " + articleID;
            }

            stmt.executeUpdate(sql);

            sql = "SELECT viewCount, usefulValue, uselessValue FROM JArticle WHERE articleID = " + articleID;

            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {
                int currentViewCount = rs.getInt(1);
                int currentUsefulValue = rs.getInt(2);
                int currentUselessValue = rs.getInt(3);

                JSONObject statistObject = new JSONObject();

//                statistObject.put("articleID", articleID);
                statistObject.put("viewCount", currentViewCount);
                statistObject.put("usefulValue", currentUsefulValue);
                statistObject.put("uselessValue", currentUselessValue);

                JResponse.sendJson(resp, statistObject);
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

    private String getSqlQuery(String category, HttpServletRequest request, int pageSize, int pageIndex) throws SQLException {

        String sql = null;
        String predicate = null;

        if (category.equalsIgnoreCase(COLLECTION)) {
            predicate = UserBehaviors.BEHAVIOR_COLLECT;
        }

        if (category.equalsIgnoreCase(EDIT)) {
            predicate = UserBehaviors.BEHAVIOR_EDIT;
        }
        
        if (category.equalsIgnoreCase(REWARD)) {
//            predicate = UserBehaviors.BEHAVIOR_REWARD_USER + "' OR predicate = '" + UserBehaviors.BEHAVIOR_REWARD_ARTICLE;
            predicate = UserBehaviors.BEHAVIOR_REWARD_ARTICLE;            
        }
        
        if (category.equalsIgnoreCase(USEFUL)) {
            predicate = UserBehaviors.BEHAVIOR_MARK_AS_USEFUL;
        }
        
        if (category.equalsIgnoreCase(USELESS)) {
            predicate = UserBehaviors.BEHAVIOR_MARK_AS_USELESS;
        }

        if (category.equalsIgnoreCase(SHARE)) {
            predicate = UserBehaviors.BEHAVIOR_SHARE;
        }
        
        int accountID = Integer.parseInt(request.getParameter("accountID"));
        sql = "SELECT objectID FROM JUserBehaviors WHERE predicate = '" + predicate + "' and subjectID = " + accountID;
        ResultSet artilesResultSet = stmt.executeQuery(sql);

        String conditions = null;
        while (artilesResultSet.next()) {
            int articleID = artilesResultSet.getInt("objectID");

            if (conditions == null) {
                conditions = "articleID = " + articleID;
            } else {
                conditions += (" OR articleID =" + articleID);
            }
        }

        sql = "SELECT * FROM JArticle WHERE " + conditions + " ORDER BY createdAt DESC OFFSET " + pageSize * pageIndex + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";

        return sql;
    }
}
