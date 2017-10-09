package jp.techacademy.hirokazu.tomoda.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;
    ContentResolver resolver;
    Cursor cursor;
    ImageView imageVIew;
    Timer mTimer;
    Handler mHandler = new Handler();
    Button nextButton;
    Button backButton;
    Button playButton;
    boolean playFlg;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //進むボタン
        nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextCursor();
                showImage();
                }
        });

        // 戻るボタン
        backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevCursor();
                showImage();
            }
        });

        //再生・停止ボタン
        playButton = (Button) findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 再生ボタンを押下
                if (playFlg == false) {
                    playButton.setText("停止");
                    nextButton.setEnabled(false);
                    backButton.setEnabled(false);

                    // タイマーの作成
                    mTimer = new Timer();
                    // タイマーの始動
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            nextCursor();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showImage();
                                }
                            });
                        }
                    }, 100, 2000);    // 最初に始動させるまで 100ミリ秒、ループの間隔を 100ミリ秒 に設

                    playFlg = true;

                    // 停止ボタンを押下
                } else {
                    mTimer.cancel();
                    playFlg = false;
                    playButton.setText("再生");
                    nextButton.setEnabled(true);
                    backButton.setEnabled(true);
                }
            }
        });

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(cursor != null) {
            cursor.close();
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        // 最初の画像を表示

        if (cursor.moveToFirst()) {
            showImage();
        } else {
            nextButton.setEnabled(false);
            backButton.setEnabled(false);
            playButton.setEnabled(false);
        }


    }

    private void showImage() {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }


    private void nextCursor() {
        if(!cursor.moveToNext()) {
            cursor.moveToFirst();
        }
    }

    private void prevCursor() {
        if(!cursor.moveToPrevious()) {
            cursor.moveToLast();
        }
    }

}
