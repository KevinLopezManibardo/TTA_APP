package com.example.kevin.apptta;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {
    /* -- Atributos -- */
    private UserStatus user;
    public final static String EXTRA_EXERCISE = "es.tta.apptta.exercise";

    /* -- Métodos sobreescritos -- */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        //Recoge el nombre de usuario y lo muestra en la vista del Menu
        Intent intent = getIntent();
        TextView textLogin = (TextView)findViewById(R.id.menu_login);
        user = (UserStatus) intent.getSerializableExtra(MainActivity.EXTRA_USER);
        textLogin.setText("Bienvenido "+user.getUser());
        TextView textLection = (TextView) findViewById(R.id.menu_lection);
        textLection.setText("Lección "+user.getLesson());
    }

    /* -- Métodos de clase -- */
    //Método que carga la vista del Test.
    public void test(View vista) {
        Intent intent = new Intent(this,TestActivity.class);
        intent.putExtra(MainActivity.EXTRA_USER,user);
        startActivity(intent);
    }

    //Método que carga la vista del Ejercicio.
    public void ejercicio (View vista) {
        Intent intent = new Intent(this,ExerciseActivity.class);
        intent.putExtra(MainActivity.EXTRA_USER,user);
        startActivity(intent);
    }

    //Método que carga la vista del Seguimiento.
    public void seguimiento (View vista) {

    }

}
