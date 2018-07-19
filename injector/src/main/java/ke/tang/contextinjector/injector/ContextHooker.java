package ke.tang.contextinjector.injector;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ServiceLoader;

import ke.tang.contextinjector.annotations.Injector;

/**
 * Created by tangke on 2018/4/20.
 */

public class ContextHooker extends ContentProvider {
    private static Context sApplicationContext;

    public static Context getApplicationContext() {
        return sApplicationContext;
    }

    @Override
    public boolean onCreate() {
        sApplicationContext = getContext().getApplicationContext();
        autoInject();
        return false;
    }

    private void autoInject() {
        final ServiceLoader<Injector> services = ServiceLoader.load(Injector.class);
        for (Injector injector : services) {
            ContextInject.inject(injector.getClass());
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
