package morxander.sexualharassmentreporter.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.HashMap;

import morxander.sexualharassmentreporter.db.DatabaseHelper;

/**
 * Created by morxander on 3/28/16.
 */
public class MainProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "morxander.sexualharassmentreporter";
    public static final String USER_URL = "content://" + PROVIDER_NAME + "/users";
    public static final Uri USER_CONTENT_URI = Uri.parse(USER_URL);
    public static final String STATISTICS_URL = "content://" + PROVIDER_NAME + "/statistics";
    public static final Uri STATISTICS_CONTENT_URI = Uri.parse(STATISTICS_URL);

    public static final int USERS = 1;
    public static final int USER_ID = 2;
    public static final int STATISTICS = 3;
    public static final int STATISTICS_ID = 4;
    public static final String _ID = "_id";

    public static final String USER_API_ID = "user_id";
    public static final String API_TOKEN = "api_token";
    public static final String EMAIL = "email";
    public static final String REPORTS_COUNT = "reports_count";

    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "users", USERS);
        uriMatcher.addURI(PROVIDER_NAME, "users/#", USER_ID);
        uriMatcher.addURI(PROVIDER_NAME, "statistics", STATISTICS);
        uriMatcher.addURI(PROVIDER_NAME, "statistics/#", STATISTICS_ID);
    }

    private static HashMap<String, String> USERS_PROJECTION_MAP,STATISTICS_PROJECTION_MAP;

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        /**
         * Create a write able database which will trigger its
         * creation if it doesn't already exist.
         */
        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (uriMatcher.match(uri)) {
            case USERS:
                qb.setTables(DatabaseHelper.USERS_TABLE_NAME);
                qb.setProjectionMap(USERS_PROJECTION_MAP);
                break;

            case USER_ID:
                qb.setTables(DatabaseHelper.USERS_TABLE_NAME);
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            case STATISTICS:
                qb.setTables(DatabaseHelper.STATISTICS_TABLE_NAME);
                qb.setProjectionMap(STATISTICS_PROJECTION_MAP);
                break;

            case STATISTICS_ID:
                qb.setTables(DatabaseHelper.STATISTICS_TABLE_NAME);
                qb.appendWhere( _ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (sortOrder == null || sortOrder == ""){
            // By default sort on API ID
            sortOrder = _ID;
        }
        Cursor c = qb.query(db,	projection,	selection, selectionArgs,null, null, sortOrder);
         // register to watch a content URI for changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case USERS:
                return "vnd.android.cursor.dir/" + PROVIDER_NAME + "users";
            case USER_ID:
                return "vnd.android.cursor.item/" + PROVIDER_NAME + ".user";
            case STATISTICS:
                return "vnd.android.cursor.dir/" + PROVIDER_NAME + "statistics";
            case STATISTICS_ID:
                return "vnd.android.cursor.item/" + PROVIDER_NAME + ".statistic";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri _uri = null;
        switch (uriMatcher.match(uri)){
            case USERS:
                long _ID1 = db.insert(DatabaseHelper.USERS_TABLE_NAME, "", contentValues);
                if (_ID1 > 0) {
                    _uri = ContentUris.withAppendedId(USER_CONTENT_URI, _ID1);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            case STATISTICS:
                long _ID2 = db.insert(DatabaseHelper.STATISTICS_TABLE_NAME, "", contentValues);
                if (_ID2 > 0) {
                    _uri = ContentUris.withAppendedId(STATISTICS_CONTENT_URI, _ID2);
                    getContext().getContentResolver().notifyChange(_uri, null);
                }
                break;
            default: throw new SQLException("Failed to insert row into " + uri);
        }
        return _uri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
