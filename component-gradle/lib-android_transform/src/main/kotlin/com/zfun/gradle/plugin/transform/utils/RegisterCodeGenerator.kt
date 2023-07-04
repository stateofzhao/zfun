package com.zfun.gradle.plugin.transform.utils

import com.zfun.gradle.plugin.transform.AbsPluginLaunch
import org.objectweb.asm.ClassReader
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
            if (jarEntry.name == AbsPluginLaunch.adapter.injectTargetClassFileName()) {
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
        val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)//ClassWriter.COMPUTE_FRAMES后，ASM会自动计算max stacks、max locals和stack map frames的具体值，可以给visitMaxs()方法传入一个错误的值，不能省略对于visitMaxs()方法的调用
        val classVisitor = AbsPluginLaunch.adapter.createASMClassVisitor(Opcodes.ASM7,classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }
}