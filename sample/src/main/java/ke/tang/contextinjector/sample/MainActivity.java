package ke.tang.contextinjector.sample;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import ke.tang.contextinjector.annotations.InjectContext;
import ke.tang.contextinjector.injector.ContextInject;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @InjectContext(priority = 100)
    static Context sStaticContext;

    @InjectContext(priority = 1000)
    static void onStaticContextReady(Context context) {
        Toast.makeText(context, R.string.toast_from_static_method, Toast.LENGTH_SHORT).show();
    }

    @InjectContext
    void onInstanceContextReady(Context context) {
        Toast.makeText(context, R.string.toast_from_instance_method, Toast.LENGTH_SHORT).show();
    }

    @InjectContext
    Context mInstanceContext;

    private TextView mState;
    private Button mInject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mState = findViewById(R.id.state);
        mInject = findViewById(R.id.inject);
        mInject.setOnClickListener(this);
        print();
    }

    private void print() {
        StringBuilder builder = new StringBuilder();
        builder.append("Static Context Field: " + String.valueOf(sStaticContext));
        builder.append("\n");
        builder.append("Instance Context Field: " + String.valueOf(mInstanceContext));
        mState.setText(builder);
    }

    @Override
    public void onClick(View view) {
        ContextInject.inject(this);
        print();
    }
}
