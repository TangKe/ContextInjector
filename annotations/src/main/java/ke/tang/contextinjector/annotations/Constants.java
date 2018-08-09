package ke.tang.contextinjector.annotations;

/**
 * 注解常量
 */
public class Constants {
    private final static String GENERATE_CLASS_NAME_POSTFIX = "_Injector";

    /**
     * 获取指定类名对应的Injector类名
     *
     * @param className
     * @return
     */
    public static String buildInjectorClassName(String className) {
        return className.concat(GENERATE_CLASS_NAME_POSTFIX);
    }

    /**
     * 获取指定类名对应的Injector类名
     *
     * @param simpleClassName
     * @return
     */
    public static String buildInjectorSimpleClassName(String simpleClassName) {
        return simpleClassName.concat(GENERATE_CLASS_NAME_POSTFIX);
    }
}
