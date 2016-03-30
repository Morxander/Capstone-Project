package morxander.sexualharassmentreporter.utilities;

import android.support.v7.app.AppCompatActivity;

/**
 * Created by morxander on 3/27/16.
 */
public class UserProvider {
    public static int getUserId(final AppCompatActivity activity){
//        activity.getSupportLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
//            @Override
//            public Loader<Cursor> onCreateLoader(int arg0, Bundle cursor) {
//                return new CursorLoader(activity,
//                        ContentProvider.createUri(UserModel.class, null),
//                        null, null, null, null
//                );
//            }
//
//            @Override
//            public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
//                data.moveToFirst();
//                int user_id_position = data.getColumnIndex("user_id");
//                int user_id = data.getInt(user_id_position);
//            }
//
//            @Override
//            public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
//
//            }
//        });
        return 0;
    }
}
