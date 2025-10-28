/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trinidadenrollment;

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
    
    Teachers(){
        connectDB();
    }
    public void connectDB(){
        TrinidadEnrollmentSystem.DBConnect();
    }
    
    public void saveRecord(String teachName, String teachAddress, String teachContactNo, String teachDepartment) {
    try {
        String idQuery = "SELECT MAX(Tid) FROM Teachers";
        TrinidadEnrollmentSystem.rs = TrinidadEnrollmentSystem.st.executeQuery(idQuery);

        int nextTeachID = 3001;
        if (TrinidadEnrollmentSystem.rs.next()) {
            int currentMaxID = TrinidadEnrollmentSystem.rs.getInt(1);
            if (currentMaxID > 0) {
                nextTeachID = currentMaxID + 1;
            }
        }

        String insertQuery = "INSERT INTO Teachers (Tid, Tname, Tadd, Tcontact, Tdept) VALUES (" + nextTeachID + ",'" + teachName + "','" + teachAddress + "','" + teachContactNo + "','" + teachDepartment + "')";
        TrinidadEnrollmentSystem.st.executeUpdate(insertQuery);
        System.out.println("Record saved successfully. New Teacher ID: " + nextTeachID);

    } catch (Exception ex) {
        System.out.println("Failed to save record: " + ex.getMessage());
    }
}
    public void deleteRecord(int teachID) {
//        TrinidadEnrollmentSystem main = new TrinidadEnrollmentSystem();
//        main.DBConnect();
        String query = "delete from Teachers where Tid=" + teachID;
        try {
            TrinidadEnrollmentSystem.st.execute(query);
            System.out.println("Information Deleted Successfully!");
        } catch (Exception ex) {
            System.out.println("Failed to Delete Information.");
        }
    }
    public void updateRecord(int teachID, String teachName, String teachAddress, String teachContactNo, String teachDepartment) {
//        TrinidadEnrollmentSystem main = new TrinidadEnrollmentSystem();
//        main.DBConnect();
        String query = "update Teachers set Tname='" + teachName + "', Tadd='" + teachAddress + "', Tcontact='" + teachContactNo + "', Tdept='" + teachDepartment + "' where Tid=" + teachID + ";";
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
