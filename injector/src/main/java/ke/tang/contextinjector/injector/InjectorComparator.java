package ke.tang.contextinjector.injector;

import java.util.Comparator;

import ke.tang.contextinjector.annotations.Injector;

public class InjectorComparator implements Comparator<Injector> {
    @Override
    public int compare(Injector o1, Injector o2) {
        final int priority1 = o1.getPriority();
        final int priority2 = o2.getPriority();
        final int diff = priority1 - priority2;
        return diff > 0 ? -1 : (diff < 0 ? 1 : 0);
    }
}
