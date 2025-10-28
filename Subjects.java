/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trinidadenrollment;

/**
 *
 * @author jpbtrinidad
 */
public class Subjects extends TrinidadEnrollmentSystem {
    int subjID;
    String subjCode;
    String subjDescription;
    int subjUnits;
    String subjSchedule;
    
    Subjects(){
        connectDB();
    }
    public void connectDB(){
        TrinidadEnrollmentSystem.DBConnect();
    }
    
    public void saveRecord(String subjCode, String subjDescription, int subjUnits, String subjSchedule) {
    try {
        String idQuery = "SELECT MAX(subjid) FROM Subjects";
        TrinidadEnrollmentSystem.rs = TrinidadEnrollmentSystem.st.executeQuery(idQuery);

        int nextSubjID = 2001;
        if (TrinidadEnrollmentSystem.rs.next()) {
            int currentMaxID = TrinidadEnrollmentSystem.rs.getInt(1);
            if (currentMaxID > 0) {
                nextSubjID = currentMaxID + 1;
            }
        }

        String insertQuery = "INSERT INTO Subjects (subjid, subjcode, subjdesc, subjunits, subjsched) VALUES (" + nextSubjID + ",'" + subjCode + "','" + subjDescription + "'," + subjUnits + ",'" + subjSchedule + "')";
        TrinidadEnrollmentSystem.st.executeUpdate(insertQuery);
        System.out.println("Record saved successfully. New Subject ID: " + nextSubjID);

    } catch (Exception ex) {
        System.out.println("Failed to save record: " + ex.getMessage());
    }
}
    public void deleteRecord(int subjID) {
//        TrinidadEnrollmentSystem main = new TrinidadEnrollmentSystem();
//        main.DBConnect();
        String query = "delete from Subjects where subjid=" + subjID;
        try {
            TrinidadEnrollmentSystem.st.execute(query);
            System.out.println("Information Deleted Successfully!");
        } catch (Exception ex) {
            System.out.println("Failed to Delete Information.");
        }
    }
    public void updateRecord(int subjID, String subjCode, String subjDescription, int subjUnits, String subjSchedule) {
//        TrinidadEnrollmentSystem main = new TrinidadEnrollmentSystem();
//        main.DBConnect();
        String query = "update Subjects set subjcode='" + subjCode + "', subjdesc='" + subjDescription + "', subjunits=" + subjUnits + ", subjsched='" + subjSchedule + "' where subjid=" + subjID;
        try {
            TrinidadEnrollmentSystem.st.execute(query);
            System.out.println("Information Updated Successfully!");
        } catch (Exception ex) {
            System.out.println("Failed to Update Information.");
        }
    }
    public void loadRecord() {
        System.out.println("Records Loaded Successfully!");
    }
    
}
