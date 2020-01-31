package com.example.faceattend;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.ConversationActions;
import android.view.textclassifier.TextLinks;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.ybq.android.spinkit.style.Wave;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
////////NOTE!!! I had to deliver this project(App+WebServer) within a month so there is a lack of OOP in this app GL :'( /////////
///////This app Takse PIC from Camera and then Sends it to A Webserver

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final int PERMISSION_CODE=1000;
    private static final int IMAGE_CAPTURE_CODE=1001;
    private Button buttonUpload;
    private Button mCaptureBtn;
    private ImageView mImageView;
    private Bitmap bitmap;
    private String contents;
    Uri image_uri;
    private String cource;
    private ProgressBar progressBar;
    private Boolean pressed = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ////////
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.cources, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        ////////

        progressBar = (ProgressBar)findViewById(R.id.spin_kit);
        progressBar.setVisibility(View.INVISIBLE);
        Wave wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);
        mCaptureBtn = findViewById(R.id.butnCapture);
        mImageView =findViewById(R.id.imageView);
        buttonUpload=findViewById(R.id.buttonUpload);

                mCaptureBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pressed=true;
                        mCaptureBtn.setClickable(false); // disable the ability to click it
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.CAMERA) ==
                                    PackageManager.PERMISSION_DENIED ||
                                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                            PackageManager.PERMISSION_DENIED) {
                                String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                requestPermissions(permission, PERMISSION_CODE);

                            } else {
                                openCamera();
                            }
                        } else {
                            openCamera();
                        }
                    }
                });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pressed==true) {
                    buttonUpload.setClickable(false); // disable the ability to click it
                    progressBar.setVisibility(View.VISIBLE);
                    uploadImage();
                }
            }
        });
    }

        private void openCamera() {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE,"New Picture");
            values.put(MediaStore.Images.Media.TITLE,"From the Camera");
            image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
            startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_CODE:}
            if(grantResults.length>0&&grantResults[0]==
                    PackageManager.PERMISSION_GRANTED){
                openCamera();
        }
            else{
                Toast.makeText(this,"Permission denied..",Toast.LENGTH_SHORT).show();
            }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
                try {
                    //Get Image from Uri
                    InputStream inputStream = getContentResolver().openInputStream(image_uri);
                    //Decode it
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    //Compress IMAGE into Smaller size while maintaining its Resoultion
                    bitmap=Bitmap.createScaledBitmap(bitmap, 240, 320, false);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
                    byte[] array = stream.toByteArray();
                    //Save Image into a String -> contents which will be passed to WebServer
                    contents = Base64.encodeToString(array,Base64.DEFAULT);
                    //Set ImageView to The taken Image
                    mImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 240, 320, false));
                  } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

                             //Upload Image to WEB SERVER USING Volley library//
    private void uploadImage() {
        StringRequest request = new StringRequest(Request.Method.POST, "http://aymankooo.pythonanywhere.com/a",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(MainActivity.this, "Image upload succeed.", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);// progressBar is a gradle from -> implementation 'com.github.ybq:Android-SpinKit:1.4.0'
                        mImageView.setImageResource(R.drawable.bb); // set ImageView to its default PIC
                        Log.d("Response DONEEEEEEEEEE", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "" +error, Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param= new HashMap<String,String>();
                param.put("img",contents);
                return param;
            }
        };request.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
            }
        });
        //requestQueue.add(request);
        Volley.newRequestQueue(MainActivity.this).add(request);
       // request.setShouldCache(false);
        Log.d("Request", request.toString());
    }


    //######Under Development#######// A List to Choose Course Name
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        cource = adapterView.getItemAtPosition(i).toString();
        //Toast.makeText(AdapterView.this, "" +cource, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}