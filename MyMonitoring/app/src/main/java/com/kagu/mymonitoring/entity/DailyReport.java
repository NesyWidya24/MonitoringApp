package com.kagu.mymonitoring.entity;

public class DailyReport {
    String id;
    String key;

    String username;
    String postId;
    String pTitleScenario;
    String pTitleTC;
    String pResult;
    String postTime;
    String postImg;
    String pProjectName;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getpTitleScenario() {
        return pTitleScenario;
    }

    public void setpTitleScenario(String pTitleScenario) {
        this.pTitleScenario = pTitleScenario;
    }

    public String getpTitleTC() {
        return pTitleTC;
    }

    public void setpTitleTC(String pTitleTC) {
        this.pTitleTC = pTitleTC;
    }

    public String getpResult() {
        return pResult;
    }

    public void setpResult(String pResult) {
        this.pResult = pResult;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getpProjectName() {
        return pProjectName;
    }

    public void setpProjectName(String pProjectName) {
        this.pProjectName = pProjectName;
    }
}
