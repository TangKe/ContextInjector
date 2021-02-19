package ke.tang.contextinjector.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ke.tang.contextinjector.injector.ContextInject;

public class LauncherActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainProcess:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.noneMainProcess:
                startActivity(new Intent(this, MultiprocessTestActivity.class));
                break;
            case R.id.kotlin:
//                startActivity(new Intent(this, KotlinActivity.class));
                break;
        }
    }
}
