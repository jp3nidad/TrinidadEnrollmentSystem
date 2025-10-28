/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trinidadenrollment;

/**
 *
 * @author chicc
 */
public class Enrolled extends TrinidadEnrollmentSystem {
    private int subject_id;

    public void setsubjid(int a){
        this.subject_id = a;
    }
    public int getsubjid(){
        return this.subject_id;
    }
    public String enrollStud(int studid) {
    try {
        String checkQuery = "SELECT COUNT(*) FROM Enroll WHERE studID = " + studid + " AND subjID = " + this.subject_id;
        TrinidadEnrollmentSystem.rs = TrinidadEnrollmentSystem.st.executeQuery(checkQuery);
        TrinidadEnrollmentSystem.rs.next();
        int count = TrinidadEnrollmentSystem.rs.getInt(1);

        if (count > 0) {
            return "Student ID:" + studid + " has already enrolled in Subject ID:" + this.subject_id;
        } else {
            String insertQuery = "INSERT INTO Enroll (studID, subjID) VALUES (" + studid + ", " + this.subject_id + ")";
            TrinidadEnrollmentSystem.st.executeUpdate(insertQuery);
            return "Student ID:" + studid + " enrolled to subject ID:" + this.subject_id;
        }
    } catch (Exception ex) {
        return "Failed to Enroll Student: " + ex.getMessage();
    }
}
    public String DropSubj(int studid) {
    String query = "DELETE FROM Enroll WHERE studID = " + studid + " AND subjID = " + this.subject_id;
    try {
        int rowsAffected = st.executeUpdate(query);
        if (rowsAffected > 0) {
            return "Student ID:" + studid + " dropped Subject ID:" + this.subject_id;
        } else {
            return "Enrollment record not found.";
        }
    } catch (Exception ex) {
        return "Failed to Drop Subject: " + ex.getMessage();
    }
}
}

