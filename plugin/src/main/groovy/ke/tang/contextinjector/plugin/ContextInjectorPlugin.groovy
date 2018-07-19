package ke.tang.contextinjector.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import ke.tang.contextinjector.annotations.Constants
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 自动生成Service和插入相关ContextInjector依赖
 */
class ContextInjectorPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(LibraryPlugin)) {
            throw new IllegalStateException("ContextInjector插件只能用在Android项目中")
        }

        def extension = project.extensions.getByType(AppExtension)
        if (null == extension) {
            extension = project.extensions.getByType(LibraryExtension)
        }

        def sourcePath = extension.sourceSets.getByName("main").resources.srcDirs[0].absolutePath
        extension.buildTypes.each {
            it.javaCompileOptions.annotationProcessorOptions.arguments.put(Constants.CONTEXT_INJECTOR_RESOURCE_PATH, sourcePath)
        }

        project.dependencies.add("annotationProcessor", "ke.tang:context-injector-compiler:1.0.1")
        project.dependencies.add("implementation", "ke.tang:context-injector-annotations:1.0.1")
        project.dependencies.add("implementation", "ke.tang:context-injector:1.0.1")
    }
}
