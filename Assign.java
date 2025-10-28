/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trinidadenrollment;

/**
 *
 * @author chicc
 */
public class Assign extends TrinidadEnrollmentSystem {
    private int subject_id;

    public void setsubjid(int a){
        this.subject_id = a;
    }
    public int getsubjid(){
        return this.subject_id;
    }
    public String AssignSubj(int tid) {
    try {
        String subjectCheckQuery = "SELECT COUNT(*) FROM Assign WHERE subjid = " + this.subject_id;
        TrinidadEnrollmentSystem.rs = TrinidadEnrollmentSystem.st.executeQuery(subjectCheckQuery);
        TrinidadEnrollmentSystem.rs.next();
        int count = TrinidadEnrollmentSystem.rs.getInt(1);

        if (count > 0) {
            return "This subject is already assigned to a teacher.";
        } else {
            String insertQuery = "INSERT INTO Assign (Tid, subjid) VALUES (" + tid + ", " + this.subject_id + ")";
            TrinidadEnrollmentSystem.st.executeUpdate(insertQuery);
            return "Teacher ID:" + tid + " assigned to Subject ID:" + this.subject_id;
        }
    } catch (Exception ex) {
        return "Failed to Assign Subject: " + ex.getMessage();
    }
}
    public String DeleteSubj(int tid) {
        String query = "DELETE FROM Assign WHERE Tid = " + tid + " AND subjid = " + this.subject_id;
        try {
            int rowsAffected = st.executeUpdate(query);
            if (rowsAffected > 0) {
                return "Teacher ID:" + tid + " un-assigned from Subject ID:" + this.subject_id;
            } else {
                return "Assignment record not found.";
            }
        } catch (Exception ex) {
            return "Failed to Delete Assignment: " + ex.getMessage();
        }
    }
}