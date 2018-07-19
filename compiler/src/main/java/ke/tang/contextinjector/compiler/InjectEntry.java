package ke.tang.contextinjector.compiler;

import java.util.Set;

import javax.lang.model.element.Element;

class InjectEntry {
    private Element mElement;
    private Set<Element> mInnerElement;

    public InjectEntry(Element element, Set<Element> innerElement) {
        mElement = element;
        mInnerElement = innerElement;
    }

    public Element getElement() {
        return mElement;
    }

    public void setElement(Element element) {
        mElement = element;
    }

    public Set<Element> getInnerElement() {
        return mInnerElement;
    }

    public void setInnerElement(Set<Element> innerElement) {
        mInnerElement = innerElement;
    }
}
