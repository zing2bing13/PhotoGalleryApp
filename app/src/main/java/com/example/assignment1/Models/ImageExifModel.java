package com.example.assignment1.Models;

import android.media.ExifInterface;

public class ImageExifModel {
    public String FilePath;
    public ExifInterface ExifData;

    public ImageExifModel(String filePath, ExifInterface exifData){
        this.FilePath = filePath;
        this.ExifData = exifData;
    }
}
