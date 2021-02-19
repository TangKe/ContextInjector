package ke.tang.contextinjector.compiler;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;

class InjectEntry {
    private Element mEnclosingElement;
    private Map<InjectType, Set<Element>> mInjectElements = new HashMap<>();

    public InjectEntry(Element enclosingElement) {
        this.mEnclosingElement = enclosingElement;
    }

    public Element getEnclosingElement() {
        return mEnclosingElement;
    }

    @NotNull
    public Set<Element> getInjectElements(InjectType injectType) {
        Set<Element> elements = mInjectElements.get(injectType);
        if (null == elements) {
            elements = new HashSet<>();
            mInjectElements.put(injectType, elements);
        }
        return elements;
    }
}
