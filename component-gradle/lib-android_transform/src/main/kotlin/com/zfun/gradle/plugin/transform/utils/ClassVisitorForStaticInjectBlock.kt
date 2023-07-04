package com.zfun.gradle.plugin.transform.utils

import com.zfun.gradle.plugin.transform.RegisterTransform
import com.zfun.gradle.plugin.transform.type.ScanItem
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 在静态块中插入代码，所以{@link IPluginLaunchAdapter#injectTargetClassName()} 类中要注册代码进去的队列必须是静态的！
 *
 * */
open class ClassVisitorForStaticInjectBlock constructor(api: Int, cv: ClassVisitor,private val methodVisitorOpt:IOpt) : ClassVisitor(api, cv) {
    private var staticBlockExists = false
    private var className: String? = ""

    interface IOpt{
        fun transform(staticBlockMethodVisitor:MethodVisitor,scanItemList:List<ScanItem>,isCreateStaticBlockMethod:Boolean)
    }

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<clinit>") {//已经存在静态代码块了
            staticBlockExists = true
            return object : MethodVisitor(Opcodes.ASM7, methodVisitor) {
                override fun visitInsn(opcode:Int) {
                    // 在静态块的末尾插入代码
                    if (opcode == Opcodes.RETURN){
                        injectInitCodeInStaticBlock(this)
                    }
                    super.visitInsn(opcode)
                }
            }
        }
        return methodVisitor
    }

    override fun visitEnd() {
        if (!staticBlockExists) {
            injectInitCodeInStaticBlock(null)
        }
        super.visitEnd()
    }

    private fun injectInitCodeInStaticBlock(existingStaticBlockMethodVisitor: MethodVisitor?) {
        var isNotEmpty = false
        if (RegisterTransform.scanComponent.isNotEmpty()) {
            for (scanItem in RegisterTransform.scanComponent) {
                if (scanItem.apiProviderImplClassList.isNotEmpty()) {
                    isNotEmpty = true
                    break
                }
            }
        }
        //
        if (isNotEmpty) {
            Logger.i("开始插桩：${className}")
            val needCreateStaticBlockMethod = null == existingStaticBlockMethodVisitor
            val methodVisitor: MethodVisitor = if (needCreateStaticBlockMethod) {
                cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
            } else {
                existingStaticBlockMethodVisitor!!
            }
            if (needCreateStaticBlockMethod) {
                methodVisitor.visitCode()
            }
            //
            methodVisitorOpt.transform(methodVisitor,RegisterTransform.scanComponent,needCreateStaticBlockMethod)
            Logger.i("插桩完成：${className}")
        }
    }

}