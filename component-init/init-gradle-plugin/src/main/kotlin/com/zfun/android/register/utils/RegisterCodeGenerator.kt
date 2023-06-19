package com.zfun.android.register.utils

import com.zfun.android.register.Constants
import com.zfun.android.register.RegisterTransform
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

//
object RegisterCodeGenerator {

    fun insertInitCode2JarFile(jarFile: File) {
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
    var className: String? = ""

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
    }

    override fun visitEnd() {
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
            val methodVisitor = cv.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
            methodVisitor.visitCode()
            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            methodVisitor.visitLdcInsn("InitMgr-static{} run start --")
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
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
                }
            }
            methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            methodVisitor.visitLdcInsn("InitMgr-static{} run finish --")
            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
            methodVisitor.visitInsn(Opcodes.RETURN)
            methodVisitor.visitMaxs(2, index)
            methodVisitor.visitEnd()
        }
        super.visitEnd()
        Logger.i("插桩完成：${className}")
    }
}