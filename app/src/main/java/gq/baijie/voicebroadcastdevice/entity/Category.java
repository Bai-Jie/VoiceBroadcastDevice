package gq.baijie.voicebroadcastdevice.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static gq.baijie.voicebroadcastdevice.storage.database.Contract.Category.COLUMN_NAME;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.Category._ID;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.PATH_CATEGORY;

@DatabaseTable(tableName = PATH_CATEGORY)
public class Category {

    @DatabaseField(columnName = _ID, generatedId = true)
    private long mId;

    @DatabaseField(columnName = COLUMN_NAME)
    private String mName;

    public long getId() {
        return mId;
    }

    public Category setId(long id) {
        mId = id;
        return this;
    }

    public String getName() {
        return mName;
    }

    public Category setName(String name) {
        mName = name;
        return this;
    }
}
