package com.kabasonic.facedetection.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Face {
    @SerializedName("attributes")
    @Expose
    private Attributes attributes;
    @SerializedName("face_token")
    @Expose
    private String faceToken;
    @SerializedName("face_rectangle")
    @Expose
    private FaceRectangle faceRectangle;

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public String getFaceToken() {
        return faceToken;
    }

    public void setFaceToken(String faceToken) {
        this.faceToken = faceToken;
    }

    public FaceRectangle getFaceRectangle() {
        return faceRectangle;
    }

    public void setFaceRectangle(FaceRectangle faceRectangle) {
        this.faceRectangle = faceRectangle;
    }

}