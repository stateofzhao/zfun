package com.zfun.register.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.zfun.annotation.register.AutoRegister;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import jdk.javadoc.internal.doclets.toolkit.builders.MethodBuilder;

//生成的类：com.zfun.register.registers.Zfun__Register__${REGISTER_MODULE_NAME}__${被注解的类name.replaceAll('.','_')}
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.zfun.annotation.register.AutoRegister")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class RegisterCompiler extends AbstractProcessor {
    private final String REGISTER_API_PROVIDER_NAME = "com.zfun.register.IRegisterProvider";
    //
    private final String KEY_MODULE_NAME = "REGISTER_MODULE_NAME";
    private final String SEPARATOR = "__";
    private final String CLASS_NAME_ROOT = "Zfun" + SEPARATOR + "Register";
    private final String PACKAGE_OF_GENERATE_FILE = "com.zfun.register.registers";
    private final String NO_MODULE_NAME_TIPS = "These no module name, at 'build.gradle', like :\n" +
            "android {\n" +
            "    defaultConfig {\n" +
            "        ...\n" +
            "        javaCompileOptions {\n" +
            "            annotationProcessorOptions {\n" +
            "                arguments = [REGISTER_MODULE_NAME: project.getName()]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

    private final String REGISTER_PROVIDER_METHOD_KEY = "key";
    private final String REGISTER_PROVIDER_METHOD_FALLBACKKEY = "fallbackKey";
    private final String REGISTER_PROVIDER_METHOD_VALUE = "value";

    private Messager messager = null;
    private String moduleName = null;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        Map<String, String> options = processingEnv.getOptions();
        if (null != options) {
            moduleName = options.get(KEY_MODULE_NAME);
        }
        if (null != moduleName && moduleName.length() > 0) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
            printMessage("The user has configuration the module name, it was [" + moduleName + "]");
        } else {
            printError(NO_MODULE_NAME_TIPS);
            throw new RuntimeException("No module name, for more information, look at gradle log.");
        }
        printMessage("init");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            printMessage("AutoRegister Compiler == start process ==");

            if (set.isEmpty()) {
                return false;
            }
            Set<? extends Element> allElements = roundEnvironment.getElementsAnnotatedWith(AutoRegister.class);//获取所有采用此注解的类
            for (Element aElement : allElements) {
                if (aElement.getKind() == ElementKind.CLASS) {
                    final AutoRegister autoRegister = aElement.getAnnotation(AutoRegister.class);
                    if (null != autoRegister) {
                        final TypeElement typeElement = (TypeElement) aElement;
                        final String elementClassName = typeElement.getQualifiedName().toString(); //被注解的类全称
                        printMessage("begin process class:${elementClassName}");
                        //
                        final TypeMirror keyTypeMirror = getKey(autoRegister);
                        final TypeElement keyTypeElement = asTypeElement(keyTypeMirror);
                        final ClassName keyClassName = ClassName.get(keyTypeElement);
                        String fallbackKey = autoRegister.fallbackKey();
                        if (fallbackKey.isEmpty()) {
                            fallbackKey = elementClassName;
                        }
                        final ClassName valueClassName = ClassName.bestGuess(elementClassName);
                        final MethodSpec.Builder keyMethodBuilder = MethodSpec.methodBuilder(REGISTER_PROVIDER_METHOD_KEY)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Override.class)
                                .addStatement("return $T.class", keyClassName)
                                .returns(Class.class);
                        final MethodSpec.Builder fallbackKeyMethodBuilder = MethodSpec.methodBuilder(REGISTER_PROVIDER_METHOD_FALLBACKKEY)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Override.class)
                                .addStatement("return $S", fallbackKey)
                                .returns(String.class);
                        final MethodSpec.Builder valueMethodBuilder = MethodSpec.methodBuilder(REGISTER_PROVIDER_METHOD_VALUE)
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Override.class)
                                .addStatement("return $T.class", valueClassName)
                                .returns(Class.class);
                        //class
                        final String simpleClassName = CLASS_NAME_ROOT + SEPARATOR + moduleName + SEPARATOR + elementClassName.replaceAll("\\.", "_");
                        final TypeSpec classSpc = TypeSpec.classBuilder(simpleClassName)
                                .addModifiers(Modifier.PUBLIC)
                                .addMethod(keyMethodBuilder.build())
                                .addMethod(fallbackKeyMethodBuilder.build())
                                .addMethod(valueMethodBuilder.build())
                                .addSuperinterface(ClassName.bestGuess(REGISTER_API_PROVIDER_NAME))
                                .build();
                        //java file
                        final JavaFile javaFile =
                                JavaFile.builder(PACKAGE_OF_GENERATE_FILE, classSpc).build();
                        try {
                            javaFile.writeTo(processingEnv.getFiler());
                        } catch (IOException e) {
                            printError("error：" + e.getLocalizedMessage());
                        }
                        printMessage("生成初AutoRegister中间类：$PACKAGE_OF_GENERATE_FILE.$simpleClassName");
                    }
                }
            }
            return true;
        } finally {
            printMessage("AutoRegister Compiler == end process ==");
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AutoRegister.class.getCanonicalName());
    }

    //自定义参数key
    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(KEY_MODULE_NAME);
        }};
    }


    private void printMessage(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Register::Compiler >>>" + msg);
    }

    private void printError(String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, "Register::Compiler >>>" + msg);
    }

    private static TypeMirror getKey(AutoRegister annotation) {
        try {
            annotation.key(); // this should throw
        } catch (MirroredTypeException mte) {
            return mte.getTypeMirror();
        }
        return null; // can this ever happen??
    }

    private TypeElement asTypeElement(TypeMirror typeMirror) {
        Types TypeUtils = this.processingEnv.getTypeUtils();
        return (TypeElement)TypeUtils.asElement(typeMirror);
    }
}