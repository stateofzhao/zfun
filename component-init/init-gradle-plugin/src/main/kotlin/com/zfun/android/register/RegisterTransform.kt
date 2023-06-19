package com.zfun.android.register

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.zfun.android.register.type.ScanItem
import com.zfun.android.register.utils.Logger
import com.zfun.android.register.utils.RegisterCodeGenerator
import com.zfun.android.register.utils.TransformProcessUtil
import org.apache.commons.io.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import java.io.File

class RegisterTransform : Transform() {

    companion object {
        var fileForInjectCode: File? = null
        val scanComponent = arrayOf(ScanItem(Constants.INIT_COMPONENT_NAME))
    }

    /**
     * name of this transform。
     * 最终在Gradle task中显示的名称为：transformClassesAndResourcesWith__ZfunInit__ForDebug
     * transform 字节码转换任务；
     * Classes And Resources 哪种类型的转换，以And链接，这里的 Resources 不是 res下的资源，而是Assets下面的资源；
     * With 后面的就是此转换的名称；
     */
    override fun getName(): String {
        return Constants.PLUGIN_NAME
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * The plugin will scan all classes in the project
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    //是否支持增量编译
    override fun isIncremental(): Boolean {
        return true
    }

    //每个 Transform 的 TransformInvocation 都是独立的，所以 outPutProvider.deleteAll() 只会删除本转换器产出的文件，不影响其它 Transform 的文件
    override fun transform(transformInvocation: TransformInvocation) {
        Logger.i("start transform ====")
        scanComponent.forEach {
            it.classList.clear()
        }
        val outPutProvider = transformInvocation.outputProvider
        val isIncremental = transformInvocation.isIncremental
        if (!isIncremental) {
            outPutProvider.deleteAll()
        }
        //
        transformInvocation.inputs.forEach { input ->
            input.jarInputs.forEach { jarInput ->
                processJarInputWithIncremental(jarInput, outPutProvider, isIncremental)
            }
            input.directoryInputs.forEach { directoryInput ->
                processDirectoryInputWithIncremental(directoryInput, outPutProvider, isIncremental)
            }
        }
        //
        fileForInjectCode?.apply {
            //修改 fileForInjectCode 字节码
            RegisterCodeGenerator.insertInitCode2JarFile(this)
        }

    }

    //=====jar
    private fun processJarInputWithIncremental(jarInput: JarInput, outPutProvider: TransformOutputProvider, isIncremental: Boolean) {
        var destName = jarInput.name
        // rename jar files
        val hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)
        if (destName.endsWith(".jar")) {
            destName = destName.substring(0, destName.length - 4)
        }
        val desFile = outPutProvider.getContentLocation(destName + "_" + hexName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        if (isIncremental) {
            val status = jarInput.status
            if (null == status) {
                if (desFile.exists()) {
                    desFile.delete()
                }
                transformJarInput(jarInput, desFile)
            } else {
                when (status) {
                    Status.NOTCHANGED -> {
                        //nothing
                    }

                    Status.CHANGED -> {
                        if (desFile.exists()) {
                            FileUtils.forceDelete(desFile)
                        }
                        transformJarInput(jarInput, desFile)
                    }

                    Status.ADDED -> {
                        transformJarInput(jarInput, desFile)
                    }

                    Status.REMOVED -> {
                        if (desFile.exists()) {
                            FileUtils.delete(desFile)
                        }
                    }
                }
            }
        } else {
            transformJarInput(jarInput, desFile)
        }
    }

    private fun transformJarInput(jarInput: JarInput, desFile: File) {
        if (TransformProcessUtil.shouldProcessPreDexJar(jarInput.file.absolutePath)) {
            TransformProcessUtil.scanJar(jarInput.file, desFile)
        }
        //将修改过的字节码copy到dest
        FileUtils.copyFile(jarInput.file, desFile)
    }

    //=====classes
    private fun processDirectoryInputWithIncremental(directoryInput: DirectoryInput, outPutProvider: TransformOutputProvider, isIncremental: Boolean) {
        val destDir = outPutProvider.getContentLocation(
                directoryInput.name,
                directoryInput.contentTypes,
                directoryInput.scopes,
                Format.DIRECTORY)
        Logger.i("processDirectoryInputWithIncremental##isIncremental：${isIncremental}")
        if (isIncremental) {
            val srcDirPath = directoryInput.file.absolutePath
            val destDirPath = destDir.absolutePath
            val changedFileMap = directoryInput.changedFiles
            changedFileMap.forEach { entry ->
                val changedInputFile = entry.key
                val changedFileStatus = entry.value ?: Status.CHANGED
                val desFile = File(changedInputFile.absolutePath.replace(srcDirPath, destDirPath))
                when (changedFileStatus) {
                    Status.NOTCHANGED -> {
                    }

                    Status.CHANGED, Status.ADDED -> {
                        FileUtils.touch(desFile)
                        transformSingleFile(changedInputFile, desFile, srcDirPath)
                    }

                    Status.REMOVED -> {
                        if (desFile.exists()) {
                            FileUtils.delete(desFile)
                        }
                    }
                }
            }
        } else {
            transformDirectoryInput(directoryInput, destDir)
        }
    }

    private fun transformDirectoryInput(directoryInput: DirectoryInput, desDir: File) {
        Logger.i("transformDirectoryInput：${directoryInput.file.absolutePath}")
        FileUtils.forceMkdir(desDir)
        eachFileRecurse(directoryInput.file){
            val desFile = File(it.absolutePath.replace(directoryInput.file.absolutePath, desDir.absolutePath))
            transformSingleFile(it, desFile, directoryInput.file.absolutePath)
        }
    }

    private fun eachFileRecurse(inputFile: File, operation: (File) -> Unit) {
        val files = inputFile.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                eachFileRecurse(file, operation)
            } else {
                operation.invoke(file)
            }
        }
    }

    private fun transformSingleFile(inputFile: File, desFile: File, srcDirPath: String) {
        Logger.i("transformSingleFile：${inputFile.absolutePath}")
        var correctSrcDirPath = srcDirPath
        if (!correctSrcDirPath.endsWith(File.separator)) {
            correctSrcDirPath += File.separator
        }
        //
        val path = inputFile.absolutePath.replace(correctSrcDirPath, "")//去除多余的路径
        if (TransformProcessUtil.shouldProcessClassFile(path)) {
            TransformProcessUtil.scanClass(inputFile)
        }
        FileUtils.copyFile(inputFile, desFile)
    }
}