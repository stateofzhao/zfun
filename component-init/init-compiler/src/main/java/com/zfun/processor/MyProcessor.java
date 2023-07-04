package com.zfun.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.zfun.annatation.init.AutoInit;

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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

//同一个注解类型在多个地方被使用（例如多个类或多个方法上），不会在每次使用该注解时就触发一次，只会回调一次！。
//注解处理器虽然会被多次回调（javac 自身机制，并不是多次使用同一个注解时触发的，例如，在注解处理器中生成的类又使用了注解），
// 但是 process() 方法中获取的 roundEnvironment.getElementsAnnotatedWith(InitInAndroid.class) size==0
// （在 process() 方法中return了true，如果return false的话，则不为0）。
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.zfun.annotation.init.AutoInit")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyProcessor extends AbstractProcessor {
    private static final String INIT_PROVIDER = "com.zfun.initapi.internal.IInitProvider";
    //
    private static final String SEPARATOR = "__";
    private static final String PACKAGE_OF_GENERATE_FILE = "com.zfun.init.inits";
    private static final String CLASS_NAME_ROOT = "Zfun" + SEPARATOR + "Init";
    private static final String KEY_MODULE_NAME = "INIT_MODULE_NAME";
    public static final String NO_MODULE_NAME_TIPS = "These no module name, at 'build.gradle', like :\n" +
            "android {\n" +
            "    defaultConfig {\n" +
            "        ...\n" +
            "        javaCompileOptions {\n" +
            "            annotationProcessorOptions {\n" +
            "                arguments = [INIT_MODULE_NAME: project.getName()]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private Messager messager;
    private Elements elementUtils;
    private String moduleName = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();

        final Map<String, String> options = processingEnv.getOptions();
        if (null != options) {
            moduleName = processingEnv.getOptions().get(KEY_MODULE_NAME);
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

    //return true表示此注解无需后续其它注解处理器来处理了
    //生成的类：com.zfun.init.inits.Zfun__Init__moduleName__[initClass.name.replaceAll('.','_')]
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set.isEmpty()) {
            return false;
        }
        printMessage("AutoInit Compiler == start process ==");
        parseInitAnnotations(set, roundEnvironment);
        printMessage("AutoInit Compiler == end process ==");
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AutoInit.class.getCanonicalName());
    }

    //自定义参数key
    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(KEY_MODULE_NAME);
        }};
    }

    private void printMessage(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, "Init::Compiler >>>" + msg);
    }

    private void printError(String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, "Init::Compiler >>>" + msg);
    }

    private void parseInitAnnotations(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> allElements = roundEnvironment.getElementsAnnotatedWith(AutoInit.class);//获取所有采用此注解的类
        if (!allElements.isEmpty()) {
            final TypeElement type_IInitProvider = elementUtils.getTypeElement(INIT_PROVIDER);

            for (Element aElement : allElements) {
                if (aElement.getKind() != ElementKind.CLASS) {
                    continue;
                }
                final TypeMirror typeMirror = aElement.asType();//获取被注解的类的类型
                final TypeElement typeElement = (TypeElement) aElement;//获取被注解的类的信息
                final String initClassNameStr = typeElement.getQualifiedName().toString();
                final ClassName initClass = ClassName.bestGuess(initClassNameStr);
                final AutoInit init = aElement.getAnnotation(AutoInit.class);
                printMessage("检测到初始化类：" + initClassNameStr);
                //method
                //字符串：$S替换后会给你添加双引号；$L则是直接替换不会添加任何额外东西；
                //$T type；
                MethodSpec.Builder getMethodBuilder = MethodSpec.methodBuilder("get")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addStatement("return new $T()", initClass)
                        .returns(Object.class);
                MethodSpec.Builder nameMethodBuilder = MethodSpec.methodBuilder("name")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addStatement("return $S", init.name())
                        .returns(String.class);

                final String initDepStr = init.dependsOn();
                final String[] initDepArray;
                if (initDepStr.length()>0){
                    initDepArray = initDepStr.split(",");
                } else {
                    initDepArray = new String[0];
                }
                final CodeBlock depArrayCode = buildArrayInitCode(initDepArray);
                MethodSpec.Builder dependsOnMethodBuilder = MethodSpec.methodBuilder("dependsOn")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addCode(depArrayCode)
                        .returns(String[].class);
                //class
                final String className = CLASS_NAME_ROOT + SEPARATOR + moduleName + SEPARATOR + initClassNameStr.replaceAll("\\.","_");
                final TypeSpec typeSpec = TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(getMethodBuilder.build())
                        .addMethod(nameMethodBuilder.build())
                        .addMethod(dependsOnMethodBuilder.build())
                        .addSuperinterface(ClassName.get(type_IInitProvider))
                        .build();
                //java
                final JavaFile javaFile = JavaFile.builder(PACKAGE_OF_GENERATE_FILE, typeSpec).build();
                try {
                    javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    printError("error：" + e.getLocalizedMessage());
                }
                printMessage("生成初始化类：" + PACKAGE_OF_GENERATE_FILE + "." + className);
            }
        }
    }

    private static CodeBlock buildArrayInitCode(String[] array) {
        final CodeBlock.Builder codeBuilder;
        if (array.length>0){
            codeBuilder = CodeBlock.builder()
                    .add("String[] strArray = new String[] {\n");

            for (int i = 0; i < array.length; i++) {
                codeBuilder.add("$S", array[i]);

                if (i < array.length - 1) {
                    codeBuilder.add(",\n");
                }
            }

            codeBuilder.add("\n};\n");
            codeBuilder.addStatement("return strArray");
        } else {
            ClassName stringClass = ClassName.get(String.class);
            codeBuilder = CodeBlock.builder()
                    .add("$T[] strArray = new $T[0];\n",stringClass,stringClass)
                    .addStatement("return strArray");
        }
        return codeBuilder.build();
    }
}
