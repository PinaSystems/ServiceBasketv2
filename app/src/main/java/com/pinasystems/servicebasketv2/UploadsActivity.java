package com.pinasystems.servicebasketv2;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadsActivity extends AppCompatActivity {

    ImageView imageView;
    VideoView videoView;
    Button selectFile,uploadFile;

    String dialogMessage;

    boolean isImage;
    private int PICK_IMAGE_REQUEST = 1;
    private int SELECT_VIDEO = 3;
    private String request_id;

    private static final int STORAGE_PERMISSION_CODE = 123;
    private static final String FILE_UPLOAD_URL = "http://www.pinasystemsdb.890m.com/sbv2php/uploadFiles.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploads);
        request_id = ((DataBank)getApplication()).getRequest_id();
        Log.w("REQID",request_id);
        imageView = (ImageView) findViewById(R.id.image_preview);
        videoView = (VideoView) findViewById(R.id.video_preview);

        selectFile = (Button) findViewById(R.id.select_video);
        selectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogMessage = "Which type of file to upload.";
                fileDialog();
            }
        });

        uploadFile = (Button) findViewById(R.id.upload_file);
        uploadFile.setEnabled(false);
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog();
                if (isImage) {
                    filetype = "image";
                } else {
                    filetype = "video";
                }
                mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                build = new android.support.v7.app.NotificationCompat.Builder(getApplicationContext());
                build.setContentTitle("Service Basket")
                        .setContentText("Upload in progress")
                        .setSmallIcon(R.drawable.upload_arrow);

                new UploadVideo().execute();

            }
        });
        // Load an ad into the AdMob banner view.
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);

        Button btnsubmit = (Button) findViewById(R.id.submit);
        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnDialog();
            }
        });

    }
    private void fileDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialogMessage);
        builder.setPositiveButton("Video File", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogMessage = "Choose Video file from:";
                choiceDialog();
                isImage = false;
            }
        });
        builder.setNeutralButton("Image File", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Checking camera availability
                dialogMessage = "Choose Image file from:";
                choiceDialog();
                isImage = true;
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void choiceDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(dialogMessage);
        builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(isImage){
                    chooseImage();
                }else{
                    chooseVideo();
                }
            }
        });
        builder.setNeutralButton("Open Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Checking camera availability
                if (!isDeviceSupportCamera()) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry! Your device doesn't support camera",
                            Toast.LENGTH_LONG).show();
                }else{
                    if(isImage){
                        captureImage();
                    }else{
                        recordVideo();
                    }
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    private void chooseImage() {
        requestStoragePermission();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a Video "), SELECT_VIDEO);
    }
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final String TAG = UploadsActivity.class.getSimpleName();

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final String IMAGE_DIRECTORY_NAME = "Service Basket";
    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Launching camera app to record video
     */
    private void recordVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);

        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file
        // name

        // start the video capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }


    Uri fileUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            Log.e("IMAGEPATH",fileUri.getPath());
            String scheme = fileUri.getScheme();
            if(scheme.equalsIgnoreCase("file")){
                File imageFile = new File(fileUri.getPath());
                Uri content_uri = null;
                try {
                    content_uri = Uri.parse(
                            MediaStore.Images.Media.insertImage(
                                    getContentResolver(),
                                    imageFile.getAbsolutePath(), null, null));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (content_uri != null) {
                    Log.e("CONTENTURI",content_uri.getPath());
                    filePath = getPath(content_uri);
                    previewMedia(true);
                    Log.e("FILEPATH",filePath);
                }

            }else if (scheme.equalsIgnoreCase("content")){
                filePath =getPath(fileUri);
                Log.e("FILEPATH",filePath);
                previewMedia(true);

            }else{
                Toast.makeText(getApplicationContext(),"Unable to fetch file",Toast.LENGTH_LONG).show();
            }

        }else if (requestCode == SELECT_VIDEO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            String scheme = fileUri.getScheme();
            if(scheme.equalsIgnoreCase("file")){
                filePath = fileUri.getPath();
                previewMedia(false);

            }else if (scheme.equalsIgnoreCase("content")){
                filePath =getVideoPath(fileUri);
                Log.e("FILEPATH",filePath);
                previewMedia(false);


            }else{
                Toast.makeText(getApplicationContext(),"Unable to fetch file",Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                filePath = fileUri.getPath();
                previewMedia(true);


            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }

        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // video successfully recorded
                // launching upload activity
                filePath = fileUri.getPath();
                previewMedia(false);

            } else if (resultCode == RESULT_CANCELED) {

                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();

            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    public String getPath(Uri uri) {
        String path;

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if(cursor == null){
            path = uri.getPath();
            Log.e("Cursor","is null");
        }else{
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            assert cursor != null;
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

        }
        return path;
    }

    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoPath(filePath);
            // start playing
            videoView.start();
        }
        uploadFile.setEnabled(true);
    }

    public void Dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setTitle("Uploading File");
        builder.setMessage("Please check the notification bar for status.");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    String filePath;

    public String getVideoPath(Uri contentUri) {
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();

            String mString = cursor.getString(column_index);
            cursor.close();
            return mString;

        }
    }

    private NotificationManager mNotifyManager;
    private android.support.v7.app.NotificationCompat.Builder build;
    int id = 1;
    String response;
    File sourceFile;
    int totalSize = 0;
    String filetype;

    private class UploadVideo extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Displays the progress bar for the first time.
            build.setProgress(100, 0, false);
            mNotifyManager.notify(id, build.build());
            sourceFile = new File(filePath);
            totalSize = (int) sourceFile.length();
            Toast.makeText(getApplicationContext(),"Uploading File",Toast.LENGTH_LONG).show();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress
            build.setProgress(100, values[0], false);
            mNotifyManager.notify(id, build.build());
            super.onProgressUpdate(values);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                BufferedInputStream bufInput = new BufferedInputStream(new FileInputStream(sourceFile));
                URL url = new URL(FILE_UPLOAD_URL);

                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                String Filename = sourceFile.getName();
                int bytesRead;
                byte[] buffer;

                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);

                conn.setDoOutput(true);

                conn.setUseCaches(false);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"" + filetype + "\";filename=\"" + Filename
                        + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                buffer = new byte[1024];
                int progress = 0;
                bytesRead = bufInput.read(buffer);
                while (bytesRead != -1) {
                    dos.write(buffer, 0, bytesRead);
                    dos.flush();
                    progress += bytesRead;
                    publishProgress((progress * 100) / totalSize); // sending progress percent to publishProgress
                    bytesRead = bufInput.read(buffer);
                }
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"reqid\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes(request_id);
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                Log.d("BuffrerReader", "" + in);

                if (in != null) {
                    response = convertStreamToString(in);
                    Log.e("FINAL_RESPONSE-LENGTH", "" + response.length());
                    Log.e("FINAL_RESPONSE", response);
                }

                bufInput.close();
                dos.flush();
                dos.close();
            } catch (IOException ioe) {
                Log.e("Image upload", "error: " + ioe.getMessage(), ioe);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if(response == null){
                response = "Failed to upload. Check internet connection";
            }else{
                if (response.startsWith("0")) {
                    Toast.makeText(getApplicationContext(),
                            "Upload error", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "File Uploaded successfully", Toast.LENGTH_SHORT)
                            .show();
                }
            }
            build.setContentText(response);
            // Removes the progress bar
            build.setProgress(0, 0, false);
            mNotifyManager.notify(id, build.build());
        }
    }

    public static String convertStreamToString(BufferedReader is)
            throws IOException {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {

                while ((line = is.readLine()) != null) {
                    sb.append(line).append("");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    //Requesting permission
    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)

            return;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }


    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onBackPressed() {
        returnDialog();
    }

    private void returnDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Finished uploading");
        builder.setMessage("Go back to Main Menu?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String account = ((DataBank)getApplication()).getAccount();
                if(account.equalsIgnoreCase("provider")){
                    Intent intent = new Intent(getApplicationContext(), ProviderMainActivity.class);
                    startActivity(intent);
                    finish();
                }else if (account.equalsIgnoreCase("requester")){
                    Intent intent = new Intent(getApplicationContext(),RequesterMainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.e("TAG","add more files");
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
