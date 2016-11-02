package org.wysaid.cgeDemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import org.wysaid.common.Common;
import org.wysaid.myUtils.FileUtil;
import org.wysaid.myUtils.ImageUtil;
import org.wysaid.myUtils.MsgUtil;
import org.wysaid.nativePort.CGEFrameRenderer;
import org.wysaid.texUtils.TextureRenderer;
import org.wysaid.texUtils.TextureRendererDrawOrigin;
import org.wysaid.texUtils.TextureRendererEdge;
import org.wysaid.texUtils.TextureRendererEmboss;
import org.wysaid.texUtils.TextureRendererLerpBlur;
import org.wysaid.texUtils.TextureRendererWave;
import org.wysaid.view.VideoPlayerGLSurfaceView;

public class VideoEditorActivity extends AppCompatActivity {

    VideoPlayerGLSurfaceView mPlayerView;
    Button mEditingBtn;
    Button mFiltersBtn;
    Button mAnination;
    Button mSubtitling;


    String mCurrentConfig;

    public static final int REQUEST_CODE_PICK_VIDEO = 1;

    private VideoPlayerGLSurfaceView.PlayCompletionCallback playCompletionCallback = new VideoPlayerGLSurfaceView.PlayCompletionCallback() {
        @Override
        public void playComplete(MediaPlayer player) {
            Log.i(Common.LOG_TAG, "The video playing is over, restart...");
            player.start();
        }

        @Override
        public boolean playFailed(MediaPlayer player, final int what, final int extra)
        {
            MsgUtil.toastMsg(VideoEditorActivity.this, String.format("Error occured! Stop playing, Err code: %d, %d", what, extra));
            return true;
        }
    };

    class MyVideoButton extends Button implements View.OnClickListener {

        Uri videoUri;
        VideoPlayerGLSurfaceView videoView;

        public MyVideoButton(Context context) {
            super(context);
        }

        @Override
        public void onClick(View v) {

            MsgUtil.toastMsg(VideoEditorActivity.this, "正在准备播放视频 " + videoUri.getHost() + videoUri.getPath() + " 如果是网络视频， 可能需要一段时间的等待");

            videoView.setVideoUri(videoUri, new VideoPlayerGLSurfaceView.PlayPreparedCallback() {
                @Override
                public void playPrepared(MediaPlayer player) {
                    mPlayerView.post(new Runnable() {
                        @Override
                        public void run() {
                            MsgUtil.toastMsg(VideoEditorActivity.this, "开始播放 " + videoUri.getHost() + videoUri.getPath());
                        }
                    });

                    player.start();
                }
            }, playCompletionCallback);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_editor);
        mPlayerView = (VideoPlayerGLSurfaceView)findViewById(R.id.videoGLSurfaceView);
        mPlayerView.setZOrderOnTop(false);
        mPlayerView.setZOrderMediaOverlay(true);

        mEditingBtn = (Button)findViewById(R.id.editing);
        mAnination = (Button)findViewById(R.id.anination);
        mSubtitling = (Button)findViewById(R.id.subtitling);
        mFiltersBtn = (Button)findViewById(R.id.filters);
        mFiltersBtn.setOnClickListener(new View.OnClickListener() {
            private int filterIndex;
            @Override
            public void onClick(View v) {
                mPlayerView.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        ++filterIndex;
                        mPlayerView.setFilterWithConfig(MainActivity.effectConfigs[filterIndex %= 5]);
                    }
                });
            }
        });


        {
            String lastVideoFileName = FileUtil.getTextContent(CameraDemoActivity.lastVideoPathFileName);
            if(lastVideoFileName == null) {
                Log.i(Common.LOG_TAG, lastVideoFileName);

                MsgUtil.toastMsg(VideoEditorActivity.this, "No video is recorded, please record one in the 2nd case.");
                return;
            }

            Uri lastVideoUri = Uri.parse(lastVideoFileName);
            mPlayerView.setVideoUri(lastVideoUri, new VideoPlayerGLSurfaceView.PlayPreparedCallback() {
                @Override
                public void playPrepared(MediaPlayer player) {
                    Log.i(Common.LOG_TAG, "The video is prepared to play");
                    player.start();
                }
            }, playCompletionCallback);
        }


        mPlayerView.setPlayerInitializeCallback(new VideoPlayerGLSurfaceView.PlayerInitializeCallback() {
            @Override
            public void initPlayer(final MediaPlayer player) {
                //针对网络视频进行进度检查
                player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {
                        Log.i(Common.LOG_TAG, "Buffer update: " + percent);
                        if(percent == 100) {
                            Log.i(Common.LOG_TAG, "缓冲完毕!");
                            player.setOnBufferingUpdateListener(null);
                        }
                    }
                });
            }
        });

    }



    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode)
        {
            case REQUEST_CODE_PICK_VIDEO:
                if(resultCode == RESULT_OK)
                {
                    mPlayerView.setVideoUri(data.getData(), new VideoPlayerGLSurfaceView.PlayPreparedCallback() {
                        @Override
                        public void playPrepared(MediaPlayer player) {
                            Log.i(Common.LOG_TAG, "The video is prepared to play");
                            player.start();
                        }
                    }, playCompletionCallback);
                }
            default: break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(VideoPlayerGLSurfaceView.LOG_TAG, "activity onPause...");
        mPlayerView.release();
        mPlayerView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mPlayerView.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
