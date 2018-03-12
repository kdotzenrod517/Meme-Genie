package com.dotzenrod.memegenerator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    public static final int REQUEST_CODE = 10;
    public static final int EXTERNAL_STORAGE_REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button shareButton = (Button) findViewById(R.id.button_share);
        Button photoButton = (Button) this.findViewById(R.id.camera);
        EditText editText = (EditText) this.findViewById(R.id.editText);
        EditText editText2 = (EditText) this.findViewById(R.id.editText2);

        editText.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        editText.setSelection(editText.getText().length());
        editText2.setFilters(new InputFilter[]{new InputFilter.AllCaps()});

        shareButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                sharePhoto();
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

    }

    private void sharePhoto() {
        createComposite();
        createShareIntent();
    }

    private void createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        File sharedFile = new File(getCacheDir(), "images/image.png");
        Uri imageUri = FileProvider.getUriForFile(this, "com.kristadotzenrod.memegenie.fileprovider", sharedFile);

        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/png");

        startActivity(shareIntent);
    }

    private void createComposite() {
        //create composite image
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.image_frame_layout);

        frameLayout.setDrawingCacheEnabled(true);
        frameLayout.getDrawingCache(true);
        Bitmap bitmap = frameLayout.getDrawingCache();

        File sharedFile = new File(getCacheDir(), "images");
        sharedFile.mkdirs();
        try {
            FileOutputStream stream = new FileOutputStream(sharedFile + "/image.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        frameLayout.setDrawingCacheEnabled(false);
        frameLayout.destroyDrawingCache();
    }


    public void pickPhotoFromGallery(View v) {
        askPermission();
    }

    private void createPhotoIntent() {
        Intent photoIntent = new Intent(Intent.ACTION_PICK);

        File photoDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        Uri photoURI = Uri.parse(photoDirectory.getPath());

        photoIntent.setDataAndType(photoURI, "image/*");

        startActivityForResult(photoIntent, REQUEST_CODE);
    }

    private void askPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    20);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            createPhotoIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    createPhotoIntent();

                } else {

                    Toast.makeText(this, "Gallery Permission Denied :(", Toast.LENGTH_SHORT).show();

                }
                return;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if (requestCode == REQUEST_CODE){
                Uri photoUri = data.getData();

                ImageView imageView = (ImageView)findViewById(R.id.image_view_meme);

                Picasso.get().load(photoUri).into(imageView);            }
        }

        if (requestCode == CAMERA_REQUEST && resultCode == MainActivity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ImageView imageView = (ImageView)findViewById(R.id.image_view_meme);
            imageView.setImageBitmap(photo);
        }
    }
}



