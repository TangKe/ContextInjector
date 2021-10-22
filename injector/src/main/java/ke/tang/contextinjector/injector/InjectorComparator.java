package ke.tang.contextinjector.injector;

import java.lang.reflect.Method;
import java.util.Comparator;

import ke.tang.contextinjector.annotations.Injector;

/**
 * 为了兼容老版本，所以使用反射处理
 */
public class InjectorComparator implements Comparator<Injector> {
    private static Method GET_PRIORITY_METHOD;

    static {
        try {
            GET_PRIORITY_METHOD = Injector.class.getDeclaredMethod("getPriority");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int compare(Injector o1, Injector o2) {
        final int priority1 = getPriorityCompat(o1);
        final int priority2 = getPriorityCompat(o2);
        // 越大优先级越高
        return Integer.compare(priority2, priority1);
    }

    private int getPriorityCompat(Injector o) {
        try {
            return (int) GET_PRIORITY_METHOD.invoke(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
