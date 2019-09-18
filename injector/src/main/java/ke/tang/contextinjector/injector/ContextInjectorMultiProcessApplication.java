package ke.tang.contextinjector.injector;

import android.net.Uri;
import android.support.multidex.MultiDexApplication;

/**
 * If your app contain more than one process, please make your application inherit from this class, because android system not create {@link android.content.ContentProvider} automatically when running in another process of an App
 */
public class ContextInjectorMultiProcessApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        getContentResolver().insert(Uri.parse(String.format("content://%s/", getPackageName().concat(".contexthooker"))), null);
    }
}
