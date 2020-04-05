package com.example.videouploadactivity;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.provider.MediaStore;

import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;
    private Uri videoUri;
    private static final int REQUEST_CODE = 10;
    private StorageReference videoRef;
    DatabaseReference myRef;
    String m_androidId;
    Button button2,button3,button4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String format = simpleDateFormat.format(new Date());

        m_androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        videoRef = storageRef.child("videogroup"+m_androidId+"/" +format);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://princetonhive.firebaseio.com/");

        myRef = database.getReference("acknowledgment");
        button2=(Button)findViewById(R.id.button2);
        button3=(Button)findViewById(R.id.button3);
        button4=(Button)findViewById(R.id.button4);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button3.setAlpha((float)0.5);
                if(videoUri!=null)
                Toast.makeText(MainActivity.this,"Uploading the video",Toast.LENGTH_LONG).show();
                upload(v);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button4.setAlpha((float)0.5);
                download(v);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button2.setAlpha((float) 0.5);
                record(v);
            }
        });


    }

    public void upload(View view) {


        if (videoUri != null) {
            UploadTask uploadTask = videoRef.putFile(videoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Upload Failed", Toast.LENGTH_LONG)
                            .show();


                }
            }).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override

                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(MainActivity.this, "Upload Complete", Toast.LENGTH_LONG).show();
                            myRef.push().setValue(m_androidId);
                        }

                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    updateProgress(taskSnapshot);

                }
            });
        } else {

            Toast.makeText(this, "Nothing to upload", Toast.LENGTH_LONG).show();
        }

    }

    public void updateProgress(UploadTask.TaskSnapshot taskSnapshot) {
        long fileSize = taskSnapshot.getTotalByteCount();
        long uploadBytes = taskSnapshot.getBytesTransferred();
        long progress = (100 * uploadBytes) / fileSize;
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.pbar);
        progressBar.setProgress((int) progress);
    }

    public void record(View view) {


        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, REQUEST_CODE);


    }

    public void download(View view) {

        Intent i = new Intent();
        i.setType("video/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select a Video"), PICK_VIDEO_REQUEST);
    }



    protected void onActivityResult(int requestCode,int resultCode,Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        videoUri=data.getData();
        if(requestCode==REQUEST_CODE){
            if(resultCode==RESULT_OK){
                Toast.makeText(this,"Video Saved:\n"+videoUri,Toast.LENGTH_SHORT).show();

            }else if(resultCode==RESULT_CANCELED)
            {
                Toast.makeText(this, "Video recording cancelled", Toast.LENGTH_SHORT).show();
            }
            else
                {
                    Toast.makeText(this, "FAILED TO RECORD", Toast.LENGTH_SHORT).show();

                }
        }

    }
    @Override
    public void onBackPressed()
    {
        Toast.makeText(this, "Exiting", Toast.LENGTH_SHORT).show();

        this.finish();
    }

}



