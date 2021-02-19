package ke.tang.contextinjector.sample.inner;

import android.content.Context;

import ke.tang.contextinjector.annotations.InjectContext;

public class JavaInjectTest {
    static class Java {
        @InjectContext
        public static void onContextReady(Context context) {

        }
    }
}
