package morxander.sexualharassmentreporter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.query.Select;

import morxander.sexualharassmentreporter.R;
import morxander.sexualharassmentreporter.db.UserModel;
import morxander.sexualharassmentreporter.utilities.ViewsUtility;

public class splashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Custom animation on image
        ImageView myView = (ImageView)findViewById(R.id.logo);
        TextView appName = (TextView)findViewById(R.id.app_name);
        ViewsUtility.changeTypeFace(this,appName);

        ObjectAnimator fadeInLogo = ObjectAnimator.ofFloat(myView, "alpha", .3f, 1f);
        fadeInLogo.setDuration(3000);

        ObjectAnimator fadeInName = ObjectAnimator.ofFloat(appName, "alpha", .3f, 1f);
        fadeInName.setDuration(3000);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeInLogo).with(fadeInName);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                UserModel db = new Select().from(UserModel.class).executeSingle();
                if(db == null){
                    finish();
                    startActivity(new Intent(splashActivity.this,LoginActivity.class));
                }else{
                    finish();
                    startActivity(new Intent(splashActivity.this,MainActivity.class));
                }

            }
        });
        mAnimationSet.start();
    }
}
