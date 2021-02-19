package ke.tang.contextinjector.compiler;

import javax.lang.model.element.Element;

import kotlin.Metadata;
import kotlinx.metadata.Flag;
import kotlinx.metadata.KmClass;
import kotlinx.metadata.jvm.KotlinClassHeader;
import kotlinx.metadata.jvm.KotlinClassMetadata;

public class KotlinClassInfo {
    private KmClass mKotlinClass;
    private Metadata mMetadata;

    private KotlinClassInfo(Metadata metadata) {
        mMetadata = metadata;
        if (null != metadata) {
            final KotlinClassMetadata kotlinClassMetadata = KotlinClassMetadata.read(new KotlinClassHeader(metadata.k(), metadata.mv(), metadata.bv(), metadata.d1(), metadata.d2(), metadata.xs(), metadata.pn(), metadata.xi()));
            if (kotlinClassMetadata instanceof kotlinx.metadata.jvm.KotlinClassMetadata.Class) {
                mKotlinClass = ((KotlinClassMetadata.Class) kotlinClassMetadata).toKmClass();
            }
        }
    }

    public static KotlinClassInfo from(Element element) {
        return new KotlinClassInfo(element.getAnnotation(Metadata.class));
    }

    public boolean isKotlinClass() {
        return null != mMetadata;
    }

    public boolean isObject() {
        return null != mKotlinClass && Flag.Class.IS_OBJECT.invoke(mKotlinClass.getFlags());
    }

    public boolean isCompanionObject() {
        return null != mKotlinClass && Flag.Class.IS_COMPANION_OBJECT.invoke(mKotlinClass.getFlags());
    }

    public boolean isPrivate() {
        return null != mKotlinClass && Flag.IS_PRIVATE.invoke(mKotlinClass.getFlags());
    }

    public String getName() {
        return null != mKotlinClass ? mKotlinClass.getName() : null;
    }

    public boolean isInnerClass() {
        return null != mKotlinClass && Flag.Class.IS_INNER.invoke(mKotlinClass.getFlags());
    }

    public String getCompanionObjectName() {
        return null != mKotlinClass ? mKotlinClass.getCompanionObject() : null;
    }
}
