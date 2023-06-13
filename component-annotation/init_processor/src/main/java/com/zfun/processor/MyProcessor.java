package com.zfun.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.zfun.annotation.InitInAndroid;
import com.zfun.processor.init.InitMgr;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

//所有注解只会生成一个类 - InitMgr$InitInAndroid 类：
//public class InitMgrConfig{
//      public static List<IInit> getAutoInit(List<IInit> autoInitList){
//          autoInitList.add(@InitInAndroid);
//          ...
//          return autoInitList;
//      }
// }
//
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.zfun.annotation.InitInAndroid")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MyProcessor extends AbstractProcessor {
    private Messager messager;
    private Elements elements;
    private boolean isFirst = false;
    private MethodSpec.Builder getAutoInitMethodBuilder;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        elements = processingEnv.getElementUtils();
        printMessage("init - MyProcessor");
        isFirst = true;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (isFirst) {
            isFirst = false;
            printMessage("InitInAndroid == start process ==");
            //method
            getAutoInitMethodBuilder = MethodSpec.methodBuilder("getAutoInit")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(List.class, "autoInitList", Modifier.FINAL)
                    .returns(void.class);
        }

        Set<? extends Element> allElements = roundEnvironment.getElementsAnnotatedWith(InitInAndroid.class);//获取所有采用此注解的类
        for (Element aElement : allElements) {
            final TypeElement typeElement = (TypeElement) aElement;
            final String initClassNameStr = typeElement.getQualifiedName().toString();
            final ClassName initClass = ClassName.bestGuess(initClassNameStr);
            final String paramStr = initClassNameStr.replace('.', '_');

            printMessage("添加初始化类：" + initClassNameStr);
            getAutoInitMethodBuilder.addStatement("final $T $L = new $T()", initClass, paramStr, initClass);//$S替换后会给你添加双引号；$L则是直接替换不会添加任何额外东西。
            getAutoInitMethodBuilder.addStatement("autoInitList.add($L)", paramStr);
        }

        if(roundEnvironment.processingOver()){
            //class
            final TypeSpec typeSpec = TypeSpec.classBuilder("InitMgr$InitInAndroid")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(getAutoInitMethodBuilder.build())
                    .build();

            //java
            final JavaFile javaFile = JavaFile.builder(InitMgr.class.getPackage().getName(), typeSpec).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                printMessage("InitInAndroid == error：" + e.getLocalizedMessage());
            }
            printMessage("InitInAndroid == end process ==");
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(InitInAndroid.class.getCanonicalName());
    }

    private void printMessage(String msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }
}
