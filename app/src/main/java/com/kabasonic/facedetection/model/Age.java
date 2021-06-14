package com.kabasonic.facedetection.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Age {
    @SerializedName("value")
    @Expose
    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
