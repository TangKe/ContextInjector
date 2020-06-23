package ke.tang.contextinjector.sample;

import android.app.Application;

import ke.tang.contextinjector.injector.ContextInject;

public class MultiProcessApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ContextInject.installMultiProcess(this);
    }
}
