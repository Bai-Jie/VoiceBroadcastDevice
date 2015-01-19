package gq.baijie.voicebroadcastdevice.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import gq.baijie.voicebroadcastdevice.storage.database.Contract;

import static gq.baijie.voicebroadcastdevice.storage.database.Contract.PATH_CATEGORY;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.PATH_SOUND;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.PATH_SOUND_CATEGORY;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.COLUMN_CATEGORY_ID;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.COLUMN_ORDER_IN_CATEGORY;
import static gq.baijie.voicebroadcastdevice.storage.database.Contract.SoundCategory.COLUMN_SOUND_ID;

// unique indexes are created in DatabaseHelper
@DatabaseTable(tableName = PATH_SOUND_CATEGORY)
public class SoundCategory {

    @DatabaseField(columnName = COLUMN_SOUND_ID,
            foreign = true, columnDefinition = "INTEGER REFERENCES "
            + PATH_SOUND + "(" + Contract.Sound._ID + ") ON DELETE CASCADE ON UPDATE CASCADE")
    private Sound mSound;

    @DatabaseField(columnName = COLUMN_CATEGORY_ID,
            foreign = true, columnDefinition = "INTEGER REFERENCES "
            + PATH_CATEGORY + "(" + Contract.Category._ID + ") ON DELETE CASCADE ON UPDATE CASCADE")
    private Category mCategory;

    @DatabaseField(columnName = COLUMN_ORDER_IN_CATEGORY)
    private long mOrderInCategory;

    public Sound getSound() {
        return mSound;
    }

    public SoundCategory setSound(Sound sound) {
        mSound = sound;
        return this;
    }

    public Category getCategory() {
        return mCategory;
    }

    public SoundCategory setCategory(Category category) {
        mCategory = category;
        return this;
    }

    public long getOrderInCategory() {
        return mOrderInCategory;
    }

    public SoundCategory setOrderInCategory(long orderInCategory) {
        mOrderInCategory = orderInCategory;
        return this;
    }
}
