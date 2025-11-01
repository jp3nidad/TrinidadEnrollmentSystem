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
public class Teachers extends TrinidadEnrollmentSystem {
    int teachID;
    String teachName;
    String teachAddress;
    String teachContactNo;
    String teachDepartment;

    Teachers() {
        connectDB();
    }

    public void connectDB() {
        TrinidadEnrollmentSystem.DBConnect();
    }

    /**
     * Saves the teacher record and creates a corresponding MySQL user
     * with automatic privilege grants.
     * @param teachName Teacher's name
     * @param teachAddress Teacher's address
     * @param teachContactNo Teacher's contact number
     * @param teachDepartment Teacher's department
     * @return The newly created Tid (Teacher ID) or -1 on failure.
     */
public int saveRecord(String teachName, String teachAddress, String teachContactNo, String teachDepartment) {
    int nextTeachID = -1;
    String newUsername = null;
    String newPassword = null;
    String dbName = TrinidadEnrollmentSystem.db;

    try {
        // --- 1. Determine next Teacher ID ---
        String idQuery = "SELECT MAX(Tid) FROM Teachers";
        TrinidadEnrollmentSystem.rs = TrinidadEnrollmentSystem.st.executeQuery(idQuery);

        nextTeachID = 3001; // default starting ID
        if (TrinidadEnrollmentSystem.rs.next()) {
            int currentMaxID = TrinidadEnrollmentSystem.rs.getInt(1);
            if (currentMaxID > 0) {
                nextTeachID = currentMaxID + 1;
            }
        }

        // --- 2. Insert Teacher Record ---
        String insertQuery = "INSERT INTO Teachers (Tid, Tname, Tadd, Tcontact, Tdept) VALUES (" +
                nextTeachID + ",'" + teachName + "','" + teachAddress + "','" + teachContactNo + "','" + teachDepartment + "')";
        TrinidadEnrollmentSystem.st.executeUpdate(insertQuery);
        System.out.println("Record saved successfully. New Teacher ID: " + nextTeachID);

        // --- 3. Prepare MySQL Credentials ---
        String newID = String.valueOf(nextTeachID);
        String sanitizedNameForUser = teachName.replaceAll("\\s+", "");
        newUsername = newID + sanitizedNameForUser;

        String rawPassword = "AdDU" + teachName;
        newPassword = rawPassword.replace("'", "''");

        // --- 4. CREATE MySQL User (Global Command, MySQL 5.x Safe) ---
        if ("root".equalsIgnoreCase(TrinidadEnrollmentSystem.uname)) {
            try (Connection adminCon = DriverManager.getConnection(
                    "jdbc:mysql://" + TrinidadEnrollmentSystem.home + ":3306/",
                    TrinidadEnrollmentSystem.uname, TrinidadEnrollmentSystem.pswd);
                 Statement adminSt = adminCon.createStatement()) {

                // 4A. Check if the user already exists
                String checkUserQuery = "SELECT COUNT(*) FROM mysql.user WHERE user='" + newUsername + "' AND host='%'";
                ResultSet rsCheck = adminSt.executeQuery(checkUserQuery);
                boolean userExists = false;
                if (rsCheck.next()) {
                    userExists = rsCheck.getInt(1) > 0;
                }

                // 4B. Create the user only if it doesn't exist
                if (!userExists) {
                    String createUserQuery = "CREATE USER '" + newUsername + "'@'%' IDENTIFIED BY '" + newPassword + "'";
                    adminSt.execute(createUserQuery);
                    System.out.println("MySQL User Created: " + newUsername);
                } else {
                    System.out.println("User '" + newUsername + "' already exists; skipping CREATE USER.");
                    // Optional: reset password for existing user
                    // String setPass = "SET PASSWORD FOR '" + newUsername + "'@'%' = PASSWORD('" + newPassword + "')";
                    // adminSt.execute(setPass);
                    // System.out.println("Password updated for existing user: " + newUsername);
                }

                // 4C. Grant privileges safely
                String grantQuery = "GRANT SELECT, INSERT, UPDATE ON `" + dbName + "`.* TO '" + newUsername + "'@'%'";
                adminSt.execute(grantQuery);
                System.out.println("Privileges granted (or ensured) for user: " + newUsername);

                // 4D. Apply immediately
                adminSt.execute("FLUSH PRIVILEGES");

            } catch (SQLException userEx) {
                System.out.println("Failed to execute CREATE/GRANT USER (Global Admin Task): " + userEx.getMessage());
            }
        }

        return nextTeachID;

    } catch (Exception ex) {
        System.out.println("Failed to save teacher record: " + ex.getMessage());
        return -1;
    }
}

    /**
     * Deletes the teacher record and attempts to drop the corresponding MySQL user.
     * @param teacherID The Tid of the teacher to delete.
     */
    public void deleteRecord(int teacherID) {
        String teacherName = null;
        String teacherIDStr = String.valueOf(teacherID);
        String dbName = TrinidadEnrollmentSystem.db; // Get the current database name

        // --- 1. Retrieve teacher name before deletion ---
        try {
            String selectQuery = "SELECT Tname FROM Teachers WHERE Tid = " + teacherID;
            TrinidadEnrollmentSystem.rs = TrinidadEnrollmentSystem.st.executeQuery(selectQuery);
            if (TrinidadEnrollmentSystem.rs.next()) {
                teacherName = TrinidadEnrollmentSystem.rs.getString("Tname");
            }
        } catch (Exception ex) {
            System.out.println("Error retrieving teacher name for deletion: " + ex.getMessage());
        }

        // --- 2. Delete teacher record ---
        String deleteQuery = "DELETE FROM Teachers WHERE Tid=" + teacherID;
        try {
            TrinidadEnrollmentSystem.st.execute(deleteQuery);
            System.out.println("Teacher Information Deleted Successfully!");
        } catch (Exception ex) {
            System.out.println("Failed to Delete Teacher Information: " + ex.getMessage());
        }

        // --- 3. Revoke Privileges (Requires root/admin privileges) ---
        if ("root".equalsIgnoreCase(TrinidadEnrollmentSystem.uname) && teacherName != null) {
            String sanitizedName = teacherName.replaceAll("\\s+", "");
            String usernameToRevoke = teacherIDStr + sanitizedName;

            Connection adminCon = null;
            try {
                adminCon = DriverManager.getConnection(
                    // Use the correct static constant HOME_IP
                    "jdbc:mysql://" + TrinidadEnrollmentSystem.home + ":3306/",
                    TrinidadEnrollmentSystem.uname, TrinidadEnrollmentSystem.pswd
                );

                try (Statement adminSt = adminCon.createStatement()) {
                    // *** MODIFIED LOGIC: REVOKE privileges ***
                    // Revoke the same privileges that were granted in saveRecord
                    String revokeQuery = "REVOKE SELECT, INSERT, UPDATE, DELETE ON `" + dbName + "`.* FROM '" + usernameToRevoke + "'@'%'";
                    adminSt.execute(revokeQuery);
                    
                    // Flush privileges to ensure the change takes effect
                    adminSt.execute("FLUSH PRIVILEGES"); 
                    
                    System.out.println("Privileges revoked on " + dbName + " for user: " + usernameToRevoke);
                }
            } catch (SQLException userEx) {
                // Catch specific error if user doesn't exist
                if (userEx.getErrorCode() == 1396) { // 1396 = Operation DROP USER failed
                     System.out.println("MySQL User " + usernameToRevoke + " does not exist (already dropped or never created).");
                } else {
                    System.out.println("Failed to revoke privileges for user " + usernameToRevoke + ": " + userEx.getMessage());
                }
            } finally {
                if (adminCon != null) {
                    try {
                        adminCon.close();
                    } catch (SQLException closeEx) {
                        System.out.println("Error closing admin connection after drop: " + closeEx.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Updates teacher record.
     */
    public void updateRecord(int teacherID, String teachName, String teachAddress, String teachContactNo, String teachDepartment) {
        String query = "UPDATE Teachers SET Tname='" + teachName + "', Tadd='" + teachAddress + "', Tcontact='" + teachContactNo + "', Tdept='" + teachDepartment + "' WHERE Tid=" + teacherID;
        try {
            TrinidadEnrollmentSystem.st.execute(query);
            System.out.println("Teacher Information Updated Successfully!");
        } catch (Exception ex) {
            System.out.println("Failed to Update Teacher Information: " + ex.getMessage());
        }
    }

    public void loadRecord() {
        // Optional: Implement similar loading logic as Students if needed
    }
}
