package com.example.user.ocr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    SurfaceView camera;
    TextView txt;
    CameraSource cs;
    final int RequestCameraPermissionID = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RequestCameraPermissionID:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    try {
                        cs.start(camera.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera = findViewById(R.id.vision);
        txt = findViewById(R.id.text);
        TextRecognizer tr = new TextRecognizer.Builder(getApplicationContext()).build();
        if (!tr.isOperational()) {
            Log.w("MainActivity", "Detector dependency not found");
        } else {
            cs = new CameraSource.Builder(getApplicationContext(), tr)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setRequestedFps(2.0f)
                    .setAutoFocusEnabled(true)
                    .build();
            camera.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                          ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},RequestCameraPermissionID);
                            return;
                        }
                        cs.start(camera.getHolder());
                        } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cs.stop();
                }
            });
            tr.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items =detections.getDetectedItems();
                    if(items.size() !=0){
                        txt.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder sb = new StringBuilder();
                                for (int i=0;i<items.size();i++){
                                    TextBlock item =items.valueAt(i);
                                    sb.append(item.getValue());
                                    sb.append("\n");
                                    txt.setText(sb.toString());
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}