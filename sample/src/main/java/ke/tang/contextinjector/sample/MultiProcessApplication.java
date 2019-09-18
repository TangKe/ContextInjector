package ke.tang.contextinjector.sample;

import android.app.Application;
import android.net.Uri;

public class MultiProcessApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        getContentResolver().query(Uri.parse(String.format("content://%s/", BuildConfig.APPLICATION_ID + ".contexthooker")), null, null, null, null);
    }
}
