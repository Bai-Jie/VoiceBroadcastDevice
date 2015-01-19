package gq.baijie.voicebroadcastdevice.storage.database;

import android.provider.BaseColumns;

public final class Contract {

    public static final String PATH_SOUND = "sound";

    public static final String PATH_CATEGORY = "category";

    public static final String PATH_SOUND_CATEGORY = "sound_category";

    public static final class Sound implements BaseColumns {

        public static final String COLUMN_TITLE = "title";

        public static final String COLUMN_FILE_NAME = "file_name";

    }

    public static final class Category implements BaseColumns {

        public static final String COLUMN_NAME = "name";
    }

    public static final class SoundCategory {

        public static final String COLUMN_SOUND_ID = "sound_id";

        public static final String COLUMN_CATEGORY_ID = "category_id";

        public static final String COLUMN_ORDER_IN_CATEGORY = "order_in_category";

        public static final String UNIQUE_INDEX_SOUND_CATEGORY =
                PATH_SOUND_CATEGORY + "_unique_index_" + COLUMN_SOUND_ID + "_" + COLUMN_CATEGORY_ID;

        public static final String UNIQUE_INDEX_CATEGORY_ORDER = PATH_SOUND_CATEGORY
                + "_unique_index_" + COLUMN_CATEGORY_ID + "_" + COLUMN_ORDER_IN_CATEGORY;
    }

}
