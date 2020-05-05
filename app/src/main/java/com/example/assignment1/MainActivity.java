package com.example.assignment1;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Struct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMG = 1;
    private static final int REQUEST_TAKE_PHOTO = 228;
    private static final int MY_CAMERA_PERMISSION_CODE = 4192;
    private static final int MY_PERMISSION_ALL = 1;
    private Uri mPicCaptureUri = null;
    private String currentImageName = null;
    private String currentPhotoPath = null;
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

        //read photos from gallery
        readPhotoGallery();

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

        //Take photo button clicked
        snapButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v){
                onTakePhotoClicked(v);
            }
        });
    }

    //Check whether all the permissions has been granted
    public static boolean hasPermissions(Context context, String... permissions){
        if(context != null && permissions != null){
            for(String permission: permissions){
                if(ActivityCompat.checkSelfPermission((Context) context, permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }


    //Take Photo Button Listener
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onTakePhotoClicked(View v){
        String[] permissionRequests = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE };
        if(!hasPermissions(this, permissionRequests)){
            ActivityCompat.requestPermissions(this, permissionRequests, MY_PERMISSION_ALL);
        }else{
            dispatchTakePictureIntent();
        }
    }

    //Handle the permissions request response
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults){
        switch(requestCode){
            case MY_PERMISSION_ALL:{
                if(grantResults.length > 0){
                    if(grantResults.length > 0){
                        for(String per : permissionsList){
                            if(grantResults[0] == PackageManager.PERMISSION_GRANTED
                                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                                    && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                                dispatchTakePictureIntent();
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    //Start the camera external activity and handle image intent
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Return the activity component to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create the File where the photo should go

            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.errorCreatingImage, Toast.LENGTH_LONG).show();
            }

            //Continue only if the File was successfully created
            if(photoFile != null){
                ContentValues values = new ContentValues(3);
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + ".jpg";
                String displayName =  imageFileName;

                values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
                values.put(MediaStore.Images.Media.TITLE, imageFileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                //get a file reference
                Uri insertUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                mPicCaptureUri = insertUri;
                try{
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, insertUri);
                    currentPhotoPath = photoFile.getAbsolutePath();
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }catch (ActivityNotFoundException e){
                    e.printStackTrace();
                }

            }
        }
    }

    //Create an image file named by timestamp and save to path
    private File createImageFile() throws IOException{

        //The public picture director
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //Create an image file name by timestamp
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";

        //put together the directory and the timestamp to make a unique image location
        File image = File.createTempFile(
                imageFileName,  /*prefix*/
                ".jpg",  /*suffix*/
                storageDir     /*directory*/
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        currentImageName = imageFileName;
        return image;
    }

    //Start the gallery external activity and handle image intent
    private void readPhotoGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent,RESULT_LOAD_IMG);
    }

    public static String getFileName(Context context, Uri uri){
        String result = null;
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                //int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                result = cursor.getString(columnIndex);
                //Bitmap selectFile = BitmapFactory.decodeFile(path);
                //imageView.setImageBitmap(selectFile);
                //currentImageCaption.setText(cursor.getString(nameIndex));
            }
            cursor.close();
        }
        return result;
    }
    //Return the encoded photo from either gallery or camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Return the image from gallery to bitmap (gallery)
        if(requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK){
            //try{

                /*Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(selectedImage);
                currentImageCaption.setVisibility(View.VISIBLE);*/

                /*
                String picturePath = getPath(this, imageUri);
                Bitmap selectFile = BitmapFactory.decodeFile(picturePath);
                imageView.setImageBitmap(selectFile);
                */
                Uri imageUri = data.getData();
                final InputStream imageStream;
                try {
                    imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageView.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                String imageName = getFileName(this, imageUri);
                currentImageCaption.setText(imageName);
            /*} catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.errorReadingGalleryPhoto, Toast.LENGTH_LONG).show();
            }*/

        //Return the encoded photo as a small bitmap under the key "data" (camera)
        }else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {

            try {
                InputStream imageStream = getContentResolver().openInputStream(mPicCaptureUri);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(bitmap);
                //Hidden the image caption if no image yet
                currentImageCaption.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            //add timestamp
            //getTimeStamp();
            currentImageCaption.setText(currentImageName);
        }

        //If no recent photo
        else{
            Toast.makeText(this, R.string.NoPhotoChosen, Toast.LENGTH_LONG).show();
            currentImageCaption.setVisibility(View.INVISIBLE);
        }
    }

    //Decode the image scale
    private BitmapFactory.Options setPicBitmapFactoryOption(){
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return bmOptions;
    }

    //Get current timestamp when taking photo
    private void getTimeStamp(){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
        currentImageCaption.setText(timeStamp);
    }

    // Called when the user taps the Search button
    public void onSearchClick(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

}
