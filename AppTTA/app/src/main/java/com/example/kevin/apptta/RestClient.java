package com.example.kevin.apptta;

import android.util.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 4/01/16.
 */
public class RestClient {
    /* -- Atributos -- */
    private final static String AUTH = "Authorization";
    private final String baseURL;
    private final Map<String,String> properties = new HashMap<>();
    public RestClient (String baseURL){ this.baseURL = baseURL; }

    /* -- Métodos de clase -- */
    //Metodo para autenticarse frente al servidor
    public void setHttpBasicAuth(String user,String passwd){
        String sumUP = user+":"+passwd;
        String basicAuth = Base64.encodeToString(sumUP.getBytes(),Base64.DEFAULT);
        setAuthorization(String.format("Basic %s", basicAuth));
    }

    //Método para establecer la conexión con el servidor
    private HttpURLConnection getConnection (String path) throws IOException{
        URL url  = new URL(String.format("%s/%s",baseURL,path));
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        for(Map.Entry<String,String> property : properties.entrySet())
            conn.setRequestProperty(property.getKey(),property.getValue());
        return conn;
    }

    //Metodo para subir un archivo al servidor
    public int postFile (String path,InputStream is, String fileName)throws IOException {
        String boundary = Long.toString(System.currentTimeMillis());
        String newLine = "\r\n";
        String prefix = "--";
        HttpURLConnection conn = null;
        try{
            conn = getConnection(path);
            conn.setRequestMethod("POST");
            /*Establecemos la cabecera*/
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            /*Establecemos el prefijo*/
            out.writeBytes(prefix+boundary+newLine);
            /*Establecemos las cabeceras para el fichero*/
            out.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""+fileName+"\""+newLine);
            out.writeBytes(newLine);

            /*Escritura del fichero*/
            byte[] data = new byte[1024*1024];
            int len;
            while((len = is.read(data)) > 0)
                out.write(data,0,len);
            /*Cierre del fichero*/
            out.writeBytes(newLine);
            /*Cierre del mensaje*/
            out.writeBytes(prefix + boundary + prefix + newLine);
            out.close();

            return conn.getResponseCode();
        }finally {
            if(conn != null)
                conn.disconnect();
        }
    }

    //Método para ¿?
    public int postJson (final JSONObject json, String path) throws IOException {
        HttpURLConnection conn = null;
        try{
            conn = getConnection(path);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            try(PrintWriter pw = new PrintWriter(conn.getOutputStream())){
                pw.print(json.toString());
                return conn.getResponseCode();
            }
        } finally {
            if(conn != null)
                conn.disconnect();
        }
    }

    //Getters y Setters
    public String getAuthorization(){
        return properties.get(AUTH);
    }
    public void setAuthorization (String auth) {
        properties.put(AUTH, auth);
    }
    public void setProperty (String name, String value){
        properties.put(name,value);
    }
    public JSONObject getJson(String path) throws IOException, JSONException{
        return new JSONObject(getString(path));
    }
    public String getString(String path) throws IOException {
        HttpURLConnection conn = null;
        try{
            conn = getConnection(path);
            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
                return br.readLine();
            }
        }finally {
            if(conn != null)
                conn.disconnect();
        }
    }
}
