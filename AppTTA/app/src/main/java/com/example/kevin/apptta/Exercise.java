package com.example.kevin.apptta;

/**
 * Created by kevin on 6/01/16.
 */
public class Exercise {
    /* -- Atributos -- */
    private int id;
    private String wording;

    /* -- Métodos de clase -- */
    //Constructor
    public Exercise(int id, String wording) {
        this.id = id;
        this.wording = wording;
    }

    public int getId() {
        return id;
    }

    public String getWording() {
        return wording;
    }
}