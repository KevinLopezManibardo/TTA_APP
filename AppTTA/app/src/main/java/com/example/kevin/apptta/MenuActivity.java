package com.example.kevin.apptta;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        TextView textLogin = (TextView)findViewById(R.id.menu_login);
        textLogin.setText(intent.getStringExtra(MainActivity.EXTRA_LOGIN));
    }

    public void test(View vista) {
        Intent intent = new Intent(this,TestActivity.class);

        startActivity(intent);
    }

    public void ejercicio (View vista) {

    }

    public void seguimiento (View vista) {

    }



}
