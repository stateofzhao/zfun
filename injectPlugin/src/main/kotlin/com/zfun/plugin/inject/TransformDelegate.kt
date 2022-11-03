package com.zfun.plugin.inject

import com.android.SdkConstants
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import com.google.common.io.Files
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class TransformDelegate(private val project: Project, private val transform: ITransform) : Transform() {

    override fun getName(): String {
        return transform.name()
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        val inputType = transform.getInputTypes()
        if (Constants.InputType_Class == inputType) {
            return TransformManager.CONTENT_CLASS
        }
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        val scope = transform.getScopes()
        if (Constants.Scope_Project == scope) {
            return TransformManager.SCOPE_FULL_PROJECT
        }
        return TransformManager.EMPTY_SCOPES
    }

    override fun isIncremental(): Boolean {
        return transform.isIncremental()
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        transformInvocation ?: return
        log("inject transform ============== 【start】")
        val context = transformInvocation.context
        val inputProvider = transformInvocation.inputs //是传过来的输入流
        val referencedInputs = transformInvocation.referencedInputs//
        val outputProvider = transformInvocation.outputProvider//获取输出目录，将修改的文件复制到输出目录，必须执行
        val isIncremental = transformInvocation.isIncremental//是否为增量编译
        log("isIncremental = $isIncremental")

        if (!isIncremental) {//非增量编译，删除之前所有编译缓存
            log("All File delete")
            outputProvider.deleteAll()
        }

        if (transform.isNeedScanAllFile()){
            log("scan all files ======= [start]")
            for(aInput in inputProvider){

            }

            log("scan all files ======= [end]")
        }

        // TransformInput有两种类型：
        //DirectoryInput：以源码方式参与构建的输入文件，包括完整的源码目录结构及其中的源码文件
        //JarInput：以 Jar 和 aar 依赖方式参与构建的输入文件，包含本地依赖和远程依赖
        //========================================DirectoryInput
        for (aInput in inputProvider) {
            log("Transform 【DirectInput】 start")
            val directInput = aInput.directoryInputs
            for (aDirectInput in directInput) {//遍历处理接收到的所有源文件
                val inputDir = aDirectInput.file //接收到的文件，是一个目录
                val changeFiles = aDirectInput.changedFiles//接收到的所有变更的文件（文件集合，单个元素是文件）
                val outputDir = outputProvider.getContentLocation(aDirectInput.name, aDirectInput.contentTypes, aDirectInput.scopes, Format.DIRECTORY)//输出到的文件目录
                log("Transform source direct : ${inputDir.name} -- start")
                if (isIncremental) {
                    val keys = changeFiles.keys.iterator()
                    while (keys.hasNext()) {
                        val inputFile = keys.next()
                        val desFile = getOutputFilePath(outputDir, inputFile)
                        when (changeFiles[inputFile]) {
                            Status.NOTCHANGED -> {
                                // nothing
                            }
                            Status.ADDED, Status.CHANGED -> {
                                prepareTransformClass(inputFile, desFile)
                            }
                            Status.REMOVED -> {
                                FileUtils.delete(desFile)
                            }
                        }
                    }
                } else {
                    for (inputFile in FileUtils.getAllFiles(inputDir)) {
                        prepareTransformClass(inputFile, getOutputFilePath(outputDir, inputFile))
                    }
                }
                log("Transform source direct : ${inputDir.name} -- end")
            }
            log("Transform 【DirectInput】 end")

            //========================================JarInput
            val jarInput = aInput.jarInputs
            log("Transform 【JarInput】 start ==== ")
            for (aJarInput in jarInput) {
                val inputJarFile = aJarInput.file //jar文件
                log("Transform Jar : ${inputJarFile.name} -- start")
                val outputJarFile = outputProvider.getContentLocation(aJarInput.name, aJarInput.contentTypes, aJarInput.scopes, Format.JAR)
                if (isIncremental) {
                    when (aJarInput.status ?: Status.NOTCHANGED) {
                        Status.NOTCHANGED -> {
                            // nothing
                        }
                        Status.ADDED, Status.CHANGED -> {
                            prepareTransformJar(inputJarFile, outputJarFile)
                        }
                        Status.REMOVED -> {
                            FileUtils.delete(outputJarFile)
                        }
                    }
                } else {
                    prepareTransformJar(inputJarFile, outputJarFile)
                }
                log("Transform Jar : ${inputJarFile.name} -- end")
            }
            log("Transform 【JarInput】 end ==== ")
        }

        log("inject transform ============== 【end】")
    }

    //获取输出文件的全名
    private fun getOutputFilePath(outputDir: File, inputFile: File) = File(outputDir, inputFile.name)

    private fun traverseFile(inputProvider:Collection<TransformInput>,opt:(aInputFile:File,fileStatus:String)->Void){
//        for (aInput in inputProvider) {
//            log("Transform 【DirectInput】 start")
//            val directInput = aInput.directoryInputs
//            for (aDirectInput in directInput) {//遍历处理接收到的所有源文件
//                val inputDir = aDirectInput.file //接收到的文件，是一个目录
//                val changeFiles = aDirectInput.changedFiles//接收到的所有变更的文件（文件集合，单个元素是文件）
//                log("Transform source direct : ${inputDir.name} -- start")
//                if (isIncremental) {
//                    val keys = changeFiles.keys.iterator()
//                    while (keys.hasNext()) {
//                        val inputFile = keys.next()
//                        when (changeFiles[inputFile]) {
//                            Status.NOTCHANGED -> {
//                                opt.invoke(inputFile, Constants.Status_NOTCHANGED)
//                            }
//                            Status.ADDED -> {
//                                prepareTransformClass_(Status.ADDED, inputFile, opt)
//                            }
//                            Status.CHANGED -> {
//                                prepareTransformClass_(Status.CHANGED, inputFile, opt)
//                            }
//                            Status.REMOVED -> {
//                                opt.invoke(inputFile, Constants.Status_REMOVED)
//                            }
//                        }
//                    }
//                } else {
//                    for (inputFile in FileUtils.getAllFiles(inputDir)) {
//                        prepareTransformClass_(null, inputFile, opt)
//                    }
//                }
//                log("Transform source direct : ${inputDir.name} -- end")
//            }
//            log("Transform 【DirectInput】 end")
//
//            //========================================JarInput
//            val jarInput = aInput.jarInputs
//            log("Transform 【JarInput】 start ==== ")
//            for (aJarInput in jarInput) {
//                val inputJarFile = aJarInput.file //jar文件
//                log("Transform Jar : ${inputJarFile.name} -- start")
//                if (isIncremental) {
//                    when (aJarInput.status ?: Status.NOTCHANGED) {
//                        Status.NOTCHANGED -> {
//                            // nothing
//                        }
//                        Status.ADDED, Status.CHANGED -> {
//                            prepareTransformJar(inputJarFile, outputJarFile)
//                        }
//                        Status.REMOVED -> {
//                            FileUtils.delete(outputJarFile)
//                        }
//                    }
//                } else {
//                    prepareTransformJar(inputJarFile, outputJarFile)
//                }
//                log("Transform Jar : ${inputJarFile.name} -- end")
//            }
//            log("Transform 【JarInput】 end ==== ")
//        }
    }

    private fun prepareTransformClass_(status: Status?, inputFile: File, opt: (aInputFile: File, fileStatus: String) -> Void) {
        if (classFilter(inputFile.name)) {
            var statusStr = Constants.Status_NEW
            if (status == Status.ADDED) {
                statusStr = Constants.Status_ADDED
            } else if (status == Status.CHANGED) {
                statusStr = Constants.Status_CHANGED
            }
            opt.invoke(inputFile, statusStr)
        }
    }

    private fun prepareTransformJar_(status: Status?, inputJar: File, opt: (aInputFile: File, fileStatus: String) -> Void){
        var statusStr = Constants.Status_NEW
        if (status == Status.ADDED) {
            statusStr = Constants.Status_ADDED
        } else if (status == Status.CHANGED) {
            statusStr = Constants.Status_CHANGED
        }
        val zipFile = ZipFile(inputJar)

    }

    //inputFile 非dir
    //outputFile 非dir
    private fun prepareTransformClass(inputFile: File, outputFile: File) :Boolean{
        if (classFilter(inputFile.name)) {
            Files.createParentDirs(outputFile)
            val result =  transform.transform(project, inputFile, outputFile)
            if (result){
                log("transform source-class : ${inputFile.name}")
            }else{
                inputFile.copyTo(outputFile)
            }
            return result
        }
        return false
    }

    private fun prepareTransformJar(inputJar: File, outputJar: File):Boolean{
        Files.createParentDirs(outputJar)

        val unzipJarDir = Constants.getUnzipJarTempDir(project, inputJar.name)
        val modifiedJarDir = Constants.getModifiedJarTempDir(project, inputJar.name)

        val fos = FileOutputStream(outputJar)

        val zipFile = ZipFile(inputJar)
        val entries = zipFile.entries()

        fos.use {
            val zos = ZipOutputStream(it)
            zos.use {
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    if (entry.isDirectory) {
                        val desDir = File(unzipJarDir, entry.name)
                        desDir.mkdirs()
                        zos.putNextEntry(ZipEntry(entry.name))
                    } else {
                        val classFile = File(unzipJarDir, entry.name)
                        Files.createParentDirs(classFile)
                        classFile.createNewFile()
                        zipFile.getInputStream(entry).use { zis ->
                            Files.write(zis.readAllBytes(), classFile)
                            zis.close()
                        }
                        val desClassFile = File(modifiedJarDir,entry.name)
                        val transform = transform.transform(project,classFile,desClassFile)
                        if (transform) {
                            log("transform jar-class : ${classFile.name}")
                        } else {
                            classFile.copyTo(desClassFile)
                        }
                        //将修改完毕的class文件写入到 outputJar
                        zos.putNextEntry(ZipEntry(entry.name))
                        FileInputStream(desClassFile).use { fis -> //Closeable.use{}会自动关闭调用者，无论是否出现异常，这里 fis（FileInputStream） 会自动关闭
                            fis.copyTo(zos)
                        }
                    }
                }
            }
        }
        return true
    }

    private fun classFilter(className: String) = className.endsWith(SdkConstants.DOT_CLASS)

    private fun log(msg: String) {
        println("$name : $msg")
    }

}

