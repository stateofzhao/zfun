package com.zfun.gradle.plugin.transform.init.utils

import com.zfun.gradle.plugin.transform.init.Constants
import com.zfun.gradle.plugin.transform.init.RegisterTransform
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile

object TransformProcessUtil {

    fun shouldProcessPreDexJar(path: String): Boolean {
        return !path.contains("com.android.support") && !path.contains("/android/m2repository")
    }

    fun shouldProcessClassFile(path: String): Boolean {
        return path.startsWith(Constants.ANNOTATION_PROCESSOR_GENERATE_PACKAGE_NAME)
    }

    fun scanJar(srcFile: File, desFile: File) {
        Logger.i("scan jar：${srcFile.absolutePath}")
        if (srcFile.exists()) {
            val srcJarFile = JarFile(srcFile)
            val enumeration = srcJarFile.entries()
            while (enumeration.hasMoreElements()) {
                val jarEntity = enumeration.nextElement() as JarEntry
                val entryName = jarEntity.name
                if (entryName.startsWith(Constants.ANNOTATION_PROCESSOR_GENERATE_PACKAGE_NAME)) {
                    //注解处理器生成的class
                    val inputStream = srcJarFile.getInputStream(jarEntity)
                    scanClass(inputStream)
                } else if (entryName == Constants.INJECT_CODE_CLASS_FILE_NAME) {
                    Logger.i("scan jar##命中要进行字节码插入的类所在jar：${srcFile.absolutePath}")
                    RegisterTransform.fileForInjectCode = desFile
                }
            }
            srcJarFile.close()
        }
    }

    fun scanClass(srcFile: File) {
        val inputString = srcFile.inputStream()
        scanClass(inputString)
    }

    private fun scanClass(inputString: InputStream) {
        val classReader = ClassReader(inputString)
        val classWriter = ClassWriter(classReader, 0)
        val classVisitor = ScanClassVisitor(Opcodes.ASM7, classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        inputString.close()
    }
}

class ScanClassVisitor(api: Int, classVisitor: ClassVisitor) : ClassVisitor(api, classVisitor) {

    /**
     * @param version jdk版本
     * @param access 类的访问权限，public,private 等
     * @param name 类名
     * @param signature 表示类的签名，如果类不是泛型或者没有继承泛型类，那么signature 值为空
     * @param superName 表示父类的名称
     * @param interfaces 类实现的接口
     * */
    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        super.visit(version, access, name, signature, superName, interfaces)
        Logger.i("scan##检测类：${name}")
        interfaces ?: return
        RegisterTransform.scanComponent.forEach { scanItem ->
            interfaces.forEach { aInterface ->
                Logger.i("scan##检测类##实现的接口：${aInterface}")
                if (aInterface == scanItem.getInterfaceName()) {
                    Logger.i("scan##命中注解解析器生成的类：${name}")
                    if (!scanItem.classList.contains(name)) {
                        scanItem.classList.add(name!!)
                    }
                }
            }
        }
    }
}