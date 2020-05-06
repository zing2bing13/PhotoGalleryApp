package com.example.assignment1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.mbms.FileInfo;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignment1.Models.ImageExifModel;
import com.example.assignment1.Util.Filter.Filter;
import com.example.assignment1.Util.Filter.ImageFilter;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int GET_FILTERS = 2;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;
    private ImageView imageView;
    private EditText currentImageCaption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set layout components
        this.imageView = (ImageView)this.findViewById(R.id.imageView);
        this.currentImageCaption = (EditText)this.findViewById(R.id.imageCaption);
        ImageButton snapButton = (ImageButton)this.findViewById(R.id.snapButton);

        //empty default image caption when focusing
        currentImageCaption.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus == true){
                    if(currentImageCaption.getText().toString().compareTo("TIMESTAMP")==0){
                        currentImageCaption.setText("");
                    }
                }
            }
        });
        //set carma open function
        snapButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v){
                if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }else{
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });
    }

    //Start the camera external activity and handle image intent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Return the activity component to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //Check camera permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_CAMERA_PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }else{
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    //Return the encoded photo as a small bitmap under the key "data"
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
            //add timestamp
            getTimeStamp();
        } else if (requestCode == GET_FILTERS && resultCode == Activity.RESULT_OK) {
            //Intent result = getIntent();
            if (data != null) {
                ImageFilter filter = (ImageFilter) data.getSerializableExtra("Filter");
                // Set filters
                if (filter != null) {
                    applyFilters(filter);
                }
            }
        }
    }

    //get current timestamp when taking photo
    private void getTimeStamp(){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH::mm::ss").format(new Date());
        currentImageCaption.setText(timeStamp);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void applyFilters(ImageFilter filter) {
        List<ImageExifModel> files = getFilePaths(this)
                                    .stream()
                                    .map(file -> new ImageExifModel(file, getExifInterface(file)))
                                    .collect(Collectors.toList());

        List<ImageExifModel> results = (List<ImageExifModel>) Filter.ApplyFilters(files, filter.GetFilters());

        if(!results.isEmpty()) {
            ImageExifModel result = results.get(0);
        }
    }

    // Called when the user taps the Search button
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onSearchClick(View view) {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique

            return;
        }
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, GET_FILTERS);
    }

    // Get list of image file paths
    private static ArrayList<String> getFilePaths(Activity activity){
        Uri uri;
        Cursor cursor;
        int column_index;
        StringTokenizer st1;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA};

        cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index);
            listOfAllImages.add(absolutePathOfImage);
        }

        return listOfAllImages;
    }

    private ExifInterface getExifInterface(String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            return exif;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
