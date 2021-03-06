import org.jetbrains.kotlin.gradle.targets.js.npm.SemVer
import org.kodein.gradle.cmake.CMakeOptions
import org.kodein.gradle.cmake.CMakePlugin
import java.util.*

plugins {
    id("org.kodein.local-properties")
    id("org.kodein.gradle.cmake")
}

val buildAll = tasks.create("build") {
    group = "build"
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()!!

val excludedTargets = kodeinLocalProperties.getAsList("excludeTargets")
val withAndroid = "android" !in excludedTargets

fun addCMakeTasks(lib: String, target: String, dir: String = lib, specificConf: CMakePlugin.Extension.Compilation.() -> Unit): Task =
    cmake.compilation("$lib-$target") {
        conf {
            cmakeProjectDir = project(":ldb:lib").projectDir.resolve("src/$dir")
            installPath = buildDir.resolve("out/$target").absolutePath

            cmakeOptions {
                +"CMAKE_POSITION_INDEPENDENT_CODE:BOOL"

                "CMAKE_C_FLAGS:STRING" += "-D_GLIBCXX_USE_CXX11_ABI=0"
                "CMAKE_CXX_FLAGS:STRING" += "-D_GLIBCXX_USE_CXX11_ABI=0"
                "CMAKE_BUILD_TYPE" += "Release"
            }
        }
        build {
            args("--config", "Release")
        }
        specificConf()
    }


fun addTarget(target: String, fpic: Boolean = true, specificOptions: CMakeOptions.() -> Unit) : Task {
    val buildCrc32c = addCMakeTasks("crc32c", target) {
        conf.cmakeOptions {
            "CRC32C_BUILD_BENCHMARKS:BOOL" += "0"
            "CRC32C_BUILD_TESTS:BOOL" += "0"
            "CRC32C_USE_GLOG:BOOL" += "0"
            if (fpic) {
                "CMAKE_C_FLAGS:STRING" += "-fPIC"
                "CMAKE_CXX_FLAGS:STRING" += "-fPIC"
            }
            specificOptions()
        }
    }

    val buildSnappy = addCMakeTasks("snappy", target) {
        conf.cmakeOptions {
            "SNAPPY_BUILD_TESTS:BOOL" += "0"
            specificOptions()
        }
    }

    val buildLevelDB = addCMakeTasks("leveldb", target, "leveldb-kodein") {
        conf {
            dependsOn(buildCrc32c, buildSnappy)
            cmakeOptions {
                "LEVELDB_BUILD_BENCHMARKS:BOOL" += "0"
                "LEVELDB_BUILD_TESTS:BOOL" += "0"

                "CMAKE_C_FLAGS:STRING" += "-I$buildDir/out/$target/include"
                "CMAKE_CXX_FLAGS:STRING" += "-I$buildDir/out/$target/include"
                +"HAVE_CRC32C"
                +"HAVE_SNAPPY"
                specificOptions()
            }
        }
    }

    return buildLevelDB
}

fun addHostTarget(target: String, specificOptions: CMakeOptions.() -> Unit) = addTarget(target, fpic = !currentOs.isWindows) {
    when {
        currentOs.isMacOsX -> {
            "CMAKE_C_FLAGS:STRING" += "-mmacosx-version-min=10.11"
            "CMAKE_CXX_FLAGS:STRING" += "-mmacosx-version-min=10.11"
        }
    }
    specificOptions()
}

val osName: String by rootProject.extra

addHostTarget(osName) {
    if (!currentOs.isWindows) {
        "CMAKE_C_COMPILER:STRING" += "clang"
        "CMAKE_CXX_COMPILER:STRING" += "clang++"
    }
}

val konanBuild = addHostTarget("konan-$osName") {
    when {
        currentOs.isLinux -> {
            val dependenciesPath = "${System.getenv("HOME")}/.konan/dependencies"
            val llvmBinPath = "$dependenciesPath/clang-llvm-8.0.0-linux-x86-64/bin"
            "CMAKE_C_COMPILER:STRING" += "$llvmBinPath/clang"
            "CMAKE_CXX_COMPILER:STRING" += "$llvmBinPath/clang++"
            val toochainPath = "$dependenciesPath/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2"
            val cFlags = "--gcc-toolchain=$toochainPath"
            "CMAKE_SYSROOT:PATH" += "$toochainPath/x86_64-unknown-linux-gnu/sysroot"
            "CMAKE_C_FLAGS:STRING" += cFlags
            "CMAKE_CXX_FLAGS:STRING" += cFlags
        }
        currentOs.isMacOsX -> {
            "CMAKE_C_COMPILER:STRING" += "clang"
            "CMAKE_CXX_COMPILER:STRING" += "clang++"
        }
        currentOs.isWindows -> {
            val userHome = System.getProperty("user.home").replace('\\', '/')
            val path = "$userHome/.konan/dependencies/msys2-mingw-w64-x86_64-clang-llvm-lld-compiler_rt-8.0.1"
            "CMAKE_C_COMPILER:STRING" += "$path/bin/clang.exe"
            "CMAKE_CXX_COMPILER:STRING" += "$path/bin/clang++.exe"
            "CMAKE_SYSROOT:PATH" += path
            val cFlags = "-femulated-tls"
            "CMAKE_C_FLAGS:STRING" += cFlags
            "CMAKE_CXX_FLAGS:STRING" += "$cFlags -std=c++11"
            "G" -= "MinGW Makefiles"
        }
    }
}

if (currentOs.isWindows || currentOs.isLinux) {
    tasks.create<Exec>("archiveFatLeveldb-konan-$osName") {
        group = "build"
        dependsOn(konanBuild)
        workingDir("$buildDir/out/konan-$osName/lib")
        outputs.file("$workingDir/libfatleveldb.a")
        inputs.files(
            "$workingDir/libcrc32c.a",
            "$workingDir/libsnappy.a",
            "$workingDir/libleveldb.a"
        )
        commandLine("ar", "-M")
        standardInput = """	
                create libfatleveldb.a	
                addlib libcrc32c.a	
                addlib libsnappy.a	
                addlib libleveldb.a	
                save	
                end	
            """.trimIndent().byteInputStream()
    }
}


if (withAndroid) {
    val localPropsFile = rootProject.file("local.properties")
    check(localPropsFile.exists()) { "Please create android root local.properties" }
    val localProps = localPropsFile.inputStream().use { Properties().apply { load(it) } }
    val sdkDir = localProps["sdk.dir"]?.let { file(it) }
            ?: throw IllegalStateException("Please set sdk.dir android sdk path in root local.properties")
    val ndkDir = sdkDir.resolve("ndk-bundle").takeIf { it.exists() }
            ?: sdkDir.resolve("ndk").takeIf { it.exists() }
                    ?.listFiles { f -> f.isDirectory }
                    ?.reduce { l, r -> if (SemVer.from(l.name) >= SemVer.from(r.name)) l else r }
            ?: throw IllegalStateException("Please install NDK")

    fun addAndroidTarget(target: String) {
        val build = addTarget("android-$target") {
            "CMAKE_TOOLCHAIN_FILE:PATH" += "${ndkDir.absolutePath}/build/cmake/android.toolchain.cmake"
            "ANDROID_NDK:PATH" += "${ndkDir.absolutePath}/"
            "ANDROID_PLATFORM:STRING" += "android-16"
            "ANDROID_ABI:STRING" += target
        }

        tasks.maybeCreate("buildAllAndroidLibs").apply {
            group = "build"
            dependsOn(build)
        }
    }

    addAndroidTarget("armeabi-v7a")
    addAndroidTarget("arm64-v8a")
    addAndroidTarget("x86")
    addAndroidTarget("x86_64")
}

fun addIosTarget(target: String, specificOptions: CMakeOptions.() -> Unit = {}) {
    val build = if (currentOs.isMacOsX) addTarget("ios-$target") {
        "G" -= "Xcode"
        "CMAKE_TOOLCHAIN_FILE:PATH" += "${projectDir.absolutePath}/src/ios-cmake/ios.toolchain.cmake"
        "PLATFORM:STRING" += target.toUpperCase()
        "CMAKE_C_FLAGS:STRING" += "-Wno-shorten-64-to-32"
        "CMAKE_CXX_FLAGS:STRING" += "-Wno-shorten-64-to-32"
        specificOptions()
    }
    else task("buildLeveldb-ios-${target}") {
        enabled = false
    }

    tasks.maybeCreate("buildAllIosLibs").apply {
        group = "build"
        dependsOn(build)
    }
}

addIosTarget("os")
addIosTarget("simulator64")
addIosTarget("watchos") {
    "CMAKE_CXX_FLAGS:STRING" += "-std=c++11"
}
addIosTarget("simulator_watchos") {
    "CMAKE_CXX_FLAGS:STRING" += "-std=c++11"
}
addIosTarget("tvos")
addIosTarget("simulator_tvos")

tasks.create<Delete>("clean") {
    delete(buildDir)
}
