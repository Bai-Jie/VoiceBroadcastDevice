package gq.baijie.voicebroadcastdevice.ui.activity;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.umeng.update.UmengUpdateAgent;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import gq.baijie.voicebroadcastdevice.R;
import gq.baijie.voicebroadcastdevice.entity.Sound;
import gq.baijie.voicebroadcastdevice.storage.database.Contract;
import gq.baijie.voicebroadcastdevice.storage.database.DatabaseHelper;

import static gq.baijie.voicebroadcastdevice.storage.database.Contract.PATH_CATEGORY;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.PATH_SOUND;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.PATH_SOUND_CATEGORY;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.Sound.COLUMN_FILE_NAME;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.Sound.COLUMN_TITLE;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.Sound._ID;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.COLUMN_CATEGORY_ID;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.COLUMN_SOUND_ID;


public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // select s.id, s.title, s.filename from sound, category, sc
    // where c.name = 'Default' and sc.cid = c.id and sc.sid=s.id
    // order by sc.order
    private static final String QUERY_SOUNDS =
            "SELECT " + PATH_SOUND + "." + _ID + ", " + PATH_SOUND + "." + COLUMN_TITLE
                    + ", " + PATH_SOUND + "." + COLUMN_FILE_NAME
            + " FROM " + PATH_CATEGORY + ", " + PATH_SOUND_CATEGORY + ", " + PATH_SOUND
            + " WHERE " + PATH_CATEGORY + "." + Contract.Category.COLUMN_NAME + " = 'Default'"
                    + " AND " + PATH_SOUND_CATEGORY + "." + COLUMN_CATEGORY_ID + " = "
                              + PATH_CATEGORY + "." + Contract.Category._ID
                    + " AND " + PATH_SOUND + "." + _ID + " = "
                              + PATH_SOUND_CATEGORY + "." + COLUMN_SOUND_ID;

    private List<Sound> mSounds;

    private MultiSelector mMultiSelector = new MultiSelector();

    private MediaPlayer mPlayer;

    private SoundsAdapter mAdapter;

    private RecyclerView mRecyclerView;

    private ActionMode mActionMode;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_menu_main, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mMultiSelector.setSelectable(true);
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteSelectedSounds();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMultiSelector.clearSelections();
            mMultiSelector.setSelectable(false);
            mActionMode = null;
        }

        private void deleteSelectedSounds() {
            List<Sound> sounds = new LinkedList<Sound>();
            for (int position : mMultiSelector.getSelectedPositions()) {
                sounds.add(mSounds.get(position));
            }
            for (Sound sound : sounds) {
                deleteSound(sound);
            }
        }
    };

    //--------------------------------------------------------------------------
    // Override Methods of Activity
    //--------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //友盟自动更新
        UmengUpdateAgent.update(this);

        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new SoundsAdapter();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadSounds();
        mPlayer = new MediaPlayer();
    }

    @Override
    protected void onStop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mRecyclerView.setAdapter(null);
        mRecyclerView = null;
        mAdapter = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_add:
                startActivity(new Intent(this, AddItemActivity.class));
                return true;
            case R.id.action_edit:
                if (mActionMode == null) {
                    mActionMode = startActionMode(mActionModeCallback);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //--------------------------------------------------------------------------
    // New Declared Methods
    //--------------------------------------------------------------------------

    private String getFileStreamAbsolutePath(String fileName) {
        return getFileStreamPath(fileName).getAbsolutePath();
    }

    private boolean soundsIsEmpty() {
        return mSounds == null || mSounds.isEmpty();
    }

    private void loadSounds() {
        try {
            Dao<Sound, Long> soundDao = getHelper().getSoundDao();
            GenericRawResults<Sound> soundGenericRawResults = soundDao
                    .queryRaw(QUERY_SOUNDS, new RawRowMapper<Sound>() {//TODO use DataType?
                        @Override
                        public Sound mapRow(String[] columnNames, String[] resultColumns)
                                throws SQLException {
                            Sound result = new Sound();
                            result.setId(Long.parseLong(resultColumns[0]))
                                    .setTitle(resultColumns[1])
                                    .setFileName(resultColumns[2]);
                            return result;
                        }
                    });
            List<Sound> sounds = soundGenericRawResults.getResults();
            sounds = new ArrayList<>(sounds);
            mSounds = sounds;
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        } catch (SQLException e) {
            e.printStackTrace();//TODO
        }
    }

    private void deleteSound(Sound sound) {
        try {
            Dao<Sound, Long> soundDao = getHelper().getSoundDao();
            soundDao.delete(sound);
            getFileStreamPath(sound.getFileName()).delete();
            int position = mSounds.indexOf(sound);
            mSounds.remove(sound);//TODO check result
            if (mAdapter != null) {
                mAdapter.notifyItemRemoved(position);
            }
        } catch (SQLException e) {
            e.printStackTrace();//TODO
        }
    }

    private boolean startPlay(String path) {
        if (mPlayer == null) {
            return false;
        }
        mPlayer.reset();
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepare();
            mPlayer.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //--------------------------------------------------------------------------
    // Nested Classes
    //--------------------------------------------------------------------------

    private class SoundsAdapter extends RecyclerView.Adapter<SoundViewHolder> {

        @Override
        public SoundViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new SoundViewHolder(inflater.inflate(R.layout.list_item_sound, parent, false));
        }

        @Override
        public void onBindViewHolder(SoundViewHolder holder, final int position) {
            holder.bindSound(mSounds.get(position));
        }

        @Override
        public int getItemCount() {
            if (soundsIsEmpty()) {
                return 0;
            } else {
                return mSounds.size();
            }
        }
    }

    public class SoundViewHolder extends SwappingHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private Sound mSound;

        private TextView mTitleTextView;

        public SoundViewHolder(View itemView) {
            super(itemView, mMultiSelector);
            mTitleTextView = (TextView) itemView.findViewById(R.id.title);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setLongClickable(true);
            setSelectionModeBackgroundDrawable(
                    getResources().getDrawable(R.drawable.activatable_item_background));
        }

        public void bindSound(Sound sound) {
            mSound = sound;
            mTitleTextView.setText(sound != null ? sound.getTitle() : "");
        }

        @Override
        public void onClick(View v) {
            if (mSound == null) {
                return;
            }
            if (mMultiSelector.tapSelection(this)) {
                int selectionsSize = mMultiSelector.getSelectedPositions().size();
                mActionMode.setTitle(String.valueOf(selectionsSize));
                if (selectionsSize == 0) {
                    mActionMode.finish();
                }
            } else { //isn't Selectable
                startPlay(getFileStreamAbsolutePath(mSound.getFileName()));
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mActionMode == null) {
                mActionMode = startActionMode(mActionModeCallback);
                mMultiSelector.setSelected(this, true);
                mActionMode.setTitle("1");
                return true;
            }
            return false;
        }
    }


}
