package org.wysaid.cgeDemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;

import org.wysaid.camera.CameraInstance;
import org.wysaid.myUtils.FileUtil;
import org.wysaid.myUtils.ImageUtil;
import org.wysaid.myUtils.MsgUtil;
import org.wysaid.nativePort.CGEFrameRecorder;
import org.wysaid.view.CameraRecordGLSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class CameraDemoActivity extends Activity implements View.OnTouchListener {

    public static String lastVideoPathFileName = FileUtil.getPath() + "/lastVideoPath.txt";

    private String mCurrentConfig;

    private CameraRecordGLSurfaceView mCameraView;
    private ImageView mThunbnailView;
    private HorizontalScrollView  mHorizontalScrollView;
    private int filterIdx = 0;
    private int filterScrollStep = 50;



    public final static String LOG_TAG = CameraRecordGLSurfaceView.LOG_TAG;

    public static CameraDemoActivity mCurrentInstance = null;
    public static CameraDemoActivity getInstance() {
        return mCurrentInstance;
    }

    private void showText(final String s) {
        mCameraView.post(new Runnable() {
            @Override
            public void run() {
                MsgUtil.toastMsg(getApplication(), s);
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }



    public static class MyButtons extends Button {

        public String filterConfig;

        public MyButtons(Context context, String config) {
            super(context);
            filterConfig = config;
        }
    }

    class RecordListener implements View.OnClickListener {

        boolean isRecording = false;
        String recordFilename;

        @Override
        public void onClick(View v) {
            Button btn = (Button)v;
            isRecording = !isRecording;
            if(isRecording)
            {
                btn.setText("正在录制");
                Log.i(LOG_TAG, "Start recording...");
                mCameraView.setClearColor(1.0f, 0.0f, 0.0f, 0.3f);
                recordFilename = ImageUtil.getPath() + "/rec_" + System.currentTimeMillis() + ".mp4";
//                recordFilename = ImageUtil.getPath(CameraDemoActivity.this, false) + "/rec_1.mp4";
                mCameraView.startRecording(recordFilename, new CameraRecordGLSurfaceView.StartRecordingCallback() {
                    @Override
                    public void startRecordingOver(boolean success) {
                        if (success) {
                            showText("启动录制成功");
                            FileUtil.saveTextContent(recordFilename, lastVideoPathFileName);
                        } else {
                            showText("启动录制失败");
                        }
                    }
                });
            }
            else
            {
                showText("录制完毕， 存储为 " + recordFilename);
                btn.setText("录制完毕");
                Log.i(LOG_TAG, "End recording...");
                mCameraView.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                mCameraView.endRecording(new CameraRecordGLSurfaceView.EndRecordingCallback() {
                    @Override
                    public void endRecordingOK() {
                        Log.i(LOG_TAG, "End recording OK");
                    }
                });
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_demo);

//        lastVideoPathFileName = FileUtil.getPathInPackage(CameraDemoActivity.this, true) + "/lastVideoPath.txt";
        Button takePicBtn = (Button) findViewById(R.id.takePicBtn);
        Button takeShotBtn = (Button) findViewById(R.id.takeShotBtn);
        Button recordBtn = (Button) findViewById(R.id.recordBtn);
        mCameraView = (CameraRecordGLSurfaceView)findViewById(R.id.myGLSurfaceView);
        mCameraView.presetCameraForward(false);

        mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.filters_scroll_view);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        mThunbnailView = (ImageView)findViewById(R.id.imagePreview);

        takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showText("Taking Picture...");

                mCameraView.takePicture(new CameraRecordGLSurfaceView.TakePictureCallback() {
                    @Override
                    public void takePictureOK(Bitmap bmp) {
                        if (bmp != null) {
                            ImageUtil.saveBitmap(bmp);
                            bmp.recycle();
                            showText("Take picture success!");
                        } else
                            showText("Take picture failed!");
                    }
                }, null, mCurrentConfig, 1.0f, true);
            }
        });

        takeShotBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showText("Taking Shot...");

                mCameraView.takeShot(new CameraRecordGLSurfaceView.TakePictureCallback() {
                    @Override
                    public void takePictureOK(Bitmap bmp) {
                        if (bmp != null) {
                            ImageUtil.saveBitmap(bmp);
                            bmp.recycle();
                            showText("Take Shot success!");
                        } else
                            showText("Take Shot failed!");
                    }
                }, false);
            }
        });

        recordBtn.setOnClickListener(new RecordListener());

        LinearLayout layout = (LinearLayout) findViewById(R.id.menuLinearLayout);

        for(int i = 0; i != MainActivity.effectConfigs.length; ++i) {
            MyButtons button = new MyButtons(this, MainActivity.effectConfigs[i]);
            button.setAllCaps(false);
            if(i == 0)
                button.setText("None");
            else
                button.setText("F" + i);

            button.setTag("F" + i);
            button.setOnClickListener(mFilterSwitchListener);
            layout.addView(button);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float intensity = progress / 100.0f;
                mCameraView.setFilterIntensity(intensity);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mCurrentInstance = this;

        Button shapeBtn = (Button)findViewById(R.id.shapeBtn);
        shapeBtn.setOnClickListener(new View.OnClickListener() {
            private boolean mIsUsingShape = false;
            Bitmap mBmp;

            @Override
            public void onClick(View v) {
                mIsUsingShape = !mIsUsingShape;
                if (mIsUsingShape) {
                    if (mBmp == null) {
                        mBmp = BitmapFactory.decodeResource(getResources(), R.drawable.mask1);
                    }
                    if (mBmp != null)
                        mCameraView.setMaskBitmap(mBmp, false, new CameraRecordGLSurfaceView.SetMaskBitmapCallback() {
                            @Override
                            public void setMaskOK(CGEFrameRecorder recorder) {
                                //flip mask
                                if(mCameraView.isUsingMask())
                                    recorder.setMaskFlipScale(1.0f, -1.0f);
                            }
                        });
                } else {
                    mCameraView.setMaskBitmap(null, false);
                }

            }
        });

        Button switchBtn = (Button)findViewById(R.id.switchCameraBtn);
        switchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCameraView.switchCamera();
            }
        });

        Button flashBtn = (Button) findViewById(R.id.flashBtn);
        flashBtn.setOnClickListener(new View.OnClickListener() {
            int flashIndex = 0;
            String[] flashModes = {
                    Camera.Parameters.FLASH_MODE_AUTO,
                    Camera.Parameters.FLASH_MODE_ON,
                    Camera.Parameters.FLASH_MODE_OFF,
                    Camera.Parameters.FLASH_MODE_TORCH,
                    Camera.Parameters.FLASH_MODE_RED_EYE,
            };

            @Override
            public void onClick(View v) {
                mCameraView.setFlashLightMode(flashModes[flashIndex]);
                ++flashIndex;
                flashIndex %= flashModes.length;
            }
        });

        mCameraView.presetRecordingSize(640, 480);
//        mCameraView.presetRecordingSize(720, 1280);
        mCameraView.setZOrderOnTop(false);
        mCameraView.setZOrderMediaOverlay(true);

        mCameraView.setOnCreateCallback(new CameraRecordGLSurfaceView.OnCreateCallback() {
            @Override
            public void createOver(boolean success) {
                if (success) {
                    Log.i(LOG_TAG, "view 创建成功");
                } else {
                    Log.e(LOG_TAG, "view 创建失败!");
                }
            }
        });

        Button pauseBtn = (Button)findViewById(R.id.pauseBtn);
        Button resumeBtn = (Button)findViewById(R.id.resumeBtn);

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.stopPreview();
            }
        });

        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.resumePreview();
            }
        });

        Button thunbnailBtn = (Button) findViewById(R.id.thunbnailBtn);

        thunbnailBtn.setOnClickListener(new View.OnClickListener() {
            boolean showThunbnailWindow = false;

            @Override
            public void onClick(View v) {
                showThunbnailWindow = !showThunbnailWindow;
                if (showThunbnailWindow) {
                    float showLeft = 0.3f, showTop = 0.35f;
                    float showRight = 0.4f + showLeft, showBottom = 0.3f + showTop;
                    mCameraView.startThunbnailCliping(150, 150, new RectF(showLeft, showTop, showRight, showBottom), new CameraRecordGLSurfaceView.TakeThunbnailCallback() {

                        public boolean isUsing = false;

                        @Override
                        public boolean isUsingBitmap() {
                            return isUsing;
                        }

                        @Override
                        public void takeThunbnailOK(Bitmap bmp) {
                            isUsing = true;
                            mThunbnailView.setImageBitmap(bmp);
                            mThunbnailView.setVisibility(View.VISIBLE);
                            isUsing = false;
                        }
                    });
                } else {
                    mCameraView.stopThunbnailCliping();
                    mThunbnailView.setVisibility(View.INVISIBLE);
                }
            }
        });

        mThunbnailView.setVisibility(View.INVISIBLE);

        Button bgBtn = (Button)findViewById(R.id.backgroundBtn);
        bgBtn.setOnClickListener(new View.OnClickListener() {
            boolean useBackground = false;
            Bitmap backgroundBmp;

            @Override
            public void onClick(View v) {
                useBackground = !useBackground;
                if (useBackground) {
                    if (backgroundBmp == null)
                        backgroundBmp = BitmapFactory.decodeResource(getResources(), R.drawable.bgview);
                    mCameraView.setBackgroundImage(backgroundBmp, false, new CameraRecordGLSurfaceView.SetBackgroundImageCallback() {
                        @Override
                        public void setBackgroundImageOK() {
                            Log.i(LOG_TAG, "Set Background Image OK!");
                        }
                    });
                } else {
                    mCameraView.setBackgroundImage(null, false, new CameraRecordGLSurfaceView.SetBackgroundImageCallback() {
                        @Override
                        public void setBackgroundImageOK() {
                            Log.i(LOG_TAG, "Cancel Background Image OK!");
                        }
                    });
                }
            }
        });

        Button fitViewBtn = (Button)findViewById(R.id.fitViewBtn);
        fitViewBtn.setOnClickListener(new View.OnClickListener() {
            boolean shouldFit = false;
            @Override
            public void onClick(View v) {
                shouldFit = !shouldFit;
                mCameraView.setFitFullView(shouldFit);
            }
        });

        final float[] mPosX = new float[1];
        final float[] mPosY = new float[1];
        final float[] mCurPosX = new float[1];
        final float[] mCurPosY = new float[1];
        mCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN: {
                        mPosX[0] = event.getX();
                        mPosY[0] = event.getY();

                        //todo code may need move
                        Log.i(LOG_TAG, String.format("Tap to focus: %g, %g", event.getX(), event.getY()));
                        final float focusX = event.getX() / mCameraView.getWidth();
                        final float focusY = event.getY() / mCameraView.getHeight();

                        mCameraView.focusAtPoint(focusX, focusY, new Camera.AutoFocusCallback() {
                            @Override
                            public void onAutoFocus(boolean success, Camera camera) {
                                if (success) {
                                    Log.e(LOG_TAG, String.format("Focus OK, pos: %g, %g", focusX, focusY));
                                } else {
                                    Log.e(LOG_TAG, String.format("Focus failed, pos: %g, %g", focusX, focusY));
                                    mCameraView.cameraInstance().setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                                }
                            }
                        });

                    }
                    break;

                    case MotionEvent.ACTION_MOVE: {
                        mCurPosX[0] = event.getX();
                        mCurPosY[0] = event.getY();

                    }
                    break;
                    case MotionEvent.ACTION_UP: {
                        Button viewOld = (Button) mHorizontalScrollView.findViewWithTag("F"+filterIdx);
                        viewOld.setTextColor(Color.WHITE);
                        if (mCurPosX[0] - mPosX[0] > 0
                                && (Math.abs(mCurPosX[0] - mPosX[0]) > 25)) {
                            if(filterIdx != MainActivity.effectConfigs.length){//filter count
                                filterIdx++;
                                mHorizontalScrollView.scrollBy(filterScrollStep, 0);
                                MyButtons btn = (MyButtons) mHorizontalScrollView.findViewWithTag("F"+filterIdx);
                                btn.setTextColor(Color.RED);

                                mCameraView.setFilterWithConfig(btn.filterConfig);
                                mCurrentConfig = btn.filterConfig;
                            }
                            System.out.println("right " + filterIdx);

                        } else if (mCurPosX[0] - mPosX[0] < 0
                                && (Math.abs(mCurPosX[0] - mPosX[0]) > 25)) {
                            if(filterIdx!=0){
                                filterIdx--;
                                mHorizontalScrollView.scrollBy(filterScrollStep, 0);
                                MyButtons btn = (MyButtons) mHorizontalScrollView.findViewWithTag("F"+filterIdx);
                                btn.setTextColor(Color.RED);

                                mCameraView.setFilterWithConfig(btn.filterConfig);
                                mCurrentConfig = btn.filterConfig;
                            }
                            System.out.println("left" + filterIdx);

                        }

                    }
                    break;

                    default:
                        break;
                }

                return true;
            }
        });

        mCameraView.setPictureSize(600, 800, true);
    }

    private View.OnClickListener mFilterSwitchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MyButtons btn = (MyButtons)v;
            mCameraView.setFilterWithConfig(btn.filterConfig);
            mCurrentConfig = btn.filterConfig;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera_demo, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        CameraInstance.getInstance().stopCamera();
        Log.i(LOG_TAG, "activity onPause...");
        mCameraView.release(null);
        mCameraView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        mCameraView.onResume();
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
