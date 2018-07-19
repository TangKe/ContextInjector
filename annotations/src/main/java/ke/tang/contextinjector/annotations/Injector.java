package ke.tang.contextinjector.annotations;

/**
 * 注入器基类，所有生成的注入器都会继承自该类
 *
 * @param <T> 需要注入的类型
 */
public abstract class Injector<T> {
    public void inject(T target) {
        if (null != target) {
            injectInstance(target);
        }
        injectStatic();
    }

    /**
     * 生成的子类中，该方法注入所有静态方法和静态变量
     */
    protected abstract void injectStatic();

    /**
     * 生成的子类中，该方法注入所有实例方法和实例变量
     *
     * @param target
     */
    protected abstract void injectInstance(T target);

}
