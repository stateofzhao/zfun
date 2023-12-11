package com.zfun.gradle.plugin.transform.init.utils

import com.zfun.gradle.plugin.transform.init.Constants
import com.zfun.gradle.plugin.transform.init.RegisterTransform
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.MethodVisitor
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

//
object RegisterCodeGenerator {

    fun insertInitCode2JarFile(jarFile: File) {
        if(null == RegisterTransform.fileForInjectCode){
            Logger.i("没有要注入代码的类，跳过字节码修改～")
            return
        }
        if (RegisterTransform.scanComponent.isEmpty()){
            Logger.i("没有注解生成的类，跳过字节码修改～～")
            return
        }

        val optJar = File(jarFile.parent, jarFile.name + ".opt")
        if (optJar.exists()) {
            FileUtils.forceDelete(optJar)
        }
        val file = JarFile(jarFile)
        val enumeration = file.entries()
        val jarOutputStream = JarOutputStream(FileOutputStream(optJar))
        while (enumeration.hasMoreElements()) {
            val jarEntry = enumeration.nextElement()
            val zipEntry = ZipEntry(jarEntry.name)
            val inputStream = file.getInputStream(jarEntry)
            jarOutputStream.putNextEntry(zipEntry)
            if (jarEntry.name == Constants.INJECT_CODE_CLASS_FILE_NAME) {
                //修改字节码
                val byteArray = process(inputStream)
                jarOutputStream.write(byteArray)
            } else {
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            inputStream.close()
            jarOutputStream.closeEntry()
        }
        jarOutputStream.close()
        file.close()
        //用修改后的jar替换原jar文件
        FileUtils.forceDelete(jarFile)
        val ok = optJar.renameTo(jarFile)
        if (ok) {
            Logger.i("jar修改完毕：${jarFile.absolutePath}")
        } else {
            Logger.i("jar修改完毕后替换失败！：${jarFile.absolutePath}")
        }
    }

    private fun process(inputStream: InputStream): ByteArray {
        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val classVisitor = RegisterClassVisitor(Opcodes.ASM7, classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }
}

class RegisterClassVisitor constructor(api: Int, cv: ClassVisitor) : ClassVisitor(api, cv) {
    private var staticBlockExists = false
    var className: String? = ""

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
                if (scanItem.classList.isNotEmpty()) {
                    isNotEmpty = true
                    break
                }
            }
        }
        //
        if (isNotEmpty) {
            Logger.i("开始插桩：${className}")
            val needCreateStaticBlockMethod = null == existingStaticBlockMethodVisitor
            val methodVisitor:MethodVisitor = if (needCreateStaticBlockMethod) {
                cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
            } else {
                existingStaticBlockMethodVisitor!!
            }
            //
            if (needCreateStaticBlockMethod) {
                methodVisitor.visitCode()
            }
            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            methodVisitor.visitLdcInsn("InitMgr-static{} run init start --")
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
            methodVisitor.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList")
            methodVisitor.visitInsn(Opcodes.DUP)
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false)
            methodVisitor.visitFieldInsn(Opcodes.PUTSTATIC, Constants.INJECT_CODE_CLASS_NAME, "iInitList", "Ljava/util/List;")
            var index = 0
            RegisterTransform.scanComponent.forEach { scanItem ->
                if (scanItem.componentName == Constants.INIT_COMPONENT_NAME) {//处理 IInit 的注册
                    scanItem.classList.forEach { className ->
                        /*val javaClassName = className.replace("/", ".")*/
                        Logger.i("开始插桩##newInit实例：${className}")
                        methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, Constants.INJECT_CODE_CLASS_NAME, "iInitList", "Ljava/util/List;")
                        methodVisitor.visitTypeInsn(Opcodes.NEW, className)
                        methodVisitor.visitInsn(Opcodes.DUP)
                        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, className, "<init>", "()V", false)
                        methodVisitor.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true)
                        methodVisitor.visitInsn(Opcodes.POP)
                        index += 1
                    }
                    scanItem.markProcessed = true
                }
            }
            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            methodVisitor.visitLdcInsn("InitMgr-static{} run init finish --")
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
            if (needCreateStaticBlockMethod){
                methodVisitor.visitInsn(Opcodes.RETURN)
                methodVisitor.visitMaxs(2, index)
                methodVisitor.visitEnd()
            } else {
                //更新下 visitMaxs

            }
            Logger.i("插桩完成：${className}")
        }
    }

}