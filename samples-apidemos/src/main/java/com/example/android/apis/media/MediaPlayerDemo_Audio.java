/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.media;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.apis.R;

import java.io.IOException;

public class MediaPlayerDemo_Audio extends Activity {

    private static final String TAG = "MediaPlayerDemo";
    private MediaPlayer mMediaPlayer;
    private static final String MEDIA = "media";
    private static final int LOCAL_AUDIO = 1;
    private static final int STREAM_AUDIO = 2;
    private static final int RESOURCES_AUDIO = 3;
    private static final int LOCAL_VIDEO = 4;
    private static final int STREAM_VIDEO = 5;
    private String path;

    private TextView tx;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        tx = new TextView(this);
        setContentView(tx);
        Bundle extras = getIntent().getExtras();
        playAudio(extras.getInt(MEDIA));
    }

    private void playAudio(Integer media) {
        try {
            if (media == LOCAL_AUDIO) {
                path = "/sdcard/Music/test.mp3"; // Example path, user should replace with actual file
                if (path.isEmpty()) {
                    Toast.makeText(this, 
                        "Please set a valid audio file path", Toast.LENGTH_LONG).show();
                    return;
                }
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnPreparedListener(mp -> mp.start());
                mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                    Toast.makeText(this, "Playback failed", Toast.LENGTH_SHORT).show();
                    return true;
                });
            } else if (media == RESOURCES_AUDIO) {
                mMediaPlayer = MediaPlayer.create(this, R.raw.test_cbr);
                if (mMediaPlayer != null) {
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.start();
                } else {
                    Toast.makeText(this, "Audio resource not found", Toast.LENGTH_SHORT).show();
                }
            } else if (media == STREAM_AUDIO) {
                String url = "http://example.com/audio.mp3"; // Example URL
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(url);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnPreparedListener(mp -> mp.start());
            }
            
            tx.setText("Playing audio...");
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
            Toast.makeText(this, "File not found or invalid format", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException: " + e.getMessage());
            Toast.makeText(this, "Playback error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO Auto-generated method stub
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }
}
