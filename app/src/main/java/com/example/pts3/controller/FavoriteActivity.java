package com.example.pts3.controller;

import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pts3.R;
import com.example.pts3.model.Track;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Objects;

public class FavoriteActivity extends AppCompatActivity {

    private ListView listView;
    private FirebaseFirestore db;
    private ArrayList<Track> tracks;
    private TextView vide;
    private InternetCheckService internetCheckService;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        mediaPlayer = new MediaPlayer();
        vide = (TextView) findViewById(R.id.vide);
        internetCheckService = new InternetCheckService();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetCheckService,intentFilter);
        db = FirebaseFirestore.getInstance();
        tracks = new ArrayList<>();
        listView = (ListView) findViewById(R.id.favList);
        getData();
    }

    private void getData(){
        db.collection("/Favorite")
                .document("/" + Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .collection("/Tracks")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String title = Objects.requireNonNull(document.get("title")).toString();
                                String preview = Objects.requireNonNull(document.get("preview")).toString();
                                String artistName = Objects.requireNonNull(document.get("artiste")).toString();
                                String cover = Objects.requireNonNull(document.get("cover")).toString();
                                String coverMax = Objects.requireNonNull(document.get("coverMax")).toString();

                                tracks.add(new Track(title, preview, artistName, cover, coverMax));
                            }
                            display(tracks);
                            if(tracks.size() == 0){
                                vide.setVisibility(View.VISIBLE);
                            }else{
                                vide.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                });
    }

    private void display(ArrayList<Track> tracks){
        FavoriteAdapter favoriteAdapter = new FavoriteAdapter(this, tracks, mediaPlayer);
        listView.setAdapter(favoriteAdapter);
    }

    @Override
    protected void onStart() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(internetCheckService,intentFilter);
        display(tracks);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(internetCheckService);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.reset();
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.reset();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.reset();
    }
}
