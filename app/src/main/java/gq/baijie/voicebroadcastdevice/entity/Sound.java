package gq.baijie.voicebroadcastdevice.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static gq.baijie.voicebroadcastdevice.storage.database.Contract.PATH_SOUND;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.Sound.COLUMN_FILE_NAME;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.Sound.COLUMN_TITLE;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.Sound._ID;

@DatabaseTable(tableName = PATH_SOUND)
public class Sound {

    @DatabaseField(columnName = _ID, generatedId = true)
    private long mId;

    @DatabaseField(columnName = COLUMN_TITLE, canBeNull = false)
    private String mTitle;

    @DatabaseField(columnName = COLUMN_FILE_NAME, canBeNull = false)
    private String mFileName;

    public long getId() {
        return mId;
    }

    public Sound setId(long id) {
        mId = id;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public Sound setTitle(String title) {
        mTitle = title;
        return this;
    }

    public String getFileName() {
        return mFileName;
    }

    public Sound setFileName(String fileName) {
        mFileName = fileName;
        return this;
    }
}
