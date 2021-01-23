package com.example.eindproject;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eindproject.Retrofit.ClientRetrofit;
import com.example.eindproject.Retrofit.UploadApi;
import com.example.eindproject.needed.ProgressRequest;
import com.example.eindproject.needed.filePath;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.net.URISyntaxException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements com.example.eindproject.needed.Callback {

    private static final int PICK_FILE_REQUEST = 1000;

    UploadApi connectService;
    Button btnUpload;
    ImageView imageViewUpload;
    Uri selectedFileUri;



    ProgressDialog dialog;

    private UploadApi getAPIUpload() {
        return ClientRetrofit.getClient().create(UploadApi.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        Toast.makeText(MainActivity.this, "Permission accepted", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You should accept permission", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        // Create connect service
        connectService = getAPIUpload();

        // InitView
        btnUpload = (Button) findViewById(R.id.UploadBtn);
        imageViewUpload = (ImageView) findViewById(R.id.imageViewUpload);


        // Event for the click
        imageViewUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFile();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });
    }

    private void uploadFile() {
        if (selectedFileUri != null) {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("Uploading...");
            dialog.setIndeterminate(false);
            dialog.setMax(100);
            dialog.setCancelable(false);
            dialog.show();

            File file = null;

            try {
                file = new File(filePath.getFilePath(this, selectedFileUri));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            if (file != null) {
                final ProgressRequest requestBody = new ProgressRequest(file, this);

                final MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestBody);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectService.uploadFile(body)
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        dialog.dismiss();
                                        String michael =  response.body().replaceAll("^[\"']+|[\"']+$", "");

                                        String image_processed_link = new StringBuilder("http://192.168.0.14:5000/" +
                                                michael).toString();


                                        System.out.println(image_processed_link);

                                        Log.i("Response code:", " " + response.body());
                                        Picasso.get().load(image_processed_link).into(imageViewUpload);
                                        Toast.makeText(MainActivity.this, "Detected successfully!", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        dialog.dismiss();
                                        Toast.makeText(MainActivity.this, "File not found" + t.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                }).start();
            }
        } else {
            Toast.makeText(MainActivity.this, "Cannot upload this file !!", Toast.LENGTH_LONG).show();
        }
    }

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_FILE_REQUEST) {
                if (data != null) {
                    selectedFileUri = data.getData();
                    if (selectedFileUri != null && !selectedFileUri.getPath().isEmpty()) {
                        imageViewUpload.setImageURI(selectedFileUri);
                    } else {
                        Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    public void onProgressUpdate(int percent) {

    }
}
