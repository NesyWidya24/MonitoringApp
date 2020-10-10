package com.kagu.mymonitoring.entity;

public class ModuleLearn {
    String id, uName, uDp, moduleId, pTitleArticle, pDescArticle, postImg, postTime;

    public ModuleLearn() {
    }

    public ModuleLearn(String id, String uName, String uEmail, String uDp, String moduleId, String pTitleArticle, String pDescArticle, String postImg, String postTime) {
        this.id = id;
        this.uName = uName;
        this.uDp = uDp;
        this.moduleId = moduleId;
        this.pTitleArticle = pTitleArticle;
        this.pDescArticle = pDescArticle;
        this.postImg = postImg;
        this.postTime = postTime;
    }

    public String getPostImg() {
        return postImg;
    }

    public void setPostImg(String postImg) {
        this.postImg = postImg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getpTitleArticle() {
        return pTitleArticle;
    }

    public void setpTitleArticle(String pTitleArticle) {
        this.pTitleArticle = pTitleArticle;
    }

    public String getpDescArticle() {
        return pDescArticle;
    }

    public void setpDescArticle(String pDescArticle) {
        this.pDescArticle = pDescArticle;
    }
}
