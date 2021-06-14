package com.kabasonic.facedetection;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kabasonic.facedetection.model.Age;
import com.kabasonic.facedetection.model.Attributes;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceAdapter extends RecyclerView.Adapter<FaceAdapter.ViewHolder> {

    private Context context;

    private List<Bitmap> faceImageList;
    private List<Attributes> attributesList;

    public FaceAdapter (Context context){
        this.context = context;
        this.faceImageList = new ArrayList<>();
        this.attributesList = new ArrayList<>();
    }

    public void addItemFaceImageList(Bitmap faceImageItem) {
        this.faceImageList.add(faceImageItem);
    }

    public void addItemAttributesList(Attributes attributesItem) {
        this.attributesList.add(attributesItem);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_detected_face,null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("Adapter","Position: " + position);
        Bitmap rowImage = faceImageList.get(position);
        Attributes attributes = attributesList.get(position);
        int rowAge = attributes.getAge().getValue();
        holder.faceImage.setImageBitmap(rowImage);
        holder.faceAge.setText(String.valueOf(rowAge));
        holder.faceId.setText(String.valueOf(position));
    }

    @Override
    public int getItemCount() {
       if(faceImageList==null)
           return 0;
       else
           return faceImageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView faceImage;
        public TextView faceId;
        public TextView faceAge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            faceImage = itemView.findViewById(R.id.item_face_image);
            faceId = itemView.findViewById(R.id.item_face_id);
            faceAge = itemView.findViewById(R.id.item_face_age);
        }
    }
}
