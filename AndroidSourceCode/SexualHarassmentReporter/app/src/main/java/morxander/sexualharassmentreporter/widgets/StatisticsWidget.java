package morxander.sexualharassmentreporter.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;

import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.providers.MainProvider;

/**
 * Implementation of App Widget functionality.
 */
public class StatisticsWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        Uri statistics_uri = Uri.parse(MainProvider.STATISTICS_URL);
        Uri user_uri = Uri.parse(MainProvider.USER_URL);
        Cursor statistics_cursor = context.getContentResolver().query(statistics_uri, null, null, null, null);
        Cursor user_cursor = context.getContentResolver().query(user_uri, null, null, null, null);
        statistics_cursor.moveToFirst();
        user_cursor.moveToFirst();
        CharSequence reports_count = "Reports Count : " + statistics_cursor.getInt(statistics_cursor.getColumnIndex(MainProvider.REPORTS_COUNT));
        CharSequence user_email = "Welcome " + user_cursor.getString(user_cursor.getColumnIndex(MainProvider.EMAIL));
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.statistics_widget);
        views.setTextViewText(R.id.appwidget_text, reports_count);
        views.setTextViewText(R.id.welcome_txt, user_email);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

