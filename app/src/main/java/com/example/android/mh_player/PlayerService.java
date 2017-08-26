package com.example.android.mh_player;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener{

    //media player
    private MediaPlayer player;
    private String songTitle="Song Title";
    private static final int NOTIFY_ID=1;
    private final IBinder musicBind = new MusicBinder();
    private String mp3URL;
    private int file_duration;

    private String mediaPlayerState = null;
    private boolean isPlaying = false;

    //Constructor
    public PlayerService() {
    }

    //Create and Configure the MediaPlayer.
    public void onCreate(){
        super.onCreate();

        //create player, configure and register listeners.
        player = new MediaPlayer();
        mediaPlayerState = "IDLE";
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    //Returns a Binder object to the calling activity. The object
    //contains a reference to this service.
    public class MusicBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    //LIFE CYCLE METHODS

    //Called by the system. We move the service to the background, easier to kill.
    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    //the calling activity gets an IBinder object.
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    //Stop and Release the MediaPlayer.
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    //MediaPlayer Methods
    /////////////////////////////

    //When ready to play, we create a notification.
    @Override
    public void onPrepared(MediaPlayer mp) {
        file_duration = mp.getAudioSessionId();
        //start playback
        mp.start();
        mediaPlayerState = "PLAYING";

        //Creating the notification
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }

    //OnError, bring the player to Idle state.
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    //When track has finished, call playNext().
    @Override
    public void onCompletion(MediaPlayer mp) { //called when track is done
        isPlaying = false;

        if(player.getCurrentPosition()>0){
            mp.reset();
            //playNext(url);
        }
    }

    //AUDIO MANAGER METHODS

    @Override
    public void onAudioFocusChange(int i) {

    }

    //METHODS CALLED FROM THE PlayerScreen

    public void setURL(String song_url){
        mp3URL = song_url;
    }

    public void playBtnPressed(){

        if (mediaPlayerState.equals("IDLE")){
            try {
                player.setDataSource(mp3URL);
            }
            catch(Exception e){
                Log.e("MUSIC SERVICE", "Error setting data source", e);
            }
            player.prepareAsync();
        } else if (mediaPlayerState.equals("PAUSED")){
            player.start();
            mediaPlayerState = "PLAYING";
        } else if (mediaPlayerState.equals("PLAYING")){
            player.pause();
            mediaPlayerState = "PAUSED";
        } else if (mediaPlayerState.equals("STOPPED")){
            player.prepareAsync();
        }
    }

    public void stopBtnPressed(){
        if (mediaPlayerState.equals("IDLE")){
            //Do nothing
        } else {
            player.stop();
            mediaPlayerState = "STOPPED";
        }
    }

    public void fwdBtnPressed(){
        if (mediaPlayerState.equals("PLAYING")){
            int currentTime = player.getCurrentPosition(); //in MS.
            player.seekTo(currentTime + 30000);
        }
    }

    public void replayBtnPressed(){
        if (mediaPlayerState.equals("PLAYING")){
            int currentTime = player.getCurrentPosition(); //in MS.
            player.seekTo(currentTime - 30000);
        }
    }

    public boolean isPlaying(){
        if (!mediaPlayerState.equals("IDLE")){
            return true;
        } else {
            return false;
        }
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return file_duration;
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seekTo(int posn){
        if (mediaPlayerState.equals("PLAYING")){
            player.seekTo(posn);
        }

    }

    public void playPrev(String song_url){
        playSong(song_url);
    }

    //skip to next
    public void playNext(String song_url){
        playSong(song_url);
    }

    public void playSong(String song_url){
        //play a song
        player.reset();

        try{
            player.setDataSource(song_url);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }
}
