package ke.tang.contextinjector.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import ke.tang.contextinjector.annotations.Constants;
import ke.tang.contextinjector.annotations.InjectContext;
import ke.tang.contextinjector.annotations.Injector;
import kotlin.Metadata;

/**
 * 生成注入器类注解处理器
 * 同时会生成{@link java.util.ServiceLoader}工作需要的配置文件
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("ke.tang.contextinjector.annotations.InjectContext")
public class ContextInjectorCompiler extends AbstractProcessor {
    private ClassName mContextInjectorClassName = ClassName.bestGuess("ke.tang.contextinjector.injector.ContextHooker");

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(InjectContext.class);

        HashMap<TypeElement, InjectEntry> extractedElement = new HashMap<>();

        for (Element element : elements) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            InjectEntry value = extractedElement.get(enclosingElement);

            if (null == value) {
                value = new InjectEntry(enclosingElement);
                extractedElement.put(enclosingElement, value);
            }

            final Set<Modifier> targetElementModifiers = element.getModifiers();

            if (isPrivate(element)) {
                System.out.println(element.getSimpleName() + "要注入的变量或方法不能为私有的且所属类也不能为私有");
                throw new IllegalStateException("要注入的变量或方法不能为私有的且所属类也不能为私有");
            }

            final ElementKind kind = element.getKind();
            if (ElementKind.FIELD == kind && !isSubtypeOfType(element.asType(), "android.content.Context")) {
                continue;
            }

            if (ElementKind.METHOD == kind) {
                final List<? extends VariableElement> parameters = ((ExecutableElement) element).getParameters();
                if (parameters.size() != 1 || !isSubtypeOfType(parameters.get(0).asType(), "android.content.Context")) {
                    //排除掉参数不是一个且参数类型不是Context的
                    continue;
                }
            }

            final KotlinClassInfo enclosingElementKotlinClassInfo = KotlinClassInfo.from(enclosingElement);
            if (targetElementModifiers.contains(Modifier.STATIC) || enclosingElementKotlinClassInfo.isObject() || enclosingElementKotlinClassInfo.isCompanionObject()) {
                value.getInjectElements(InjectType.STATIC).add(element);
            } else {
                value.getInjectElements(InjectType.INSTANCE).add(element);
            }
        }

        try {
            for (Map.Entry<TypeElement, InjectEntry> entry : extractedElement.entrySet()) {
                ClassName className = ClassName.get(entry.getKey());
                final String packageName = className.packageName();
                final String classSimpleName = Constants.buildInjectorSimpleClassName(className.reflectionName().replace(packageName.isEmpty() ? packageName : packageName.concat("."), ""));
                JavaFile file = JavaFile.builder(packageName, buildClass(classSimpleName, entry)).build();
                file.writeTo(processingEnv.getFiler());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    private TypeSpec buildClass(String className, Map.Entry<TypeElement, InjectEntry> entry) {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .superclass(ParameterizedTypeName.get(ClassName.get(Injector.class), getRawType(TypeName.get(entry.getKey().asType()))))
                .addAnnotation(AnnotationSpec.builder(AutoService.class).addMember("value", CodeBlock.builder().add("$T.class", Injector.class).build()).build())
                .addModifiers(Modifier.PUBLIC);

        builder.addMethod(buildInjectStaticMethod(entry.getValue()));
        builder.addMethod(buildInjectInstanceMethod(entry.getValue()));
        return builder.build();
    }

    private boolean isPrivate(Element element) {
        if (null != element) {
            final KotlinClassInfo kotlinClassInfo = KotlinClassInfo.from(element);
            if (kotlinClassInfo.isKotlinClass()) {
                return kotlinClassInfo.isPrivate() || isPrivate(element.getEnclosingElement());
            } else {
                return element.getModifiers().contains(Modifier.PRIVATE) || isPrivate(element.getEnclosingElement());
            }
        } else {
            return false;
        }
    }

    public TypeName getRawType(TypeName name) {
        if (name instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) name).rawType;
        }
        return name;
    }

    private MethodSpec buildInjectInstanceMethod(InjectEntry entry) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("injectInstance")
                .addParameter(getRawType(TypeName.get(entry.getEnclosingElement().asType())), "target")
                .addModifiers(Modifier.PROTECTED)
                .returns(void.class)
                .addAnnotation(Override.class);

        for (Element element : entry.getInjectElements(InjectType.INSTANCE)) {
            final ElementKind kind = element.getKind();
            if (ElementKind.METHOD == kind) {
                builder.addStatement("target.$N($T.getApplicationContext())", element.getSimpleName(), mContextInjectorClassName);
            } else if (ElementKind.FIELD == kind) {
                builder.addStatement("target.$N = $T.getApplicationContext()", element.getSimpleName(), mContextInjectorClassName);
            }
        }

        return builder.build();
    }

    private MethodSpec buildInjectStaticMethod(InjectEntry entry) {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("injectStatic")
                .addModifiers(Modifier.PROTECTED)
                .returns(void.class)
                .addAnnotation(Override.class);
        for (Element element : entry.getInjectElements(InjectType.STATIC)) {
            final KotlinClassInfo enclosingKotlinClassInfo = KotlinClassInfo.from(entry.getEnclosingElement());
            final ElementKind kind = element.getKind();
            TypeName typeName = getRawType(TypeName.get(entry.getEnclosingElement().asType()));
            if (ElementKind.METHOD == kind) {
                if (enclosingKotlinClassInfo.isObject()) {
                    builder.addStatement("$T.INSTANCE.$N($T.getApplicationContext())", typeName, element.getSimpleName(), mContextInjectorClassName);
                } else if (enclosingKotlinClassInfo.isCompanionObject()) {
                    builder.addStatement("$T.$N($T.getApplicationContext())", typeName, element.getSimpleName(), mContextInjectorClassName);
                } else {
                    builder.addStatement("$T.$N($T.getApplicationContext())", typeName, element.getSimpleName(), mContextInjectorClassName);
                }
            } else if (ElementKind.FIELD == kind) {
//                if (enclosingKotlinClassInfo.isObject()) {
//                    builder.addStatement("$T.INSTANCE.$N = $T.getApplicationContext()", typeName, element.getSimpleName(), mContextInjectorClassName);
//                } else if (enclosingKotlinClassInfo.isCompanionObject()) {
//                    builder.addStatement("$T.$N.$N = $T.getApplicationContext()", typeName, targetClassInfo.getCompanionObjectName(), element.getSimpleName(), mContextInjectorClassName);
//                } else {
                builder.addStatement("$T.$N = $T.getApplicationContext()", typeName, element.getSimpleName(), mContextInjectorClassName);
//                }
            }
        }
        return builder.build();
    }

    static boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (isTypeEqual(typeMirror, otherType)) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
        return otherType.equals(typeMirror.toString());
    }
}
