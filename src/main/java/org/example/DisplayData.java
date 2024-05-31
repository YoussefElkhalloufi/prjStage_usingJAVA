package org.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/DisplayData") // Define the URL pattern here
public class DisplayData extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // JDBC driver name and database URL
        String JDBC_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        String DB_URL = "jdbc:sqlserver://192.168.11.103:1433;databaseName=Vocalcom";

        // Database credentials
        String USER = "sa";
        String PASS = "sa";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);

            // Open a connection
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // Execute SQL query
            String sql = "SELECT g.NOM_GRP , COUNT(i.ident) " +
                    "FROM ident i INNER JOIN MaintenantTeleope m ON i.ident = m.Ident " +
                    "INNER JOIN groupes g ON m.grp = g.Num_Camp GROUP BY g.NOM_GRP";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            // Convert ResultSet to JSON
            StringBuilder jsonData = new StringBuilder("[");
            while (rs.next()) {
                jsonData.append("{\"group\": \"" + rs.getString(1) + "\", \"count\": \"" + rs.getInt(2) + "\"},");
            }
            // Remove the last comma
            if (jsonData.charAt(jsonData.length() - 1) == ',') {
                jsonData.deleteCharAt(jsonData.length() - 1);
            }
            jsonData.append("]");

            // Set response content type
            response.setContentType("application/json");

            // Send JSON response
            PrintWriter out = response.getWriter();
            out.print(jsonData.toString());
            out.flush();
        } catch (SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}
