package com.zfun.plugin.inject.impl

import com.zfun.plugin.inject.Constants
import com.zfun.plugin.inject.ITransform
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class InjectTransform:ITransform {

    override fun transform(project: Project, inputFile: File, outputFile: File): Boolean {
//        if (!outputFile.exists()){
//            outputFile.createNewFile()
//        }
//        val inputStream = FileInputStream(inputFile)
//
//        val classPool = ClassPool.getDefault()
//        val ctClass = classPool.makeClass(inputStream)
//        if (Constants.Inject_Package_Name == ctClass.packageName){
//            return false
//        }
//
//
//        release(inputStream,ctClass)
        return false
    }

    private fun release(inputStream:InputStream ,ctClass: CtClass){
        ctClass.detach()
        inputStream.close()
    }
}