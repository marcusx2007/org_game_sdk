### Origin Android SDK 集成文档.

> 简要: 此文档用于帮助集成game-sdk.

#### 集成
  1. 在项目的build.gradle中添加mavenCentral().
  ```gradle
  repositories {
      mavenCentral()
  }
  ```
  2. 在module的build.gradle中添加SDK的依赖配置.
  ```gradle
  implementation("io.github.marcusx2007.origi:game-sdk:0.0.1")
  ```

#### 重要文件说明

* **game-sdk-0.0.1.enc**
  SDK重要配置文件,通过该文件进行SDK的初始化. 代码如下:

```kotlin
import com.gaming.core.GameSDK

  /**
   * 初始化方法,建议在Application的onCreate中进行调用.
   * @param application app上下文
   * @param debug 调试模式,打开相关日志.
   * @param data sdk初始化配置信息.(game-sdk-x.x.x.enc文件内容)
   */
  GameSDK.init(application:Application,debug:Boolean,data:ByteArray)

  /**
   * 必须在init函数后调用.否则无效.
   * @param activity 当前Activity
   * @param invocation, start的结果返回.在该回调函数中执行其他逻辑.
   */
  GameSDK.start(activity: Activity,invocation:()->Unit)
```

