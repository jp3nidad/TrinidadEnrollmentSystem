package com.mycompany.trinidadenrollment;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class TrinidadEnrollmentSystem {
    public static Connection con;
    public static Statement st;
    public static ResultSet rs;

    public static String db;
    public static String uname = "root";
    public static String pswd = "root";
    public static String home = "192.168.254.114";
    
        public static int currentlySelectedSubjectId = -1;  

    
    public static void main(String[] args) {
        //DBConnect();
        Login login = new Login();
        login.setVisible(true);
    }

    public static void DBConnect() {
        try {
            String school = "";

            Class.forName("com.mysql.cj.jdbc.Driver");

            if (db == null || db.trim().isEmpty()) {
                System.out.println("Warning: No database selected. Please select a database first.");
                throw new Exception("No database selected. Please choose a semester database.");
            }

            if ("root".equalsIgnoreCase(uname)) {
                con = DriverManager.getConnection(
                        "jdbc:mysql://" + home + ":3306/?zeroDateTimeBehavior=CONVERT_TO_NULL&connectTimeout=5000",
                        uname, pswd
                );
                st = con.createStatement();
                st.execute("CREATE DATABASE IF NOT EXISTS `" + db + "`");
                st.close();
                con.close();
            }

            con = DriverManager.getConnection(
                    "jdbc:mysql://" + home + ":3306/" + db + "?zeroDateTimeBehavior=CONVERT_TO_NULL&connectTimeout=5000",
                    uname, pswd
            );
            st = con.createStatement();

            System.out.println("Connected to database: " + db);
        } catch (Exception ex) {
            System.out.println("Failed to Connect: " + ex);
            ex.printStackTrace();
            throw new RuntimeException("Database connection failed: " + ex.getMessage(), ex);
        }
    }
}
