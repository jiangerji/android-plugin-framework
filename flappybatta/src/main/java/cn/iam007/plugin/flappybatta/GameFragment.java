package cn.iam007.plugin.flappybatta;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import cn.iam007.plugin.base.PluginBaseFragment;
import cn.iam007.plugin.dynamicloader.PluginResources;

public class GameFragment extends PluginBaseFragment implements Callback,
        OnClickListener {
    /**
     * set to true in order to print fps in screen.
     */
    private static final boolean SHOW_FPS = false;
    /**
     * set to false to disable coins.
     */
    private static final boolean ENABLE_COIN = true;
    private ImageView ivBackground;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private LinkedList<Sprite> sprites;
    private SoundPool soundPool;

    @SuppressWarnings("unused")
    private static final String TAG = "GameFragment";
    private Drawable blockerUp;
    private Drawable blockerDown;
    private Drawable coin;
    private static final long GAP = 20;
    private static final long NEW_BLOCKER_COUNT = 60;
    private static final long NEW_COIN_COUNT = 60;
    private static final int[] BACKGROUND = new int[]{
            R.drawable.bg_general_day, R.drawable.bg_general_night};

    private Paint globalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean surfaceCreated = false;
    ;
    private Thread drawingTheard;
    private int[] soundIds;
    private Dialog alertDialog;

    private static final int SOUND_DIE = 0;
    private static final int SOUND_HIT = 1;
    private static final int SOUND_POINT = 2;
    private static final int SOUND_SWOOSHING = 3;
    private static final int SOUND_WING = 4;

    private BattaSprite battaSprite;
    private ScoreSprite scoreSprite;
    private GroundSprite groundSprite;
    private SplashSprite splashSprite;
    private FpsSprite fpsSprite;

    private int blockerCount = 0;
    private int coinCount = 0;
    private int lastBlockY = 0;
    private volatile int currentPoint = 0;
    private volatile int currentStatus = Sprite.STATUS_NOT_STARTED;

    private Random random = new Random();

    public static PluginResources mResources;

    public static PluginResources getPluginResource() {
        return mResources;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResources = PluginResources.getResource(getActivity(), GameFragment.class);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = mResources.inflate(getActivity(), R.layout.activity_game,
                container,
                false);// 关联布局文件
        ivBackground = (ImageView) view.findViewById(R.id.iv_background);
        surfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
        surfaceView.setKeepScreenOn(true);
        holder = surfaceView.getHolder();
        surfaceView.setZOrderOnTop(true);
        surfaceView.setOnClickListener(this);
        holder.addCallback(this);
        holder.setFormat(PixelFormat.TRANSLUCENT);
        loadRes();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        restart();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopDrawingThread();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopDrawingThread();
        soundPool.release();

        mResources = null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceCreated = true;
        startDrawingThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceCreated = false;
        stopDrawingThread();
    }

    public void showRestartDialog() {
        int highest = PrefUtil.getHighestScore(getActivity());
        String text = null;
        if (currentPoint > highest) {
            highest = currentPoint;
            PrefUtil.setHighestScore(getActivity(), currentPoint);
        } else {
        }
        text = "本次分数:" + currentPoint + "\n历史高分:" + highest;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Game Over");
        builder.setMessage(text);
        builder.setPositiveButton("再玩一次",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playSwooshing();
                        restart();
                    }
                });
        builder.setNegativeButton("退出游戏",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playSwooshing();
                        getActivity().finish();
                    }
                });
        builder.setCancelable(false);
        alertDialog = builder.show();
    }

    private void restart() {
        if (!getActivity().isFinishing()) {
            ivBackground.setImageResource(BACKGROUND[random
                    .nextInt(BACKGROUND.length)]);
            soundPool.play(soundIds[SOUND_SWOOSHING], 0.5f, 0.5f, 1, 0, 1);
            sprites = new LinkedList<Sprite>();
            battaSprite = new BattaSprite(getActivity());
            scoreSprite = new ScoreSprite(getActivity());
            groundSprite = new GroundSprite(getActivity());
            splashSprite = null;
            sprites.add(scoreSprite);
            sprites.add(groundSprite);
            if (SHOW_FPS) {
                fpsSprite = new FpsSprite(getActivity());
                sprites.add(fpsSprite);
            } else {
                fpsSprite = null;
            }
            sprites.add(battaSprite);
            HintSprite hintSprite = new HintSprite(getActivity());
            sprites.add(hintSprite);
            blockerCount = 0;
            coinCount = (int) (NEW_COIN_COUNT / 2);
            currentPoint = 0;
            lastBlockY = 0;
            currentStatus = Sprite.STATUS_NOT_STARTED;
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
                alertDialog = null;
            }
            if (surfaceCreated) {
                startDrawingThread();
            }
        }
    }

    private void loadRes() {
        //        Resources res = getResources();
        blockerUp = mResources.getDrawable(R.drawable.img_block_up);
        blockerDown = mResources.getDrawable(R.drawable.img_block_down);
        coin = mResources.getDrawable(R.drawable.img_coin);
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        AssetManager assetManager = mResources.getAssets();
        soundIds = new int[5];
        try {
            soundIds[SOUND_DIE] = soundPool.load(assetManager.openFd("sfx_die.ogg"),
                    1);
            soundIds[SOUND_HIT] = soundPool.load(assetManager.openFd("sfx_hit.ogg"),
                    1);
            soundIds[SOUND_POINT] = soundPool.load(
                    assetManager.openFd("sfx_point.ogg"), 1);
            soundIds[SOUND_SWOOSHING] = soundPool.load(
                    assetManager.openFd("sfx_swooshing.ogg"), 1);
            soundIds[SOUND_WING] = soundPool.load(
                    assetManager.openFd("sfx_wing.ogg"), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startDrawingThread() {
        stopDrawingThread();
        drawingTheard = new DrawingThread();
        drawingTheard.start();
    }

    private void stopDrawingThread() {
        if (drawingTheard != null) {
            drawingTheard.interrupt();
            try {
                drawingTheard.join();
            } catch (InterruptedException e) {
            }
            drawingTheard = null;
        }
    }

    @SuppressLint("WrongCall")
    private class DrawingThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!Thread.interrupted()) {
                long startTime = System.currentTimeMillis();
                Canvas canvas = holder.lockCanvas();
                try {
                    cleanCanvas(canvas);
                    Iterator<Sprite> iterator = sprites.iterator();
                    while (iterator.hasNext()) {
                        Sprite sprite = iterator.next();
                        if (sprite.isAlive()) {
                            sprite.onDraw(canvas, globalPaint, currentStatus);
                        } else {
                            iterator.remove();
                            // Log.d(TAG, "remove sprite");
                        }
                    }
                } finally {
                    holder.unlockCanvasAndPost(canvas);
                }
                long duration = (System.currentTimeMillis() - startTime);
                long gap = GAP - duration;
                if (gap > 0) {
                    try {
                        sleep(gap);
                    } catch (Exception e) {
                        break;
                    }
                }
                if (currentStatus == Sprite.STATUS_NOT_STARTED) {
                    continue;
                }
                if (currentStatus == Sprite.STATUS_GAME_OVER) {
                    if (battaSprite.isHit(battaSprite)
                            && !splashSprite.isAlive()) {
                        onGameOver();
                        break;
                    } else {
                        continue;
                    }
                }
                boolean hit = false;
                for (Sprite sprite : sprites) {
                    if (sprite.isHit(battaSprite)) {
                        onHit();
                        hit = true;
                        break;
                    }
                }
                if (hit) {
                    sprites.addLast(splashSprite = new SplashSprite());
                    currentStatus = Sprite.STATUS_GAME_OVER;
                    continue;
                }
                if (blockerCount > NEW_BLOCKER_COUNT) {
                    blockerCount = 0;
                    BlockerSprite sprite = BlockerSprite.obtainRandom(getActivity(),
                            blockerUp,
                            blockerDown,
                            battaSprite.getX(),
                            lastBlockY);
                    lastBlockY = sprite.getUpHeight();
                    sprites.addFirst(sprite);
                    // Log.d(TAG, "new sprite");
                } else {
                    blockerCount++;
                }
                if (ENABLE_COIN) {
                    if (coinCount > NEW_COIN_COUNT) {
                        coinCount = 0;
                        CoinSprite sprite = new CoinSprite(getActivity(),
                                coin);
                        sprites.addFirst(sprite);
                        // Log.d(TAG, "new coin");
                    } else {
                        coinCount++;
                    }
                }
                for (Sprite sprite : sprites) {
                    int point = sprite.getScore();
                    if (point > 0) {
                        onGetPoint(point);
                    }
                }
            }
            Log.d("DrawingThread", "quit");
        }
    }

    private void onGetPoint(int point) {
        currentPoint += point;
        scoreSprite.setCurrentScore(currentPoint);
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    soundPool.play(soundIds[SOUND_POINT], 0.5f, 0.5f, 1, 0, 1);
                }
            }
        });
    }

    private void onGameOver() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    soundPool.play(soundIds[SOUND_DIE], 0.5f, 0.5f, 1, 0, 1);
                    showRestartDialog();
                }
            }
        });
    }

    private void onHit() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    soundPool.play(soundIds[SOUND_HIT], 0.5f, 0.5f, 1, 0, 1);
                }
            }
        });
    }

    private void playSwooshing() {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    soundPool.play(soundIds[SOUND_SWOOSHING],
                            0.5f,
                            0.5f,
                            1,
                            0,
                            1);
                }
            }
        });
    }

    private void cleanCanvas(Canvas canvas) {
        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
    }

    @Override
    public void onClick(View v) {
        switch (currentStatus) {
            case Sprite.STATUS_NOT_STARTED:
                currentStatus = Sprite.STATUS_NORMAL;
            case Sprite.STATUS_NORMAL:
                battaSprite.onTap();
                soundPool.play(soundIds[SOUND_WING], 0.5f, 0.5f, 1, 0, 1);
                break;

            default:
                break;
        }
    }
}
