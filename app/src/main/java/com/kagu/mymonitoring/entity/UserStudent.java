package com.kagu.mymonitoring.entity;

public class UserStudent {
    private String id;
    private String endDate;
    private String fullname;
    private String majors;
    private String nimNis;
    private String picName;
    private String projectName;
    private String schoolAddress;
    private String schoolName;
    private String schoolPhone;
    private String startDate;
    private String studentAddress;

    public UserStudent() {
    }

    public UserStudent(String id, String endDate, String fullname, String majors, String nimNis, String picName, String projectName, String schoolAddress, String schoolName, String schoolPhone, String startDate, String studentAddress) {
        this.id = id;
        this.endDate = endDate;
        this.fullname = fullname;
        this.majors = majors;
        this.nimNis = nimNis;
        this.picName = picName;
        this.projectName = projectName;
        this.schoolAddress = schoolAddress;
        this.schoolName = schoolName;
        this.schoolPhone = schoolPhone;
        this.startDate = startDate;
        this.studentAddress = studentAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getMajors() {
        return majors;
    }

    public void setMajors(String majors) {
        this.majors = majors;
    }

    public String getNimNis() {
        return nimNis;
    }

    public void setNimNis(String nimNis) {
        this.nimNis = nimNis;
    }

    public String getPicName() {
        return picName;
    }

    public void setPicName(String picName) {
        this.picName = picName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSchoolAddress() {
        return schoolAddress;
    }

    public void setSchoolAddress(String schoolAddress) {
        this.schoolAddress = schoolAddress;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolPhone() {
        return schoolPhone;
    }

    public void setSchoolPhone(String schoolPhone) {
        this.schoolPhone = schoolPhone;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStudentAddress() {
        return studentAddress;
    }

    public void setStudentAddress(String studentAddress) {
        this.studentAddress = studentAddress;
    }
}
