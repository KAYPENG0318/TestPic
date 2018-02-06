package com.wanna.testpic;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Date;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");

    private File tempFile;
    Button bt1;
    ImageView imageView;
    Uri FileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt1 = (Button)findViewById(R.id.petButton);
        bt1.setText("選擇圖片");//設定按鈕內文字


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                TextView tv = (TextView)findViewById(R.id.textView);
                String value = dataSnapshot.getValue(String.class);
                tv.setText(value);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value

            }
        });


    }
    //上傳firebase
    public void buttonSend(View v)
    {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String str="image/" + String.valueOf(new java.util.Date().getTime());
        StorageReference riversRef = storageRef.child(str);
        //StorageReference storageRef = firebase.storage().ref();
        //StorageReference imagesRef = storageRef.child("images");




        UploadTask uploadTask = riversRef.putFile(FileUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("firebase",exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("firebase","success");
            }
        });



        //myRef.setValue("Hello, World!");



        Toast.makeText(MainActivity.this,"已上傳",Toast.LENGTH_SHORT).show();
    }



    public void petButton(View v)
    {


        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {WRITE_EXTERNAL_STORAGE,
                            READ_EXTERNAL_STORAGE},
                    123
            );
        }
        else
        {

            readPic();
        }
    }
    private void readPic()
    {
        this.tempFile = new File(getExternalFilesDir("PHOTO"), "myphoto.jpg");
        //找尋Button按鈕


        bt1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

               Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //開啟Pictures畫面Type設定為image
                intent.setType("image/*");
                //使用Intent.ACTION_GET_CONTENT這個Action  //會開啟選取圖檔視窗讓您選取手機內圖檔
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.putExtra("crop", true);// crop=true 有這句才能叫出裁剪頁面.
                intent.putExtra("aspectX", 1);// 这兩項為裁剪框的比例.
                intent.putExtra("aspectY", 1);// x:y=1:1
                intent.putExtra("return-data", true);
                intent.putExtra("output", Uri.fromFile(tempFile));
                intent.putExtra("outputFormat", "JPEG");//返回格式


                Intent destIntent = Intent.createChooser(intent, "選擇圖片");
                startActivityForResult(destIntent, 456);





                //取得相片後返回本畫面
                //startActivityForResult(Intent.createChooser(intent,"選擇圖片"),456);
            }
        });

    }
    //取得相片後返回的監聽式
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 456) {
            //當使用者按下確定後
            if (resultCode == RESULT_OK) {
                // 設定到ImageView

                //上傳Firebase
                FileUri = data.getData();//取得圖檔的路徑位置

                 //Log.d("uri", uri.toString());//寫log
                //抽象資料的接口
                ContentResolver cr = this.getContentResolver();
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(FileUri));//由抽象資料接口轉換圖檔路徑為Bitmap
                    imageView = (ImageView) findViewById(R.id.petImage);//取得圖片控制項ImageView
                    imageView.setImageBitmap(bitmap);// 將Bitmap設定到ImageView
                } catch (FileNotFoundException e) {
                    Log.e("Exception", e.getMessage(), e);
                }






            }
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 123)
        {

            if (grantResults.length > 0
                    && grantResults[0] == PERMISSION_GRANTED) {
                //取得權限，進行檔案存取

                readPic();
            } else {
                //使用者拒絕權限，停用檔案存取功能
            }
            return;
        }
    }

}