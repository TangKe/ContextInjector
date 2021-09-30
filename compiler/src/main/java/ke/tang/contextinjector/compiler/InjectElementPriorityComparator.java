package ke.tang.contextinjector.compiler;

import java.util.Comparator;

import javax.lang.model.element.Element;

import ke.tang.contextinjector.annotations.InjectContext;

public class InjectElementPriorityComparator implements Comparator<Element> {
    @Override
    public int compare(Element o1, Element o2) {
        final InjectContext injectContext1 = o1.getAnnotation(InjectContext.class);
        final InjectContext injectContext2 = o2.getAnnotation(InjectContext.class);
        if (null == injectContext1) {
            if (null == injectContext2) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (null == injectContext2) {
                return -1;
            } else {
                final int priority1 = injectContext1.priority();
                final int priority2 = injectContext2.priority();
                if (priority1 > priority2) {
                    return -1;
                } else if (priority2 > priority1) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}
