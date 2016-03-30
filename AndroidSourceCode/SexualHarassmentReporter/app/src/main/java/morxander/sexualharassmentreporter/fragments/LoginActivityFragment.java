package morxander.sexualharassmentreporter.fragments;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.activities.MainActivity;
import morxander.sexualharassmentreporter.activities.SignupActivity;
import morxander.sexualharassmentreporter.providers.MainProvider;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoginActivityFragment extends Fragment {

    EditText input_email,input_password;
    TextInputLayout input_layout_email,input_layout_password;
    Button btn_login,btn_signup;
    private ProgressDialog loading_dialog;
    private AsyncHttpClient client;
    private RequestParams params;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_login, container, false);
        initViews(rootView);
        setFont();
        setOnClick();
        return rootView;
    }

    private void initViews(View rootView) {
        input_email = (EditText)rootView.findViewById(R.id.input_email);
        input_password = (EditText)rootView.findViewById(R.id.input_password);
        btn_login = (Button) rootView.findViewById(R.id.btn_login);
        btn_signup = (Button) rootView.findViewById(R.id.btn_signup);
        input_layout_email = (TextInputLayout)rootView.findViewById(R.id.input_layout_email);
        input_layout_password = (TextInputLayout)rootView.findViewById(R.id.input_layout_password);
        loading_dialog = new ProgressDialog(getContext());
        loading_dialog.setMessage(getString(R.string.loading));
    }

    private void setFont() {
        ViewsUtility.changeTypeFace(getContext(),input_email);
        ViewsUtility.changeTypeFace(getContext(),input_password);
        ViewsUtility.changeTypeFace(getContext(),btn_login);
        ViewsUtility.changeTypeFace(getContext(),btn_signup);
        ViewsUtility.changeTypeFace(getContext(),input_layout_email);
        ViewsUtility.changeTypeFace(getContext(),input_layout_password);
    }

    private boolean validatePassword() {
        if (input_password.getText().toString().trim().isEmpty()) {
            input_layout_password.setError(getString(R.string.required));
            ViewsUtility.requestFocus(getActivity(), input_password);
            return false;
        } else {
            input_layout_password.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEmail() {
        String email = input_email.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            input_layout_email.setError(getString(R.string.required));
            ViewsUtility.requestFocus(getActivity(), input_email);
            return false;
        } else {
            input_layout_email.setErrorEnabled(false);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void setOnClick() {
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
                startActivity(new Intent(getActivity(), SignupActivity.class));
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateEmail() && validatePassword()){
                    client = new AsyncHttpClient();
                    params = new RequestParams();
                    params.add("email", input_email.getText().toString());
                    params.add("password", input_password.getText().toString());
                    loading_dialog.show();
                    client.post(getContext(), getString(R.string.api_base_url) + getString(R.string.api_login_url), params, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            loading_dialog.dismiss();
                            try {
                                String str = new String(responseBody, "UTF-8");
                                JSONObject json = new JSONObject(str);
                                if (json.getBoolean(getString(R.string.api_response_status))) {
                                    ContentValues statistics_values = new ContentValues();
                                    statistics_values.put(MainProvider.REPORTS_COUNT, json.getString(getString(R.string.reports_count)));
                                    Uri statistics_uri = getActivity().getContentResolver().insert(
                                            MainProvider.STATISTICS_CONTENT_URI, statistics_values);
                                    ContentValues user_values = new ContentValues();
                                    user_values.put(MainProvider.EMAIL, input_email.getText().toString());
                                    user_values.put(MainProvider.USER_API_ID, json.getString(getString(R.string.api_response_id)));
                                    user_values.put(MainProvider.API_TOKEN, json.getString(getString(R.string.api_response_token)));
                                    Uri user_uri = getActivity().getContentResolver().insert(
                                            MainProvider.USER_CONTENT_URI, user_values);
                                    Intent intent = new Intent(getContext(),MainActivity.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                } else {
                                    Toast.makeText(getContext(), json.getString(getString(R.string.api_response_err_msg)), Toast.LENGTH_LONG).show();
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();
                            loading_dialog.dismiss();

                        }
                    });
                }
            }
        });
    }
}
