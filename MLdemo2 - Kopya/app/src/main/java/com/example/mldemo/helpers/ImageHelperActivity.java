package com.example.mldemo.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
// first we define image labeler or image classification object
// then we pass the bitmap to this image labeler  to get classifications

import com.example.mldemo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.mlkit.vision.common.InputImage;

import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.util.Calendar;


public class ImageHelperActivity extends AppCompatActivity {

    private ImageView inputImageView;



    private TextView outputTextView;

    private  int REQUEST_PICK_IMAGE=1000;

   // private ImageLabeler imageLabeler;

    private Segmenter segmenter;
    private static int counter =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_helper);

        inputImageView = findViewById(R.id.imageViewInput);
        outputTextView = findViewById(R.id.textViewOutput);

        //image labeler defined, ready to except bitmaps
        //imageLabeler = ImageLabeling.getClient(new ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build());

        SelfieSegmenterOptions options =
                new SelfieSegmenterOptions.Builder()
                        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)

                        .build();

         segmenter = Segmentation.getClient(options);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //we need to add this condition so that we wont be asking again
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
                //denied -1, granted 0

            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    public void onPıckImage(View view){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,REQUEST_PICK_IMAGE);
    }
    public void onStartCamera(View  view){

    }
//to process image we use on activivy result and loadfromURI, we are getting bitmap here, we are creating bitmap from URI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //as soon as we get the infromation that image has been choosen we go into those if blocks
        if (resultCode ==RESULT_OK){
            if (requestCode ==REQUEST_PICK_IMAGE){

                 Uri uri = data.getData();

                 Bitmap bitmap = loadFromUri(uri);
                runClassification(bitmap);
                 //inputImageView.setImageBitmap(bitmap);



            }
        }
    }

    private Bitmap loadFromUri(Uri uri){
        Bitmap bitmap = null;

        //user may put corrupted files check that if u want
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),uri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }else {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        return bitmap;
    }

    private void runClassification(Bitmap bitmap)  {
        InputImage inputImage = InputImage.fromBitmap(bitmap,0);
        //process has 2 listener succes and failure
        Task<SegmentationMask> result =
                segmenter.process(inputImage);
        //
                      result  .addOnSuccessListener(
                                new OnSuccessListener<SegmentationMask>() {
                                    @Override
                                    public void onSuccess(SegmentationMask segmentationMask) {

                                        ByteBuffer mask = segmentationMask.getBuffer();
                                        int maskWidth = segmentationMask.getWidth();
                                        int maskHeight = segmentationMask.getHeight();
                                        Bitmap bitmapResult = bitmap.copy(Bitmap.Config.ARGB_8888,true);
                                        for (int y = 0; y < maskHeight; y++) {
                                            for (int x = 0; x < maskWidth; x++) {
                                                // Gets the confidence of the (x,y) pixel in the mask being in the foreground.
                                                float foregroundConfidence = mask.getFloat();
                                                if (foregroundConfidence<0.85f){
                                                    bitmapResult.setPixel(x,y,Color.alpha(Color.TRANSPARENT));


                                                }
                                            }
                                        }


                                      //  bitmapResult.copyPixelsFromBuffer(mask);


                                       // BitmapDrawable bitmapDrawable = new BitmapDrawable()




                                        inputImageView.setImageBitmap(bitmapResult);
/*                                        try{
                                            File file = new File(Environment.getExternalStorageDirectory() + File.separator + "Output.png");
                                            file.createNewFile();
                                          FileOutputStream fout =   new FileOutputStream(file);

                                            bitmapResult.compress(Bitmap.CompressFormat.PNG, 100,fout); // YOU can also save it in JPEG
                                            byte[] bitmapdata = bitmapResult.getNinePatchChunk();

                                            //Bufferla önce bi yerde biriktirip ondan sonra toplu aktarmak daha etkili olacak.hardiske
                                                fout.write(bitmapdata);
                                                fout.close();


                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }*/
                                        // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                                        //bitmapResult.setHasAlpha(true);
                                       //String imageFileName =  "output";
                                     // String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmapResult, imageFileName, null);
                                        //Uri uriimage = Uri.parse(path);

                                       counter++;
                                       // Calendar cal = Calendar.getInstance();


                                    saveBitmap("Output",bitmapResult);









                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        System.out.println("SOMETHING DID NOT WORK OUT FAILURE"+e.toString());
                                    }
                                });




    }


    public static void saveBitmap(String imgName,
                                  Bitmap mBitmap) {//  ww  w.j  a va 2s.c  o  m

        File f = new File(Environment.getExternalStorageDirectory()
                .toString() + "/" + imgName +".png");

        while (f.exists()){

            f = new File(Environment.getExternalStorageDirectory()
                    .toString() + "/" + imgName+ counter+ ".png");
                    counter++;
        }


        try {
            f.createNewFile();

        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 10, fOut);

        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}