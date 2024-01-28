package ke.tang.contextinjector.sample

import android.content.Context
import android.util.Log
import ke.tang.contextinjector.annotations.InjectContext

@InjectContext
fun onContextReady(context: Context) {
    Log.e("Tank", "Android Ready")
}

class KotlinClass {

    internal fun test() {

    }

    class KotlinInnerClass {
        @InjectContext
        lateinit var context: Context

        @InjectContext
        fun contextReady(context: Context) {
            Log.e("Tank", "Android Ready")
        }
    }

    companion object {
        @InjectContext
        fun contextReady(context: Context) {
            Log.e("Tank", "Android Ready")
        }
    }
}