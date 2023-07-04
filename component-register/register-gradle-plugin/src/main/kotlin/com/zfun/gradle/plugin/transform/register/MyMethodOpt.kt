package com.zfun.gradle.plugin.transform.register

import com.zfun.gradle.plugin.transform.type.ScanItem
import com.zfun.gradle.plugin.transform.utils.ClassVisitorForStaticInjectBlock
import com.zfun.gradle.plugin.transform.utils.Logger
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type


class MyMethodOpt :ClassVisitorForStaticInjectBlock.IOpt{

    // Constants.INJECT_CODE_CLASS_NAME 静态块中插入的代码：
    //        className impl = new className();
    //        sRegisterList.add(impl);
    //
    //        final Class<?> key= impl.key();
    //        final Class<?> value = impl.value();
    //        if (Object.class != key){
    //            sRegisterMap.put(key,value);
    //        } else {
    //            String fallbackKey = impl.fallbackKey();
    //            if (null == fallbackKey){
    //                fallbackKey = value.getName();
    //            }
    //            sRegisterMap.put(fallbackKey,value);
    //        }
    override fun transform(staticBlockMethodVisitor: MethodVisitor, scanItemList: List<ScanItem>,isCreateStaticBlockMethod:Boolean) {
        staticBlockMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        staticBlockMethodVisitor.visitLdcInsn("RegisterCenter-static{} run for insert register--")
        staticBlockMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
        //init map
        /*staticBlockMethodVisitor.visitTypeInsn(Opcodes.NEW, "java/util/HashMap")
        staticBlockMethodVisitor.visitInsn(Opcodes.DUP)
        staticBlockMethodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false)
        staticBlockMethodVisitor.visitFieldInsn(Opcodes.PUTSTATIC, Constants.INJECT_CODE_CLASS_NAME, Constants.INJECT_CODE_CLASS_FIELD_MAP_NAME, "Ljava/util/HashMap;")*/
        //init llist
        /*staticBlockMethodVisitor.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList")
        staticBlockMethodVisitor.visitInsn(Opcodes.DUP)
        staticBlockMethodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false)
        staticBlockMethodVisitor.visitFieldInsn(Opcodes.PUTSTATIC, Constants.INJECT_CODE_CLASS_NAME, Constants.INJECT_CODE_CLASS_FIELD_LIST_NAME, "Ljava/util/List;")*/
        var index = 1
        scanItemList.forEach { scanItem ->
            if (scanItem.apiProviderClassSimpleName == Constants.REGISTER_COMPONENT_REGISTER_PROVIDER) {//处理 IRegisterProvider 的注册
                scanItem.apiProviderImplClassList.forEach { className ->
                    /*val javaClassName = className.replace("/", ".")*/
                    Logger.i("开始插桩##${Constants.REGISTER_COMPONENT_REGISTER_PROVIDER}实现类实例：${className}")
                    staticBlockMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, Constants.INJECT_CODE_CLASS_NAME, Constants.INJECT_CODE_CLASS_FIELD_LIST_NAME, "Ljava/util/List;")
                    staticBlockMethodVisitor.visitTypeInsn(Opcodes.NEW, className)
                    staticBlockMethodVisitor.visitInsn(Opcodes.DUP)
                    staticBlockMethodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", "()V", false)
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ASTORE, 0)
                    staticBlockMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, Constants.INJECT_CODE_CLASS_NAME, Constants.INJECT_CODE_CLASS_FIELD_LIST_NAME, "Ljava/util/List;");
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                    staticBlockMethodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
                    staticBlockMethodVisitor.visitInsn(Opcodes.POP)
                    //
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                    staticBlockMethodVisitor.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        className,
                        "key",
                        "()Ljava/lang/Class;",
                        false
                    )
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ASTORE, 1)
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                    staticBlockMethodVisitor.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        className,
                        "value",
                        "()Ljava/lang/Class;",
                        false
                    )
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ASTORE, 2)
                    staticBlockMethodVisitor.visitLdcInsn(Type.getType("Ljava/lang/Object;"))
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
                    val label0 = Label()
                    staticBlockMethodVisitor.visitJumpInsn(Opcodes.IF_ACMPEQ, label0)
                    staticBlockMethodVisitor.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        Constants.INJECT_CODE_CLASS_NAME,
                        Constants.INJECT_CODE_CLASS_FIELD_MAP_NAME,
                        "Ljava/util/Map;"
                    )
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 1)
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
                    staticBlockMethodVisitor.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        "java/util/Map",
                        "put",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                        true
                    )
                    staticBlockMethodVisitor.visitInsn(Opcodes.POP)
                    val label1 = Label()
                    staticBlockMethodVisitor.visitJumpInsn(Opcodes.GOTO, label1)
                    staticBlockMethodVisitor.visitLabel(label0)
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 0)
                    staticBlockMethodVisitor.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        className,
                        "fallbackKey",
                        "()Ljava/lang/String;",
                        false
                    )
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ASTORE, 3)
                    staticBlockMethodVisitor.visitInsn(Opcodes.ACONST_NULL)
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 3)
                    val label2 = Label()
                    staticBlockMethodVisitor.visitJumpInsn(Opcodes.IF_ACMPNE, label2)
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
                    staticBlockMethodVisitor.visitMethodInsn(
                        Opcodes.INVOKEVIRTUAL,
                        "java/lang/Class",
                        "getName",
                        "()Ljava/lang/String;",
                        false
                    )
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ASTORE, 3)
                    staticBlockMethodVisitor.visitLabel(label2)
                    staticBlockMethodVisitor.visitFieldInsn(
                        Opcodes.GETSTATIC,
                        Constants.INJECT_CODE_CLASS_NAME,
                        Constants.INJECT_CODE_CLASS_FIELD_MAP_NAME,
                        "Ljava/util/Map;"
                    )
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 3)
                    staticBlockMethodVisitor.visitVarInsn(Opcodes.ALOAD, 2)
                    staticBlockMethodVisitor.visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        "java/util/Map",
                        "put",
                        "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                        true
                    )
                    staticBlockMethodVisitor.visitInsn(Opcodes.POP)
                    staticBlockMethodVisitor.visitLabel(label1)
                    index += 3
                }
            }
        }
        staticBlockMethodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
        staticBlockMethodVisitor.visitLdcInsn("RegisterCenter-static{} run insert register finish --")
        staticBlockMethodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
        if (isCreateStaticBlockMethod){
            staticBlockMethodVisitor.visitInsn(Opcodes.RETURN)
            staticBlockMethodVisitor.visitMaxs(2, index)
            staticBlockMethodVisitor.visitEnd()
        } else {
            //更新下 visitMaxs
        }
    }
}