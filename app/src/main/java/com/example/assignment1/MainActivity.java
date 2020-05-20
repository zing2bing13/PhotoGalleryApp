package com.example.assignment1;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.assignment1.Models.ImageExifModel;
import com.example.assignment1.Util.Filter.Filter;
import com.example.assignment1.Util.Filter.ImageFilter;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private static final int RESULT_LOAD_IMG = 1;
    private static final int REQUEST_TAKE_PHOTO = 228;
    private static final int MY_PERMISSION_ALL = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 101;
    static final int GET_FILTERS = 3;
    private Uri mPicCaptureUri = null;
    private String currentImageName = null;
    private String currentPhotoPath = null;
    private int photoLocation = -1;
    private ImageView imageView;
    private EditText currentImageCaption;
    private TextView currentTimeStamp;
    private Button previousPhotoBtn;
    private Button nextPhotoBtn;
    private Button tagBtn;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private Location currentLocation;
    private LocationCallback locationCallback;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final int REQUEST_GRANT_PERMISSION = 2;
    private TextView longitude;
    private TextView latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set layout components
        this.imageView = (ImageView) this.findViewById(R.id.imageView);
        this.currentImageCaption = (EditText) this.findViewById(R.id.imageCaption);
        this.currentTimeStamp = (TextView) this.findViewById(R.id.timeStamp);
        FloatingActionButton snapButton = (FloatingActionButton) this.findViewById(R.id.fab);
        Button galleryButton = (Button) this.findViewById(R.id.gallery);
        this.previousPhotoBtn = (Button) this.findViewById(R.id.buttonLeft);
        this.nextPhotoBtn = (Button) this.findViewById(R.id.buttonRight);
        this.longitude = (TextView) this.findViewById(R.id.longitude);
        this.latitude = (TextView) this.findViewById(R.id.latitude);
        BottomNavigationView bottomNav = (BottomNavigationView) findViewById(R.id.bottom_nav_view);

        //read photos from gallery
        //readPhotoGallery();

        //gallery button clicked
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readPhotoGallery();
            }
        });

        //previous photo button clicked
        previousPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPreviousPhotoBtnClicked(v);
            }
        });

        //next photo button clicked
        nextPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextPhotoBtnClicked(v);
            }
        });


        //Take photo button clicked
        snapButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                onTakePhotoClicked(v);
            }
        });


        /*//empty default image caption when focusing
        currentImageCaption.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String chk = currentPhotoPath.toString();
                if(hasFocus == true){
                    if(currentImageCaption.getText().toString().compareTo(chk)==0){
                        currentImageCaption.setText("");
                    }
                }
            }
        });*/

        currentImageCaption.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (currentPhotoPath != null) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        setExifAttr(currentPhotoPath,
                                ExifInterface.TAG_IMAGE_DESCRIPTION,
                                currentImageCaption.getText().toString());

                        return false;
                    }
                    return false;
                } else {
                    currentImageCaption.clearFocus();
                    return false;
                }
            }
        });

        nextPhotoBtn.setVisibility(View.INVISIBLE);
        previousPhotoBtn.setVisibility(View.INVISIBLE);

        longitude.setVisibility(View.INVISIBLE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        settingsCheck();


        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tagButton:
                        setLocationListener();
                        longitude.setVisibility(View.VISIBLE);
                        latitude.setVisibility(View.VISIBLE);
                        break;
                    case R.id.shareButton:
                        onShareBtnClicked();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_GRANT_PERMISSION);
        }
        if(locationCallback==null)
            buildLocationCallback();
        if(currentLocation==null)
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setLocationListener(){
        getLocation();
        latitude.setText("latitude "+currentLocation.getLongitude());
        longitude.setText("longitude "+currentLocation.getLongitude());
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    // Check for location settings
    public void settingsCheck() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                Log.d("TAG", "onSuccess: settingsCheck");
                getCurrentLocation();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    Log.d("TAG", "onFailure: settingsCheck");
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_GRANT_PERMISSION);
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("TAG", "onSuccess: getLastLocation");
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            currentLocation=location;
                            //Log.d("TAG", "onSuccess:latitude "+location.getLatitude());
                            //Log.d("TAG", "onSuccess:longitude "+location.getLongitude());
                            //longitude.setText("latitude "+location.getLatitude() + " longitude "+location.getLongitude());
                        }else{
                            Log.d("TAG", "location is null");
                            buildLocationCallback();
                        }
                    }
                });
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    currentLocation=location;
                    //Log.d("TAG", "onLocationResult: "+currentLocation.getLatitude());
                    //Log.d("TAG", "onSuccess:longitude "+currentLocation.getLongitude());
                    //latitude.setText("latitude "+currentLocation.getLongitude());
                    //longitude.setText("longitude "+currentLocation.getLongitude());
                }
            };
        };
    }

    public static String locationStringFromLocation(Location location) {
        return Location.convert(location.getLatitude(), Location.FORMAT_DEGREES)
                + " " + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
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

    //Left arrow button listener
    public void onPreviousPhotoBtnClicked(View v){
        try {
            File file = new File(currentPhotoPath);
            String filename = file.getName();
            getPhotoFromArray("back", filename);
        } catch (Exception e) {
        }
    }

    //Right arrow button listener
    public void onNextPhotoBtnClicked(View v){
        try {
            File file = new File(currentPhotoPath);
            String filename = file.getName();
            getPhotoFromArray("fwd", filename);
        } catch (Exception e){
        }
    }

    //Share button listener
    public void onShareBtnClicked() {
        String FILES_AUTHORITY = "com.example.assignment1.fileprovider";

        try {
            File file = new File(currentPhotoPath);
            Uri uriToImage = FileProvider.getUriForFile(
                    getApplicationContext(),
                    FILES_AUTHORITY,
                    file);

            Intent shareIntent = ShareCompat.IntentBuilder.from(this).setStream(uriToImage).getIntent();

            // Provide read access
            shareIntent.setData(uriToImage);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //Intent shareIntent = new Intent();
            //shareIntent.setAction(Intent.ACTION_SEND);
            //shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
            //shareIntent.setType("image/jpeg");

            startActivity(Intent.createChooser(shareIntent, "Share image to..."));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Take Photo Button Listener
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onTakePhotoClicked(View v){
        String[] permissionRequests = {Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(!hasPermissions(this, permissionRequests)){
            ActivityCompat.requestPermissions(this, permissionRequests, MY_PERMISSION_ALL);
        }else{
            dispatchTakePictureIntent();
        }
    }

    //Handle the permissions request response
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults){

        switch(requestCode){
            case MY_PERMISSION_ALL:{
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

    //Start the camera external activity and handle image intent
    @RequiresApi(api = Build.VERSION_CODES.M)
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
                ContentValues values = new ContentValues(5);
                /*
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());*/
                Long lastmodified= photoFile.lastModified();
                //String dateTaken = getTimeStamp(this, date);
                Date d = new Date(lastmodified);
                String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(d);
                String imageFileName = "JPEG_" + timeStamp + ".jpg";

                //place metadata to values collection
                values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
                values.put(MediaStore.Images.Media.DATE_TAKEN, timeStamp);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                //get a file reference
                Uri insertUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                mPicCaptureUri = insertUri;

                try {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, insertUri);
                    currentPhotoPath = photoFile.getAbsolutePath();
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //get all filepath in the picture folder
    protected ArrayList<String> getAllFilePaths(){
        Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.ImageColumns.DATA};
        Cursor c = null;
        SortedSet<String> dirList = new TreeSet<String>();
        ArrayList<String> resultIAV = new ArrayList<String>();

        String[] directories = null;
        if (u != null)
        {
            c = getContentResolver().query(u, projection, null, null, null);
        }

        if ((c != null) && (c.moveToFirst()))
        {
            do
            {
                String tempDir = c.getString(0);
                tempDir = tempDir.substring(0, tempDir.lastIndexOf("/"));
                try{
                    dirList.add(tempDir);
                }
                catch(Exception e)
                {
                }

            }
            while (c.moveToNext());
            directories = new String[dirList.size()];
            dirList.toArray(directories);
        }
        for(int i=0;i<dirList.size();i++)
        {
            File imageDir = new File(directories[i]);
            File[] imageList = imageDir.listFiles();
            if(imageList == null)
                continue;
            for (File imagePath : imageList) {
                try {
                    if(imagePath.isDirectory())
                    {
                        imageList = imagePath.listFiles();
                    }
                    if ( imagePath.getName().contains(".jpg")|| imagePath.getName().contains(".JPG")
                            || imagePath.getName().contains(".jpeg")|| imagePath.getName().contains(".JPEG")
                            || imagePath.getName().contains(".png") || imagePath.getName().contains(".PNG")
                            || imagePath.getName().contains(".gif") || imagePath.getName().contains(".GIF")
                            || imagePath.getName().contains(".bmp") || imagePath.getName().contains(".BMP")
                    )
                    {
                        String path= imagePath.getAbsolutePath();
                        resultIAV.add(path);
                    }
                }
                //  }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return resultIAV;
    }

    //Create an image file named by timestamp and save to path
    private File createImageFile() throws IOException{

        //The public picture director
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //Create an image file name by timestamp
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm").format(new Date());
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

    //get image caption from file path
    private static String getFileName(Context context, Uri uri){
        String result = null;
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DISPLAY_NAME);
        if(cursor != null){
            if(cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                result = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return result;
    }
    private static String getTimeStamp(String filepath){
        File file = new File(filepath);
        //get image taken timestamp to textview
        Long lastmodified= file.lastModified();
        //String dateTaken = getTimeStamp(this, date);
        Date d = new Date(lastmodified);
        String dateTaken = new SimpleDateFormat("yyyy/MM/dd HH:mm").format(d);
        return dateTaken;
    }

    /*
    private String getImageLocation(Context context, Uri uri){
        String result = null;
        String[] projection = {MediaStore.Images.Media.DESCRIPTION};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, MediaStore.Images.Media.DESCRIPTION);
        if(cursor != null){
            if(cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DESCRIPTION);
                result = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return result;
    }*/

    //get image file path from file path
    private static String getFilePath(Context context, Uri uri){
        String result = null;
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                result = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        return result;
    }

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

    //Return the encoded photo from either gallery or camera
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            //Return the image from gallery to bitmap (gallery)
            getPhotoFromGallery(data);
            longitude.setVisibility(View.INVISIBLE);
            latitude.setVisibility(View.INVISIBLE);

        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            //Return the encoded photo as a small bitmap under the key "data" (camera)

            try {
                InputStream imageStream = getContentResolver().openInputStream(mPicCaptureUri);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream, null, setPicBitmapFactoryOption());
                imageView.setImageBitmap(bitmap);

                //Hidden the image caption if no image yet
                currentImageCaption.setVisibility(View.VISIBLE);
                nextPhotoBtn.setVisibility(View.VISIBLE);
                previousPhotoBtn.setVisibility(View.VISIBLE);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //add timestamp
            currentImageCaption.setText(currentImageName);

            //get image taken timestamp to textview
            String dateTaken = getTimeStamp(currentPhotoPath);
            currentTimeStamp.setText(dateTaken);
        } else if (requestCode == GET_FILTERS && resultCode == Activity.RESULT_OK) {
            //Intent result = getIntent();
            if (data != null) {
                ImageFilter filter = (ImageFilter) data.getSerializableExtra("Filter");
                // Set filters
                if (filter != null) {
                    applyFilters(filter);
                }
            }
            Toast.makeText(this, R.string.NoPhotoChosen, Toast.LENGTH_LONG).show();
            longitude.setVisibility(View.INVISIBLE);
            latitude.setVisibility(View.INVISIBLE);
        }

        else if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK){
            getCurrentLocation();
        }
        else if(requestCode==REQUEST_CHECK_SETTINGS && resultCode==RESULT_CANCELED){
            Toast.makeText(this, "Please enable Location settings...!!!", Toast.LENGTH_SHORT).show();
        }
        //If no recent photo
        else{
            Toast.makeText(this, R.string.NoPhotoChosen, Toast.LENGTH_LONG).show();
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
        }
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, GET_FILTERS);
    }

    private String getExifAttr(String path, String tag) {
        try {
            ExifInterface exif = new ExifInterface(path);
            return String.valueOf(exif.getAttribute(tag));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setExifAttr(String path, String tag, String value) {
        try {
            ExifInterface exif = new ExifInterface(path);
            exif.setAttribute(tag, value);
            exif.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getPhotoFromArray(String dir, String filename) {
        ArrayList<String> allImagePaths = getAllFilePaths();

        if(currentPhotoPath!=null){
            photoLocation = allImagePaths.indexOf(currentPhotoPath);
        }

        //not the last image
        if (dir.compareTo("back") == 0) {
            if (photoLocation >= 1) {
                photoLocation--;
            }
        } else {
            if(photoLocation <(allImagePaths.size()-1)) {
                photoLocation++;
            }
        }

        currentPhotoPath = allImagePaths.get(photoLocation);
        getPhotoMeta(allImagePaths.get(photoLocation), filename);
    }

    public void getPhotoFromGallery(Intent data) {
        Uri imageUri = data.getData();
        mPicCaptureUri = imageUri;
        currentPhotoPath = getFilePath(this, imageUri);
        String imageName = getFileName(this, imageUri);

        nextPhotoBtn.setVisibility(View.VISIBLE);
        previousPhotoBtn.setVisibility(View.VISIBLE);
        getPhotoMeta(currentPhotoPath, imageName);
    }

    public void getPhotoMeta(String path, String name) {
        Bitmap selectedImage = BitmapFactory.decodeFile(currentPhotoPath, setPicBitmapFactoryOption());
        imageView.setImageBitmap(selectedImage);

        String exifCaption = getExifAttr(path, ExifInterface.TAG_IMAGE_DESCRIPTION);
        String exifDateTime = getExifAttr(path, ExifInterface.TAG_DATETIME);

        //put image caption to editview
        if(exifCaption != null && !exifCaption.isEmpty()) {

            if (exifCaption.compareTo("null") == 0) {
                currentImageCaption.setText(name);
            } else {
                currentImageCaption.setText(exifCaption);
            }
        } else {
            currentImageCaption.setText(name);
        }

        //get image taken timestamp to textview
        if(exifDateTime != null && !exifDateTime.isEmpty()) {
            if (exifDateTime.compareTo("null") == 0) {
                currentTimeStamp.setText(getTimeStamp(currentPhotoPath));
            } else {
                currentTimeStamp.setText(exifDateTime);
            }
        } else {
            currentTimeStamp.setText(getTimeStamp(currentPhotoPath));
        }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void applyFilters(ImageFilter filter) {
        List<ImageExifModel> files = getFilePaths(this)
                .stream()
                .map(file -> new ImageExifModel(file, getExifInterface(file)))
                .collect(Collectors.toList());

        List<ImageExifModel> results = (List<ImageExifModel>) Filter.ApplyFilters(files, filter.GetFilters());

        if(!results.isEmpty()) {
            ImageExifModel result = results.get(0);

            try {
                //currentPhotoPath = getFilePath(this, Uri.parse(result.FilePath));
                File file = new File(result.FilePath);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageView.setImageBitmap(bitmap);

                currentImageCaption.setVisibility(View.VISIBLE);
                nextPhotoBtn.setVisibility(View.VISIBLE);
                previousPhotoBtn.setVisibility(View.VISIBLE);
                currentImageCaption.setText(result.ExifData.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION));
                currentTimeStamp.setText("");
                imageView.setVisibility(View.VISIBLE);
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
        else {
            currentImageCaption.setVisibility(View.INVISIBLE);
            nextPhotoBtn.setVisibility(View.INVISIBLE);
            previousPhotoBtn.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
        }
    }
}
