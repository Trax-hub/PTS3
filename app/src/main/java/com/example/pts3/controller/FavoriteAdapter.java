package com.example.pts3.controller;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pts3.R;
import com.example.pts3.model.Track;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class FavoriteAdapter extends ArrayAdapter<Track>{

        private final MediaPlayer mediaPlayer;
        private final FavoriteAdapter adapter;
        private final ArrayList<Track> trackArrayList;
        private View oldView;
        private boolean mediaPlayerOnBreak=false;

        public FavoriteAdapter(Context context, ArrayList<Track> trackArrayList, MediaPlayer mediaPlayer){
            super(context, R.layout.favorite_item,R.id.trackName, trackArrayList);
            this.adapter = this;
            this.trackArrayList = trackArrayList;
            this.mediaPlayer = mediaPlayer;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = super.getView(position, convertView, parent);

            Track tracks = getItem(position);

            if (view == null){
                view = LayoutInflater.from(getContext()).inflate(R.layout.favorite_item, parent, false);
            }

            ImageView cover = view.findViewById(R.id.cover);
            TextView track = view.findViewById(R.id.trackName);
            TextView artist = view.findViewById(R.id.artist);
            ImageButton delete = view.findViewById(R.id.deleteFavorite);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    CollectionReference collecRef = FirebaseFirestore.getInstance()
                            .collection("/Favorite")
                            .document("/" + Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                            .collection("/Tracks");


                    collecRef.whereEqualTo("artiste", tracks.getArtistName())
                            .whereEqualTo("title", tracks.getTitle())
                            .whereEqualTo("cover", tracks.getCover())
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful()){
                                        for(DocumentSnapshot documentSnapshot : task.getResult()){
                                            collecRef.document(documentSnapshot.getId()).delete();
                                        }
                                        trackArrayList.remove(tracks);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                }
            });

            cover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (tracks.getPreview() != null) {

                        if (mediaPlayer.isPlaying() && view.equals(oldView)) {
                            mediaPlayer.pause();
                            mediaPlayerOnBreak = true;
                        } else if (mediaPlayerOnBreak && view.equals(oldView)) {
                            mediaPlayer.start();
                            mediaPlayerOnBreak = false;
                        } else {
                            mediaPlayer.reset();
                            try {
                                mediaPlayer.setDataSource(tracks.getPreview());
                                mediaPlayer.setVolume(0.5f, 0.5f);
                                mediaPlayer.prepare();
                                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mediaPlayer) {
                                        mediaPlayer.setLooping(false);
                                        mediaPlayer.start();
                                        oldView = view;
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });

            if(tracks.getCover().isEmpty()){
                tracks.setCover("https://drive.google.com/drive/u/0/folders/1KrHiDVmSoHVJDUHZ3l6Uvk949CeHYfp2");
                tracks.setCoverMax("https://drive.google.com/drive/u/0/folders/1KrHiDVmSoHVJDUHZ3l6Uvk949CeHYfp2");
            }

            Picasso.get().load(tracks.getCover()).fit().into(cover);
            track.setText(tracks.getTitle());
            artist.setText(tracks.getArtistName());

            return view;
        }
}

