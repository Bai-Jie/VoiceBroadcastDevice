package gq.baijie.voicebroadcastdevice.storage.database;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;

import gq.baijie.voicebroadcastdevice.R;
import gq.baijie.voicebroadcastdevice.entity.Category;
import gq.baijie.voicebroadcastdevice.entity.Sound;
import gq.baijie.voicebroadcastdevice.entity.SoundCategory;

import static gq.baijie.voicebroadcastdevice.storage.database.Contract.PATH_SOUND_CATEGORY;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.COLUMN_CATEGORY_ID;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.COLUMN_ORDER_IN_CATEGORY;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.COLUMN_SOUND_ID;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.UNIQUE_INDEX_CATEGORY_ORDER;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.UNIQUE_INDEX_SOUND_CATEGORY;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "broadcast.db";

    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_UNIQUE_INDEX_SOUND_CATEGORY =
            "CREATE UNIQUE INDEX " + UNIQUE_INDEX_SOUND_CATEGORY + " ON " + PATH_SOUND_CATEGORY
                    + "(" + COLUMN_SOUND_ID + ", " + COLUMN_CATEGORY_ID + ")";

    private static final String CREATE_UNIQUE_INDEX_CATEGORY_ORDER =
            "CREATE UNIQUE INDEX " + UNIQUE_INDEX_CATEGORY_ORDER + " ON " + PATH_SOUND_CATEGORY
                    + "(" + COLUMN_CATEGORY_ID + ", " + COLUMN_ORDER_IN_CATEGORY + ")";

    private Dao<Category, Long> mCategoryDao;

    private Dao<Sound, Long> mSoundDao;

    private Dao<SoundCategory, ?> mSoundCategoryDao;

    private final Object mCategoryDaoLock = new Object();

    private final Object mSoundDaoLock = new Object();

    private final Object mSoundCategoryDaoLock = new Object();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Category.class);
            TableUtils.createTable(connectionSource, Sound.class);
            TableUtils.createTable(connectionSource, SoundCategory.class);
            database.execSQL(CREATE_UNIQUE_INDEX_SOUND_CATEGORY);
            database.execSQL(CREATE_UNIQUE_INDEX_CATEGORY_ORDER);
            // create default category
            Dao<Category, Long> categoryDao = getDao(Category.class);
            Category defaultCategory = new Category();
            defaultCategory.setName("Default");
            categoryDao.create(defaultCategory);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getSimpleName(), "Unable to create database:", e);
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource,
            int oldVersion, int newVersion) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    public Dao<Category, Long> getCategoryDao() throws SQLException {
        synchronized (mCategoryDaoLock) {
            if (mCategoryDao == null) {
                mCategoryDao = getDao(Category.class);
            }
            return mCategoryDao;
        }
    }

    public Dao<Sound, Long> getSoundDao() throws SQLException {
        synchronized (mSoundDaoLock) {
            if (mSoundDao == null) {
                mSoundDao = getDao(Sound.class);
            }
            return mSoundDao;
        }
    }

    public Dao<SoundCategory, ?> getSoundCategoryDao() throws SQLException {
        synchronized (mSoundCategoryDaoLock) {
            if (mSoundCategoryDao == null) {
                mSoundCategoryDao = getDao(SoundCategory.class);
            }
            return mSoundCategoryDao;
        }
    }
}
