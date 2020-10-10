package com.kagu.mymonitoring.entity;

public class Project {
    private String id;
    private String idProject;
    private String projectName;
    private String imgProject;
    private String projectClient;
    private String projectDesc;
    private String namePic;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdProject() {
        return idProject;
    }

    public void setIdProject(String idProject) {
        this.idProject = idProject;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getImgProject() {
        return imgProject;
    }

    public void setImgProject(String imgProject) {
        this.imgProject = imgProject;
    }

    public String getProjectClient() {
        return projectClient;
    }

    public void setProjectClient(String projectClient) {
        this.projectClient = projectClient;
    }

    public String getProjectDesc() {
        return projectDesc;
    }

    public void setProjectDesc(String projectDesc) {
        this.projectDesc = projectDesc;
    }

    public String getNamePic() {
        return namePic;
    }

    public void setNamePic(String namePic) {
        this.namePic = namePic;
    }
}
