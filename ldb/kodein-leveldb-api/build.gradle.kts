plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-atomicfu")
}

val kotlinxAtomicFuVer: String by getRootProject().extra
val kodeinLogVer: String by getRootProject().extra
val kodeinMemoryVer: String by getRootProject().extra

kodein {
    kotlin {
        common.main.dependencies {
            api("org.kodein.log:kodein-log-api:$kodeinLogVer")
            api("org.kodein.memory:kodein-memory:$kodeinMemoryVer")

            api("org.jetbrains.kotlinx:atomicfu-common:$kotlinxAtomicFuVer")
        }

        add(kodeinTargets.jvm) {
            target.setCompileClasspath()

            main.dependencies {
                compileOnly("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
            }
        }

        sourceSet(kodeinSourceSets.allNative) {
            main.dependencies {
                api("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }
        }

        add(listOf(kodeinTargets.native.linuxX64, kodeinTargets.native.macosX64)) {
            main.dependencies {
                api("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }
        }

        add(kodeinTargets.native.allIos) {
            main.dependencies {
                api("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }
        }
    }
}
