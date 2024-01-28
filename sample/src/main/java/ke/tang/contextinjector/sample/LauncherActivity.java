package ke.tang.contextinjector.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.mainProcess) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.noneMainProcess) {
            startActivity(new Intent(this, MultiprocessTestActivity.class));
        } else if (id == R.id.kotlin) {
//                startActivity(new Intent(this, KotlinActivity.class));
        }
    }
}
