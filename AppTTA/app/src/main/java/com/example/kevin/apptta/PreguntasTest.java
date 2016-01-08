package com.example.kevin.apptta;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by kevin on 4/01/16.
 */
public class PreguntasTest {
    /* -- Atributos -- */
    RestClient restClient;

    /* -- Métodos de clase -- */
    //Constructor. Crea una conexión con el servidor REST
    public PreguntasTest(String user, String passwd) {
        restClient = new RestClient("http://u017633.ehu.eus:18080/AlumnoTta/rest/tta");
        restClient.setHttpBasicAuth(user,passwd);
    }

    //Método para obtener la pregunta de test y sus respuestas
    public Test getTest(int id) throws IOException, JSONException{
        //Se reciben los datos en formato JSON
        JSONObject json = restClient.getJson(String.format("getTest?id=%d", id));
        //Se extrae la pregunta
        String test_wording = json.getString("wording");
        //Se extraen las respuestas
        JSONArray jsonArray = json.getJSONArray("choices");
        //Variables en las que se van a almacenar los datos
        int length  = jsonArray.length();
        String [] choicesWording = new String[length];
        boolean [] choicesCorrect = new boolean[length];
        String [] choicesAdvise = new String[length];
        String [] choicesAdviseType = new String[length];
        int [] choicesId = new int[length];
        //Se extrae la informacion del JSON
        for(int i = 0;i<length;i++){
            JSONObject jsonChoice = jsonArray.getJSONObject(i);
            choicesId[i] = jsonChoice.getInt("id");
            choicesWording[i] = jsonChoice.getString("answer");
            choicesAdvise[i] = jsonChoice.getString("advise");
            choicesCorrect[i] = jsonChoice.getBoolean("correct");
            if(jsonChoice.isNull("resourceType")){
                choicesAdviseType[i] = null;
            }else{
                choicesAdviseType[i] = jsonChoice.getJSONObject("resourceType").getString("mime");;
            }
        }
        //Se devuelven los resultados en un objeto de clase Test
        Test test = new Test(test_wording,choicesId,choicesWording,choicesCorrect,choicesAdvise,choicesAdviseType);
        return test;
    }

    public Exercise getExercise(int id)throws IOException, JSONException{
        JSONObject json = restClient.getJson(String.format("getExercise?id=%d",id));
        Exercise exercise = new Exercise(json.getInt("id"),json.getString("wording"));
        return exercise;
    }

    public UserStatus getStatus(String dni,String pss) throws IOException, JSONException{
        JSONObject json = restClient.getJson(String.format("getStatus?dni=%s",dni));
        UserStatus user = new UserStatus(json.getInt("id"),json.getString("user"),json.getInt("lessonNumber"),
                json.getString("lessonTitle"),json.getInt("nextTest"),json.getInt("nextExercise"),dni,pss);
        return user;
    }

    public int postTest(int user, int choice)throws IOException, JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId",user);
        jsonObject.put("choiceId",choice);
        return restClient.postJson(jsonObject,"postChoice");
    }

    public int postExercise(Uri uri, int user, int exercise,String name)throws IOException{
        InputStream is = new FileInputStream(uri.getPath());
        String path = "postExercise?user="+user+"&id="+exercise;
        return restClient.postFile(path,is,name);
    }
}
