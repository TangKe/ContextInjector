package ke.tang.contextinjector.injector;

import java.util.WeakHashMap;

import ke.tang.contextinjector.annotations.Constants;
import ke.tang.contextinjector.annotations.Injector;

/**
 * {@link android.content.Context}注入入口类
 */
public class ContextInject {
    private static WeakHashMap<Class<?>, Class<? extends Injector<?>>> sInjectorClassCache = new WeakHashMap<>();
    private static WeakHashMap<Class<? extends Injector>, Injector<?>> sInjectorCache = new WeakHashMap<>();

    /**
     * 支持任意包含{@link ContextInject}注解的类的注入
     * 支持实例对象、类
     * <ul>
     * <li>当参数为实例对象时，会注入被{@link ContextInject}注解标记的实例变量、静态变量、实例方法、静态方法</li>
     * <li>当参数为类时，会注入被{@link ContextInject}注解标记静态变量、静态方法</li>
     * </ul>
     *
     * @param obj
     */
    public static void inject(Object obj) {
        boolean isClass = obj instanceof Class;
        final Class<? extends Injector<?>> injectorClass = findInjectorClass(obj);
        if (null == injectorClass) {
            throw new IllegalStateException(obj.getClass().getName() + " 找不到对应的注入类，请检查注解处理器是否正常工作或者混淆设置是否正确");
        }
        Injector injector = sInjectorCache.get(injectorClass);
        if (null != injector) {
            injector.inject(isClass ? null : obj);
        } else {
            try {
                injector = injectorClass.newInstance();
                sInjectorCache.put(injectorClass, injector);
                injector.inject(isClass ? null : obj);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static Class<? extends Injector<?>> findInjectorClass(Object obj) {
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();

        if (Injector.class.isAssignableFrom(clazz)) {
            return (Class<? extends Injector<?>>) clazz;
        }

        Class<? extends Injector<?>> injectorClass = sInjectorClassCache.get(clazz);
        if (null != injectorClass) {
            return injectorClass;
        }

        try {
            injectorClass = (Class<? extends Injector<?>>) Class.forName(Constants.buildInjectorClassName(clazz.getCanonicalName()));
        } catch (ClassNotFoundException e) {
            if (null != clazz.getSuperclass()) {
                injectorClass = findInjectorClass(clazz.getSuperclass());
            }
        }
        if (null != injectorClass) {
            sInjectorClassCache.put(clazz, injectorClass);
        }
        return injectorClass;
    }


}
