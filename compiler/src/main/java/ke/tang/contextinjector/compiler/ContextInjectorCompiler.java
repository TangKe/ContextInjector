package ke.tang.contextinjector.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
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
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import ke.tang.contextinjector.annotations.Constants;
import ke.tang.contextinjector.annotations.InjectContext;
import ke.tang.contextinjector.annotations.Injector;

/**
 * 生成注入器类注解处理器
 * 同时会生成{@link java.util.ServiceLoader}工作需要的配置文件
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("ke.tang.contextinjector.annotations.InjectContext")
public class ContextInjectorCompiler extends AbstractProcessor {
    private final static Set<Modifier> EXCLUDE_MODIFIERS = new HashSet<>();
    private final static String SERVICE_FILE_PATH = "META-INF/services/" + Injector.class.getName();

    static {
        EXCLUDE_MODIFIERS.add(Modifier.PRIVATE);
        EXCLUDE_MODIFIERS.add(Modifier.PROTECTED);
    }

    private FileObject mServiceFilePath;
    private Writer mWriter;

    private ClassName mContextInjectorClassName = ClassName.bestGuess("ke.tang.contextinjector.injector.ContextHooker");

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (null == mServiceFilePath) {
            try {
                mServiceFilePath = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", SERVICE_FILE_PATH);
                mWriter = mServiceFilePath.openWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(InjectContext.class);

        HashMap<TypeElement, HashMap<InjectType, Set<Element>>> extractedElement = new HashMap<>();
        for (Element element : elements) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

            HashMap<InjectType, Set<Element>> value = extractedElement.get(enclosingElement);
            if (null == value) {
                value = new HashMap<>();
                extractedElement.put(enclosingElement, value);
            }

            final Set<Modifier> modifiers = element.getModifiers();

            if (modifiers.contains(EXCLUDE_MODIFIERS)) {
                continue;
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

            if (modifiers.contains(Modifier.STATIC)) {
                Set<Element> staticElements = value.get(InjectType.STATIC);
                if (null == staticElements) {
                    staticElements = new HashSet<>();
                    value.put(InjectType.STATIC, staticElements);
                }
                staticElements.add(element);
            } else {
                Set<Element> instanceElements = value.get(InjectType.INSTANCE);
                if (null == instanceElements) {
                    instanceElements = new HashSet<>();
                    value.put(InjectType.INSTANCE, instanceElements);
                }
                instanceElements.add(element);
            }
        }

        try {
            for (Map.Entry<TypeElement, HashMap<InjectType, Set<Element>>> entry : extractedElement.entrySet()) {
                ClassName className = ClassName.get(entry.getKey());

                final String classSimpleName = Constants.buildInjectorSimpleClassName(className.simpleName());
                JavaFile file = JavaFile.builder(className.packageName(), buildClass(classSimpleName, entry)).build();
                mWriter.write(className.packageName() + "." + classSimpleName);
                mWriter.write("\n");
                file.writeTo(processingEnv.getFiler());
            }

            if (roundEnvironment.processingOver()) {
                mWriter.flush();
                mWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    private TypeSpec buildClass(String className, Map.Entry<TypeElement, HashMap<InjectType, Set<Element>>> entry) {
        final TypeSpec.Builder builder = TypeSpec.classBuilder(className)
                .superclass(ParameterizedTypeName.get(ClassName.get(Injector.class), getRawType(TypeName.get(entry.getKey().asType()))))
                .addModifiers(Modifier.PUBLIC);

        builder.addMethod(buildInjectStaticMethod(new InjectEntry(entry.getKey(), entry.getValue().get(InjectType.STATIC))));
        builder.addMethod(buildInjectInstanceMethod(new InjectEntry(entry.getKey(), entry.getValue().get(InjectType.INSTANCE))));
        return builder.build();
    }

    public TypeName getRawType(TypeName name) {
        if (name instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) name).rawType;
        }
        return name;
    }

    private MethodSpec buildInjectInstanceMethod(InjectEntry entry) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("injectInstance")
                .addParameter(getRawType(TypeName.get(entry.getElement().asType())), "target")
                .addModifiers(Modifier.PROTECTED)
                .returns(void.class)
                .addAnnotation(Override.class);

        if (null != entry.getInnerElement()) {
            for (Element element : entry.getInnerElement()) {
                final ElementKind kind = element.getKind();
                if (ElementKind.METHOD == kind) {
                    builder.addStatement("target.$N($T.getApplicationContext())", element.getSimpleName(), mContextInjectorClassName);
                } else if (ElementKind.FIELD == kind) {
                    builder.addStatement("target.$N = $T.getApplicationContext()", element.getSimpleName(), mContextInjectorClassName);
                }
            }
        }


        return builder.build();
    }

    private MethodSpec buildInjectStaticMethod(InjectEntry entry) {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("injectStatic")
                .addModifiers(Modifier.PROTECTED)
                .returns(void.class)
                .addAnnotation(Override.class);
        if (null != entry.getInnerElement()) {
            for (Element element : entry.getInnerElement()) {
                final ElementKind kind = element.getKind();
                TypeName typeName = getRawType(TypeName.get(entry.getElement().asType()));
                if (ElementKind.METHOD == kind) {
                    builder.addStatement("$T.$N($T.getApplicationContext())", typeName, element.getSimpleName(), mContextInjectorClassName);
                } else if (ElementKind.FIELD == kind) {
                    builder.addStatement("$T.$N = $T.getApplicationContext()", typeName, element.getSimpleName(), mContextInjectorClassName);
                }
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
