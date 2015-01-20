package gq.baijie.voicebroadcastdevice.ui.activity;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import gq.baijie.voicebroadcastdevice.R;
import gq.baijie.voicebroadcastdevice.entity.Category;
import gq.baijie.voicebroadcastdevice.entity.Sound;
import gq.baijie.voicebroadcastdevice.entity.SoundCategory;
import gq.baijie.voicebroadcastdevice.storage.database.Contract;
import gq.baijie.voicebroadcastdevice.storage.database.DatabaseHelper;
import gq.baijie.voicebroadcastdevice.util.Utils;

public class AddItemActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    private static final String LOG_TAG = AddItemActivity.class.getSimpleName();


    private boolean mIsPlaying = false;

    private boolean mIsRecording = false;

    private File mFileName;

    private MediaRecorder mRecorder;

    private MediaPlayer mPlayer;

    private EditText mTitleEditText;

    private TextView mDurationTextView;

    private Button mPlayButton;

    private Button mRecordButton;

    //--------------------------------------------------------------------------
    // Override Methods of Activity
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFile();

        setContentView(R.layout.add_item);
        mTitleEditText = (EditText) findViewById(R.id.title);
        mDurationTextView = (TextView) findViewById(R.id.duration);
        mPlayButton = (Button) findViewById(R.id.play);
        mRecordButton = (Button) findViewById(R.id.record);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(!mIsPlaying);
            }
        });
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(!mIsRecording);
            }
        });
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onPlay(false);
            }
        });
    }

    @Override
    public void onPause() {
        if (mFileName.exists()) {
            save();//TODO delete
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mRecordButton = null;
        mPlayButton = null;
        mDurationTextView = null;
        mTitleEditText = null;
        super.onDestroy();
    }

    //--------------------------------------------------------------------------
    // New Declared Methods
    //--------------------------------------------------------------------------

    private void setFile() {
        do {
            mFileName = getFileStreamPath(System.currentTimeMillis() + ".3gp");
        } while (mFileName.exists());
    }

    private void deleteFile() {
        if (mFileName.exists()) {
            mFileName.delete();
        }
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private boolean preparePlayer() {
        if (mPlayer == null) {
            return false;
        }
        mPlayer.reset();
        try {
            mPlayer.setDataSource(mFileName.getAbsolutePath());
            mPlayer.prepare();
            if (mDurationTextView != null) {
                mDurationTextView.setText(Utils.formatDuration(mPlayer.getDuration() / 1000));
            }
            if (mPlayButton != null) {
                mPlayButton.setEnabled(true);
            }
            return true;
        } catch (IOException e) {
            Log.e(LOG_TAG, "mPlayer.prepare() failed", e);
            if (mDurationTextView != null) {
                mDurationTextView.setText("");
            }
            if (mPlayButton != null) {
                mPlayButton.setEnabled(false);
            }
            return false;
        }
    }

    private void startPlaying() {
        try {
            mPlayer.start();
            mPlayButton.setText(R.string.stop);
            mIsPlaying = true;
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "mPlayer.start() failed", e);
        }
    }

    private void stopPlaying() {
        mPlayButton.setText(R.string.play);
        mIsPlaying = false;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName.getAbsolutePath());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
            mRecordButton.setText(R.string.stop);
            mIsRecording = true;
        } catch (IOException e) {
            Log.e(LOG_TAG, "mRecorder.prepare() failed", e);
            stopRecording();
        }
    }

    private void stopRecording() {
        try {
            mRecorder.stop();
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "mRecorder.stop() failed", e);
        }
        mRecorder.release();
        mRecorder = null;
        mRecordButton.setText(R.string.record);
        mIsRecording = false;

        preparePlayer();
    }

    private void clear() {
        deleteFile();
        mPlayer.reset();
        if (mTitleEditText != null) {
            mTitleEditText.setText("");
        }
        if (mDurationTextView != null) {
            mDurationTextView.setText("");
        }
        if (mPlayButton != null) {
            mPlayButton.setEnabled(false);
        }
    }

    private void save() {
        Sound sound = new Sound();
        sound.setFileName(mFileName.getName());
        sound.setTitle(mTitleEditText.getText().toString());
        DatabaseHelper databaseHelper = getHelper();
        try {
            Dao<Category, Long> categoryDao = databaseHelper.getCategoryDao();
            Dao<Sound, Long> soundDao = databaseHelper.getSoundDao();
            Dao<SoundCategory, ?> soundCategoryDao = databaseHelper.getSoundCategoryDao();
            Category category =
                    categoryDao.queryForEq(Contract.Category.COLUMN_NAME, "Default").get(0);
            QueryBuilder<SoundCategory, ?> soundCategoryQueryBuilder = soundCategoryDao
                    .queryBuilder()
                    .selectRaw("max(" + Contract.SoundCategory.COLUMN_ORDER_IN_CATEGORY + ")");
            soundCategoryQueryBuilder.where()
                    .eq(Contract.SoundCategory.COLUMN_CATEGORY_ID, category.getId());
            long maxOrder = soundCategoryDao
                    .queryRawValue(soundCategoryQueryBuilder.prepareStatementString());
            soundDao.create(sound);
            SoundCategory soundCategory = new SoundCategory()
                    .setCategory(category)
                    .setSound(sound)
                    .setOrderInCategory(maxOrder + 1);
            soundCategoryDao.create(soundCategory);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "save() failed", e);
        }
    }

}
