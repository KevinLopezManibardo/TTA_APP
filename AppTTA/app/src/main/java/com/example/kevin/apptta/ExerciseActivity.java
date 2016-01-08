package com.example.kevin.apptta;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class ExerciseActivity extends AppCompatActivity {
    /* -- Atributos -- */
    private Uri pictureURI;
    final private int READ_REQUEST_CODE=0;
    final private int VIDEO_REQUEST_CODE=1;
    final private int AUDIO_REQUEST_CODE=2;
    final private int PICTURE_REQUEST_CODE=3;
    private Exercise exercise;
    private UserStatus user;

    /* -- Métodos sobreescritos -- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        Intent intent = getIntent();
        user = (UserStatus) intent.getSerializableExtra(MainActivity.EXTRA_USER);

        /*Cargar el ejercicio*/
        final PreguntasTest data = new PreguntasTest(user.getUser_dni(),user.getUser_pss());
        new Thread(new Runnable() {
            final TextView tv = (TextView) findViewById(R.id.exercise_wording);
            @Override
            public void run() {
                try{
                    exercise=data.getExercise(1);
                    tv.post(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(exercise.getWording());
                        }
                    });
                }catch(Exception e){
                    Log.e("demo", e.getMessage(), e);
                }
            }
        }).start();

        /*comprobar todos los dispositivos y eliminar los que no tiene el movil*/
        View view;
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            view = findViewById(R.id.send_pic_button);
            view.setEnabled(false);
            view = findViewById(R.id.send_video_button);
            view.setEnabled(false);
        }
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            view = findViewById(R.id.send_audio_button);
            view.setEnabled(false);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultcode, Intent data){
        if(resultcode != Activity.RESULT_OK)
            return;
        switch(requestCode){
            case READ_REQUEST_CODE:
                sendFile(data.getData());
                break;
            case VIDEO_REQUEST_CODE:
                sendFile(data.getData());
                break;
            case AUDIO_REQUEST_CODE:
                sendFile(data.getData());
                break;
            case PICTURE_REQUEST_CODE:
                sendFile(pictureURI);
                break;
        }
    }

    /* -- Métodos de clase -- */
    public void sendFile(View view){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent,READ_REQUEST_CODE);
    }

    public void sendImage(View view){
        //Se comprueba que haya cámara
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            Toast.makeText(getApplicationContext(),R.string.no_camera,Toast.LENGTH_SHORT).show();
        else{
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Se comprueba que haya aplicación para capturar imagen
            if(intent.resolveActivity(getPackageManager())!=null){
                /*Hay aplicación para capturar imagen*/
                File dir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                try {
                    File file = File.createTempFile("tta", ".jpg", dir);
                    pictureURI = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,pictureURI);
                    startActivityForResult(intent,PICTURE_REQUEST_CODE);
                }catch (IOException e){
                    Log.e("demo",e.getMessage(),e);
                }
            }else{
                /*No hay aplicación para capturar imagen*/
                Toast.makeText(getApplicationContext(),R.string.no_app,Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void sendAudio(View view){
        /*Se vuelve a comprobar aunque no debería ser necesario*/
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE))
            /*No hay micro en el dispositivo*/
            Toast.makeText(getApplicationContext(),R.string.no_micro,Toast.LENGTH_SHORT).show();
        else{
            Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            if(intent.resolveActivity(getPackageManager()) != null)
                /*hay aplicacioes para grabar aduio*/
                startActivityForResult(intent,AUDIO_REQUEST_CODE);
            else
                /*no hay aplicaciones para grabar audio*/
                Toast.makeText(getApplicationContext(),R.string.no_app,Toast.LENGTH_SHORT).show();
        }
    }

    public void sendVideo(View view){
        /*Se vuelve a comprobar aunque no debería ser necesario*/
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            /*No hay camara en el dispositivo*/
            Toast.makeText(getApplicationContext(),R.string.no_camera,Toast.LENGTH_SHORT).show();
        else{
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if(intent.resolveActivity(getPackageManager()) != null)
                /*hay aplicaciones para el video*/
                startActivityForResult(intent,VIDEO_REQUEST_CODE);
            else
                /*no hay aplicaciones para el vidoe*/
                Toast.makeText(getApplicationContext(),R.string.no_app, Toast.LENGTH_SHORT).show();
        }
    }
    private void sendFile(final Uri uri){
        final PreguntasTest data = new PreguntasTest(user.getUser_dni(),user.getUser_pss());
        File f = new File(uri.getPath());
        final String name = f.getName();
        final View view = findViewById(R.id.exercise_wording);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    data.postExercise(uri,user.getId(),user.getNextExercise(),name);
                }catch (Exception e){
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.upload_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.e("demo", e.getMessage(), e);
                }
            }
        }).start();
    }
}
