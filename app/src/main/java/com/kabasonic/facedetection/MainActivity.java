package com.kabasonic.facedetection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kabasonic.facedetection.model.Age;
import com.kabasonic.facedetection.model.Attributes;
import com.kabasonic.facedetection.model.Face;
import com.kabasonic.facedetection.model.FaceDetected;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    private ImageView mainImage;
    private MaterialButton actionImageBt;
    private RecyclerView rvDataFaces;
    private FaceAdapter faceAdapter;
    private String photoPath;

    private FaceApi faceApi;

    public static final String API_KEY = "Z8wAnve8-j-79A6AMq5XWAYzS9wrn0s8";
    public static final String API_SECRET = "LVb5YVJKIcSJu8kE53Ra-lberVWDqWz8";
    public static final String BASE_URL = "https://api-us.faceplusplus.com/facepp/v3/";

    private int sizeListFaceToken = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViewComponents();
        actionImageBt.setOnClickListener(v ->
                dialogSelectedPhoto() );
        initRv();
        initApi();

    }

    // init view components on the main screen
    private void initViewComponents() {
        mainImage = findViewById(R.id.action_image);
        actionImageBt = findViewById(R.id.action_button_image);
        rvDataFaces = findViewById(R.id.rv_data_faces);
    }

    //create and manage dialog window
    private void dialogSelectedPhoto() {
        LayoutInflater inflater = getLayoutInflater();
        View viewDialog = inflater.inflate(R.layout.dialog_photo_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(viewDialog);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        LinearLayout fieldCamera = viewDialog.findViewById(R.id.selected_camera_image);
        LinearLayout fieldGallery = viewDialog.findViewById(R.id.selected_gallery_image);
        fieldCamera.setOnClickListener(v -> {
            // call function who load photo with camera and closed dialog window
            dispatchTakePhotoIntent();
            alertDialog.dismiss();
        });
        fieldGallery.setOnClickListener(v -> {
            // call function who load photo with gallery and closed dialog window
            mGetContent.launch("image/*");
            alertDialog.dismiss();
        });
    }

    //function create new image file
    private File createImageFile() throws IOException{
        @SuppressLint("SimpleDateFormat")
        String imageFileName = new SimpleDateFormat("yyyyMMddd_HHmmss").format(new Date());
        File storageDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir);
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePhotoIntent() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePicture.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException e){
                Log.e(getClass().getSimpleName(), String.valueOf(e));
            }
            if(photoFile != null){
                Uri photoURI = FileProvider.getUriForFile(
                        this,
                        "com.kabasonic.android.fileprovider",
                        photoFile);
                photoPath = photoFile.getAbsolutePath();
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                activityResultLauncher.launch(takePicture);
            }
        }
    }

    //callback with gallery
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    String imagePath = getImagePath(uri);
                    File file = new File(imagePath);
                    if(file.exists()){
                        mainImage.setImageURI(Uri.parse(imagePath));
                        sendPhotoToServer(file,uri);
                    }
                }
            });

    public String getImagePath(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }


    //callback with camera
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == Activity.RESULT_OK){
                    Log.d(TAG,"photoPath " + photoPath);
                   File file = new File(photoPath);
                   if(file.exists()){
                       mainImage.setImageURI(Uri.fromFile(file));
                       sendPhotoToServer(file, Uri.fromFile(file));
                   }
                }
            }
    );

    private void initApi(){
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                //using Interceptor for headers
                .addInterceptor(new Interceptor() {
                    @NotNull
                    @Override
                    public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {
                        Request originalRequest = chain.request();
                        Request newRequest = originalRequest.newBuilder()
                                //using once request, if need more, use .addheader()
                                //.header("Headers 1: ", "value 1")
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();

        Gson gson = new GsonBuilder().serializeNulls().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        faceApi = retrofit.create(FaceApi.class);
    }

    private void sendPhotoToServer(File imageFile, Uri fileUri) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("api_key",API_KEY)
                .addFormDataPart("api_secret",API_SECRET);
        if (imageFile.exists()) {
            builder.addFormDataPart("image_file", imageFile.getName(), RequestBody.create(MultipartBody.FORM, imageFile));
        }
        RequestBody requestBody = builder.build();
        Call<FaceDetected> call = faceApi.postImage(requestBody);
        call.enqueue(new Callback<FaceDetected>() {
            @Override
            public void onResponse(Call<FaceDetected> call, Response<FaceDetected> response) {
                if(!response.isSuccessful()){
                    Log.d(getClass().getSimpleName(), "onResponse|postList|Message" + response.message());
                    return;
                }
                //if code is 200 then post is update (PUT)
                Log.d("#","Code: " + response.code());
                FaceDetected faceDetected = response.body();
                if(faceDetected.getFaces()!=null){
                    List<String> faceTokensList = new ArrayList<>();
                    for(Face itemFace: faceDetected.getFaces()){
                        Log.d("Face","Item: " + itemFace.getFaceToken());
                        faceTokensList.add(itemFace.getFaceToken());
                    }
                    detectedAge(faceTokensList, imageFile);
                }
            }
            @Override
            public void onFailure(Call<FaceDetected> call, Throwable t) {
                Log.d(getClass().getSimpleName(), "onFailure|postsList|Message: " + t.getMessage());
            }
        });
    }

    private void detectedAge(List<String> faceToken, File imageFile){
        String singleToken;
        SystemClock.sleep(1000);
        if(sizeListFaceToken < faceToken.size()){
            singleToken = faceToken.get(sizeListFaceToken);
            sizeListFaceToken++;
        }else {
            sizeListFaceToken = 0;
            return;
        }
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("api_key",API_KEY)
                .addFormDataPart("api_secret",API_SECRET)
                .addFormDataPart("return_attributes","age")
                .addFormDataPart("face_tokens",singleToken);
        RequestBody requestBody = builder.build();

        Call<FaceDetected> call = faceApi.postFacesAge(requestBody);
        call.enqueue(new Callback<FaceDetected>() {
            @Override
            public void onResponse(Call<FaceDetected> call, Response<FaceDetected> response) {
                if(!response.isSuccessful()){
                    Log.d(getClass().getSimpleName(), "onResponse|postList|Message" + response.message());
                    return;
                }
                //if code is 200 then post is update (PUT)
                Log.d("#","Code: " + response.code());
                FaceDetected faceDetected = response.body();
                for(Face itemFace: faceDetected.getFaces()){
                    Log.d(TAG,"Age: " + itemFace.getAttributes().getAge().getValue());
                    Log.d(TAG,"Rectangle");
                    Log.d(TAG,"Top: " + itemFace.getFaceRectangle().getTop());
                    Log.d(TAG,"Left: " + itemFace.getFaceRectangle().getLeft());
                    Log.d(TAG,"Width: " + itemFace.getFaceRectangle().getWidth());
                    Log.d(TAG,"Height: " + itemFace.getFaceRectangle().getHeight());
                    faceAdapter.addItemFaceImageList(processingImage(imageFile
                            ,itemFace.getFaceRectangle().getLeft()
                            ,itemFace.getFaceRectangle().getTop()
                            ,itemFace.getFaceRectangle().getWidth()
                            ,itemFace.getFaceRectangle().getHeight()));
                    faceAdapter.addItemAttributesList(itemFace.getAttributes());
                    faceAdapter.notifyDataSetChanged();
                }


            }
            @Override
            public void onFailure(Call<FaceDetected> call, Throwable t) {
                Log.d(getClass().getSimpleName(), "onFailure|postsList|Message: " + t.getMessage());
            }
        });
        detectedAge(faceToken, imageFile);
    }

    private void initRv(){
        faceAdapter = new FaceAdapter(MainActivity.this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this,RecyclerView.VERTICAL,false);
        rvDataFaces.setHasFixedSize(true);
        rvDataFaces.setLayoutManager(layoutManager);
        rvDataFaces.setAdapter(faceAdapter);
    }

    //cropped and processing image for RecyclerView
    private Bitmap processingImage(File inputImageFile, int x, int y, int width, int height){
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(inputImageFile.getAbsolutePath(),bmOptions);
        bitmap = Bitmap.createBitmap(bitmap,x,y,width,height);
        return Bitmap.createScaledBitmap(bitmap,160,160,false);
    }
    /*
     * Należy stworzyć aplikację, która dla zdjęcia zrobionego aparatem lub załadowanego z pamięci urządzenia,
     * obliczy liczbę ludzi oraz wyświetli wynik w rozbiciu na dzieci i dorosłych.
     * Za wartość graniczną między "dzieciństwem" a "dorosłością" należy wybrać liczbę z przedziału 15-20,
     * w zależności od zastosowanego mechanizmu szacowania wieku
     * */

    /*
    API key: Z8wAnve8-j-79A6AMq5XWAYzS9wrn0s8
    API secret: LVb5YVJKIcSJu8kE53Ra-lberVWDqWz8
     */

}