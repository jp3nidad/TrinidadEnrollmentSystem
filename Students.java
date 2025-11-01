package com.mycompany.trinidadenrollment;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author jpbtrinidad
 */
public class Students extends TrinidadEnrollmentSystem {
    int StudentID;
    String Name;
    String Address;
    String Contact;
    String Gender;
    String Yrlevel;

    Students(){
        connectDB();
    }
    public void connectDB(){
        TrinidadEnrollmentSystem.DBConnect();
    }

    /**
     * Saves the student record and attempts to create a corresponding MySQL user.
     * NOTE: Privileges (GRANTs) must be assigned manually by an administrator after creation.
     * @param Name Student's name (used as initial MySQL password)
     * @param Address Student's address
     * @param Contact Student's contact info
     * @param Gender Student's gender
     * @param Yrlevel Student's year level
     * @return The newly created studid (Student ID) or -1 on failure.
     */
public int saveRecord(String Name, String Address, String Contact, String Gender, String Yrlevel) {
    int nextStudID = -1;
    String newUsername = null;
    String newPassword = null;
    String dbName = TrinidadEnrollmentSystem.db;

    try {
        // --- 1. Determine next Student ID ---
        String idQuery = "SELECT MAX(studid) FROM Students";
        TrinidadEnrollmentSystem.rs = TrinidadEnrollmentSystem.st.executeQuery(idQuery);

        nextStudID = 1001;
        if (TrinidadEnrollmentSystem.rs.next()) {
            int currentMaxID = TrinidadEnrollmentSystem.rs.getInt(1);
            if (currentMaxID > 0) {
                nextStudID = currentMaxID + 1;
            }
        }

        // --- 2. Insert Student Record ---
        String insertQuery = "INSERT INTO Students (studid, studname, studadd, studcontact, studgender, yrlvl) VALUES (" +
                nextStudID + ",'" + Name + "','" + Address + "','" + Contact + "','" + Gender + "','" + Yrlevel + "')";
        TrinidadEnrollmentSystem.st.executeUpdate(insertQuery);
        System.out.println("Record saved successfully. New Student ID: " + nextStudID);

        // --- 3. Prepare User Credentials ---
        String newID = String.valueOf(nextStudID);
        String sanitizedNameForUser = Name.replaceAll("\\s+", "");
        newUsername = newID + sanitizedNameForUser;

        String rawPassword = "AdDU" + Name;
        newPassword = rawPassword.replace("'", "''");

        // --- 4. CREATE MySQL User (Global Command) ---
        if ("root".equalsIgnoreCase(TrinidadEnrollmentSystem.uname)) {
            Connection adminCon = null;
            try {
                adminCon = DriverManager.getConnection(
                        "jdbc:mysql://" + TrinidadEnrollmentSystem.home + ":3306/",
                        TrinidadEnrollmentSystem.uname, TrinidadEnrollmentSystem.pswd
                );

                try (Statement adminSt = adminCon.createStatement()) {

                    // ✅ Check first if the user already exists
                    String checkUserQuery = "SELECT COUNT(*) FROM mysql.user WHERE user='" + newUsername + "' AND host='%'";
                    ResultSet rsCheck = adminSt.executeQuery(checkUserQuery);

                    boolean userExists = false;
                    if (rsCheck.next()) {
                        userExists = rsCheck.getInt(1) > 0;
                    }

                    // ✅ Only create user if it doesn’t exist
                    if (!userExists) {
                        String createUserQuery = "CREATE USER '" + newUsername + "'@'%' IDENTIFIED BY '" + newPassword + "'";
                        adminSt.execute(createUserQuery);
                        System.out.println("MySQL User Created: " + newUsername);
                    } else {
                        System.out.println("User '" + newUsername + "' already exists, skipping CREATE USER.");
                    }

                    // ✅ Always grant privileges for the current database
                    String grantQuery = "GRANT SELECT ON `" + dbName + "`.* TO '" + newUsername + "'@'%'";
                    adminSt.execute(grantQuery);
                    System.out.println("Privileges ensured for user: " + newUsername);

                    adminSt.execute("FLUSH PRIVILEGES");
                }

            } catch (SQLException userEx) {
                System.out.println("Failed to execute CREATE USER or GRANT: " + userEx.getMessage());
            } finally {
                if (adminCon != null) {
                    try {
                        adminCon.close();
                    } catch (SQLException closeEx) {
                        System.out.println("Error closing admin connection: " + closeEx.getMessage());
                    }
                }
            }
        }

        return nextStudID;

    } catch (Exception ex) {
        System.out.println("Failed to save record: " + ex.getMessage());
        return -1;
    }
}


    /**
     * Deletes the student record and attempts to drop the corresponding MySQL user.
     * @param StudentID The ID of the student to delete.
     */
    public void deleteRecord(int StudentID) {
        String studentName = null;
        String studentIDStr = String.valueOf(StudentID);
        String dbName = TrinidadEnrollmentSystem.db; // Get the current database name

        // --- 1. Retrieve the student name before deletion ---
        try {
            String selectQuery = "SELECT studname FROM Students WHERE studid = " + StudentID;
            TrinidadEnrollmentSystem.rs = TrinidadEnrollmentSystem.st.executeQuery(selectQuery);
            if (TrinidadEnrollmentSystem.rs.next()) {
                studentName = TrinidadEnrollmentSystem.rs.getString("studname");
            }
        } catch (Exception ex) {
            System.out.println("Error retrieving student name for deletion: " + ex.getMessage());
        }

        // --- 2. Delete the student record from the table ---
        String deleteQuery = "DELETE FROM Students WHERE studid=" + StudentID;
        try {
            TrinidadEnrollmentSystem.st.execute(deleteQuery);
            System.out.println("Information Deleted Successfully!");
        } catch (Exception ex) {
            System.out.println("Failed to Delete Information: " + ex.getMessage());
            // Optionally, decide if you still want to try dropping the MySQL user
            // return; // Uncomment to stop if DB delete fails
        }

        // --- 3. Revoke Privileges (Requires root/admin privileges) ---
        if ("root".equalsIgnoreCase(TrinidadEnrollmentSystem.uname) && studentName != null) {

            String sanitizedName = studentName.replaceAll("\\s+", "");
            String usernameToRevoke = studentIDStr + sanitizedName;

            Connection adminCon = null;
            try {
                 // Connect globally to execute administrative REVOKE command
                adminCon = DriverManager.getConnection(
                    // Use the correct static constant HOME_IP
                    "jdbc:mysql://" + TrinidadEnrollmentSystem.home + ":3306/",
                    TrinidadEnrollmentSystem.uname, TrinidadEnrollmentSystem.pswd
                );

                try (Statement adminSt = adminCon.createStatement()) {
                    // --- MODIFIED LOGIC: REVOKE PRIVILEGES INSTEAD OF DROP USER ---
                    // Revoke the same privileges that were granted in saveRecord
                    String revokeQuery = "REVOKE SELECT, INSERT, UPDATE, DELETE ON `" + dbName + "`.* FROM '" + usernameToRevoke + "'@'%'";
                    adminSt.execute(revokeQuery);
                    
                    // Flush privileges to ensure the change takes effect
                    adminSt.execute("FLUSH PRIVILEGES"); 
                    
                    System.out.println("Privileges revoked on " + dbName + " for user: " + usernameToRevoke);
                }
            } catch (SQLException userEx) {
                // Catch specific error if user doesn't exist (MySQL error code 1396)
                if (userEx.getErrorCode() == 1396) {
                     System.out.println("MySQL User " + usernameToRevoke + " does not exist (already dropped or never created).");
                } else {
                    System.out.println("Failed to revoke privileges for user " + usernameToRevoke + ": " + userEx.getMessage());
                }
            } finally {
                if (adminCon != null) {
                    try {
                        adminCon.close();
                    } catch (SQLException closeEx) {
                        System.out.println("Error closing admin connection after revoke: " + closeEx.getMessage());
                    }
                }
            }
        }
    }

    public void updateRecord(int StudentID, String Name, String Address, String Contact, String Gender, String YrLevel) {
        String query = "UPDATE Students SET studname='" + Name + "', studadd='" + Address + "', studcontact='" + Contact + "', studgender='" + Gender + "', yrlvl='" + YrLevel + "' WHERE studid=" + StudentID;
        try {
            TrinidadEnrollmentSystem.st.execute(query);
            System.out.println("Information Updated Successfully!");
        } catch (Exception ex) {
            System.out.println("Failed to Update Information: " + ex.getMessage());
        }
    }
    public void loadRecord() {
        // This method doesn't seem to load anything, just prints. Consider removing or implementing.
        // System.out.println("Records Loaded Successfully!");
    }
}