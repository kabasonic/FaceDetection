package com.kabasonic.facedetection.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FaceDetected {
    @SerializedName("request_id")
    @Expose
    private String requestId;
    @SerializedName("time_used")
    @Expose
    private Integer timeUsed;
    @SerializedName("faces")
    @Expose
    private List<Face> faces = null;
    @SerializedName("image_id")
    @Expose
    private String imageId;
    @SerializedName("face_num")
    @Expose
    private Integer faceNum;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(Integer timeUsed) {
        this.timeUsed = timeUsed;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public void setFaces(List<Face> faces) {
        this.faces = faces;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public Integer getFaceNum() {
        return faceNum;
    }

    public void setFaceNum(Integer faceNum) {
        this.faceNum = faceNum;
    }
}
