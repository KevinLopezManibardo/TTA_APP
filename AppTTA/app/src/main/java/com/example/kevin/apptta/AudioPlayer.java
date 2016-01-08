package com.example.kevin.apptta;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;

import java.io.IOException;

/**
 * Created by kevin on 4/01/16.
 */
public class AudioPlayer implements MediaController.MediaPlayerControl, MediaPlayer.OnPreparedListener {
    /* -- Atributos -- */
    private View view;
    private MediaPlayer player;
    private MediaController controller;

    /* -- Métodos de clase -- */
    //Constructor. Crea un objeto de la clase mediaController
    public AudioPlayer(View view){
        this.view = view;
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        controller = new MediaController(view.getContext()){
            @Override
            public boolean dispatchKeyEvent(KeyEvent event){
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                    release();
                return super.dispatchKeyEvent(event);
            }
        };
    }

    //Metodo para establecer la Uri del audio y reproducirlo
    public void setAudioUri(Uri uri) throws IOException {
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDataSource(view.getContext(), uri);
        player.prepare();
        player.start();
    }

    //Metodo para liberar los recursos de audio
    public void release(){
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
    }

    /* -- Métodos sobreescritos -- */
    @Override
    public void onPrepared(MediaPlayer np){
        controller.setMediaPlayer(this);
        controller.setAnchorView(view);
        controller.show(0);
    }

    @Override
    public void start(){
        player.start();
    }

    @Override
    public void pause(){
        player.pause();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        player.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return (player.getCurrentPosition()*100)/player.getDuration();
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return player.getAudioSessionId();
    }
}
