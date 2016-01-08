package com.example.kevin.apptta;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    /* -- Atributos -- */
    public final static String EXTRA_USER = "es.tta.demo.user";
    private NetworkReceiver networkReceiver;

    /* -- Métodos sobreescritos -- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Registra BroadcastReceiver para seguir los cambios en la conexión de red
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        networkReceiver = new NetworkReceiver();
        this.registerReceiver(networkReceiver, filter);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //Desregistra BroadcastReceiver cuando la app es destruida
        if(networkReceiver != null){
            this.unregisterReceiver(networkReceiver);
        }
    }

    /* -- Métodos de clase -- */
    //Método que loguea al usuario y carga la vista del Menu.
    public void login (final View view){
        final Intent intent = new Intent(this,MenuActivity.class);
        EditText editLogin = (EditText) findViewById(R.id.login);
        EditText editPasswd = (EditText) findViewById(R.id.password);
        /*Verificar que el DNI sigue el formato indicado*/
        final String pss = editPasswd.getText().toString();
        final String dni = editLogin.getText().toString();
        if(dni.matches("[0-9]{8}[A-Z]")){
            /*Obtener los datos de usuario*/
            final PreguntasTest data = new PreguntasTest(dni,pss);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UserStatus user=null;
                    try{
                        user = data.getStatus(dni,pss);
                    }catch(Exception e){
                        Log.e("demo", e.getMessage(), e);
                    }finally {
                        if(user!=null){
                            intent.putExtra(EXTRA_USER,user);
                            Log.i("demo", user.getUser_dni() + user.getUser_pss() + user.getUser());
                            view.post(new Runnable() {
                                @Override
                                public void run() {

                                    startActivity(intent);
                                }
                            });
                        }else{
                            Toast.makeText(getApplicationContext(),R.string.server_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }).start();
        }else{
            Toast.makeText(getApplicationContext(),R.string.wrong_dni, Toast.LENGTH_LONG).show();
        }

    }

}
