package com.kabasonic.facedetection;

import com.kabasonic.facedetection.model.FaceDetected;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

public interface FaceApi {
    @POST("detect")
    Call<FaceDetected> postImage(@Body RequestBody file);

    @POST("face/analyze")
    Call<FaceDetected> postFacesAge(@Body RequestBody body);

}
