package morxander.sexualharassmentreporter.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import morxander.sexualharassmentreporter.R;

/**
 * Created by morxander on 2/28/16.
 */
public class ViewsUtility {

    private static Typeface getTypeFace(Context context){
        Typeface face = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.current_font_path));
        return face;
    }

    public static void changeTypeFace(Context context, EditText editText){
        Typeface face = getTypeFace(context);
        editText.setTypeface(face);
    }

    public static void changeTypeFace(Context context, TextView textView){
        Typeface face = getTypeFace(context);
        textView.setTypeface(face);
    }

    public static void changeTypeFace(Context context, RadioButton radioButton){
        Typeface face = getTypeFace(context);
        radioButton.setTypeface(face);
    }

    public static void changeTypeFace(Context context, Button button){
        Typeface face = getTypeFace(context);
        button.setTypeface(face);
    }

    public static void changeTypeFace(Context context, Switch view){
        Typeface face = getTypeFace(context);
        view.setTypeface(face);
    }

    public static void changeTypeFace(Context context, TextInputLayout text){
        Typeface face = getTypeFace(context);
        text.setTypeface(face);
    }

    public static void requestFocus(Activity activity, View view) {
        if (view.requestFocus()) {
            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

}
