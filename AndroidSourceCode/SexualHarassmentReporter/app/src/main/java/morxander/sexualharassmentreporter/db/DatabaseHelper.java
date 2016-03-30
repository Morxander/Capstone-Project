package morxander.sexualharassmentreporter.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by morxander on 3/29/16.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    public static final String DATABASE_NAME = "Users";
    public static final String USERS_TABLE_NAME = "users_table";
    public static final String STATISTICS_TABLE_NAME = "statistics";
    static final int DATABASE_VERSION = 1;
    static final String CREATE_USER_TABLE =
            " CREATE TABLE " + USERS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " user_id INTEGER NOT NULL, " +
                    " api_token TEXT NOT NULL, " +
                    " email TEXT NOT NULL);";
    static final String CREATE_STATISTICS_TABLE =
            "CREATE TABLE " + STATISTICS_TABLE_NAME +
                    " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    " reports_count INTEGER NOT NULL);";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_STATISTICS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_STATISTICS_TABLE);
        onCreate(db);
    }
}
