package com.example.kevin.apptta;

import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.widget.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.util.Log;

import java.io.IOException;
//(DONE)
public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    /* -- Atributos -- */
    private View.OnClickListener listener;
    Test test;
    private LinearLayout layout;
    private int correcto;
    String advise;
    String adviseType;
    private UserStatus user;

    /* -- Métodos sobreescritos -- */
    @Override //Sobreescribimos el método onClick para poder implementar OnClickListener
    public void onClick(View u){
        findViewById(R.id.button_send_test).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Intent intent = getIntent();
        user = (UserStatus) intent.getSerializableExtra(MainActivity.EXTRA_USER);

        listener = this;

        //Referenciamos los elementos en los que se mostraran la pregunta y las respuestas
        final TextView textWording = (TextView) findViewById(R.id.test_wording);
        final RadioGroup group = (RadioGroup) findViewById(R.id.test_choices);


        //Creamos un hilo para cargar en segundo plano las preguntas y respuestas del test y mostrarlas
        new Thread(new Runnable() {
            //Sobreescribimos el método run() de Thread
            @Override
            public void run() {
                PreguntasTest data = new PreguntasTest(user.getUser_dni(),user.getUser_pss());
                try {
                    //Carga de las preguntas y respuestas
                    test = data.getTest(1);

                    //Se muestra la pregunta
                    textWording.post(new Runnable() {
                        @Override
                        public void run() {
                            textWording.setText(test.getWording());
                        }
                    });

                    //Se añade programaticamente un RadioButton para cada posible respuesta
                    int i = 0;
                    for (Test.Choice choice : test.getChoices()) {
                        final RadioButton radio = new RadioButton(getApplicationContext());
                        radio.setText(choice.getWording());
                        radio.setOnClickListener(listener);
                        radio.setTextColor(Color.BLACK);
                        group.post(new Runnable() {
                            @Override
                            public void run() {
                                group.addView(radio);
                            }
                        });
                        //Se almacena cual de las respuestas es la correcta
                        if (choice.isCorrect()) {
                            correcto = i;
                        }
                        i++;
                    }
                } catch (Exception e) {
                    Log.e("demo", e.getMessage(), e);
                }
            }
        }).start();

        //Cargamos la vista completa para poder hacer cambios sobre ella
        layout = (LinearLayout) findViewById(R.id.test_layout);
    }

    /* -- Métodos de clase -- */
    //Metodo que implementa la funcion del boton Enviar
    public void send (final View view){
        //Se obtiene el ID de la respuesta seleccionada y con ello la posición del boton seleccionado
        RadioGroup group = (RadioGroup) findViewById(R.id.test_choices);
        int selectedID = group.getCheckedRadioButtonId();
        View radioButton = group.findViewById(selectedID);
        final int selected = group.indexOfChild(radioButton);

        //Se desactivan los botones
        int choices = group.getChildCount();
        for (int i=0; i < choices; i++){
            group.getChildAt(i).setEnabled(false);
        }

        //Se elimina el boton de Enviar
        View button = findViewById(R.id.button_send_test);
        ((ViewGroup) button.getParent()).removeView(button);

        //Se resalta en verde la respuesta correcta
        group.getChildAt(correcto).setBackgroundColor(Color.GREEN);

        if(selected != correcto){
            //Si la respuesta seleccionada es incorrecta, se resalta en rojo
            group.getChildAt(selected).setBackgroundColor(Color.RED);
            Toast.makeText(getApplicationContext(), "Que mala suerte...", Toast.LENGTH_SHORT).show();
            //Se muestra la ayuda
            advise = test.getChoice(selected).getAdvise();
            adviseType = test.getChoice(selected).getAdviseType();
            if(advise != null && !advise.isEmpty()){
                findViewById(R.id.button_view_advice).setVisibility(View.VISIBLE);
            }
        } else
            Toast.makeText(getApplicationContext(),"¡Enhorabuena!",Toast.LENGTH_SHORT).show();
        /*Envio al servidor*/
        final PreguntasTest data = new PreguntasTest(user.getUser_dni(),user.getUser_pss());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    data.postTest(user.getId(),selected);
                }catch(Exception e){
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

    //Metodo para escoger el tipo de ayuda
    public void help(View view) throws IOException {
        view.setEnabled(false);
        switch(adviseType){
            case Test.ADVISE_AUDIO:
                showAudio();
                break;
            case Test.ADVISE_HTML:
                showHtml();
                break;
            case Test.ADVISE_VIDEO:
                showVideo();
                break;
        }
    }

    //Metodo para mostrar ayuda en forma de audio
    private void showAudio() throws IOException {
        View view = new View(this);
        AudioPlayer audio = new AudioPlayer(view);
        //Se indica la Uri del audio
        audio.setAudioUri(Uri.parse(advise));
        //Se añade programáticamente el reproductor de audio
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        layout.addView(view);
        //Se reproduce el audio
        audio.start();
    }

    //Metodo para mostrar ayuda en formato HTML
    private void showHtml(){
        //Se comprueba si se va a mostrar a traves de un navegador web...
        if (advise.substring(0, 10).contains("://")) {
            Uri uri = Uri.parse(advise);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        //...o si se va a añadir a la vista
        else {
            WebView web = new WebView(this);
            web.loadData(advise, "text/html", null);
            web.setBackgroundColor(Color.TRANSPARENT);
            web.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            layout.addView(web);
        }
    }

    //Metodo para mostrar ayuda en forma de video
    private void showVideo(){
        VideoView video = new VideoView(this);
        //Se indica la Uri del video
        video.setVideoURI(Uri.parse(advise));
        //Se añade programáticamente el reproductor de video
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        video.setLayoutParams(params);
        //Se añade programáticamente un panel de control para el reproductor de video
        MediaController controller = new MediaController(this){
            @Override
            public void hide(){}

            @Override
            public boolean dispatchKeyEvent(KeyEvent event){
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    finish();
                return super.dispatchKeyEvent(event);
            }
        };
        controller.setAnchorView(video);
        video.setMediaController(controller);
        layout.addView(video);
        //Se reproduce el video
        video.start();
    }


}
