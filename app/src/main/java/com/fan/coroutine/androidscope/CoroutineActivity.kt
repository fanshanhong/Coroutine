package com.fan.coroutine.androidscope;

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.*
import java.lang.Exception


/**
 * 演示 协程在 Android 页面中的使用
 * 配合 UI 生命周期作用域
 */
open class CoroutineActivity : ScopedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 方式1
        val mainScope = MainScope()
        mainScope.launch {  }

        // 2. 方式2 构造带有作用域的抽象 Activity
        // 就是 ScopedActivity 的实现 CoroutineScope 接口
        // 后续可以直接调用 launch
        launch {  }// 就相当于:mainScope.launch {  }

        // 3. 还可以将这个 Scope 实例传递给其他需要的模块
        val coroutinePresenter = CoroutinePresenter(this)
        coroutinePresenter.getUserData()

        // mainScope 的生命周期跟 Activity 是一致的, 因为在 onDestroy 中调用 cancel 方法了

        // 4. lifecycleScope
        // lifecycleScope 与 Activity 的生命周期是一致的.
        // 当 Activity 关闭, 或者横竖屏切换, 导致activity 销毁,  这个协程就会被取消
        // 当 Activity 重建, 重建协程并启动
        // 如果考虑到Activity的杀死和重建, 更好的方案或许是 viewModelScope
        lifecycleScope.launch {
            try {
                println("in lifecycleScope start")
                delay(4000)
                println("in lifecycleScope end")
            } catch (e: Exception) {
                println(" in lifecycle: $e")
            }
        }

        lifecycleScope.launch {

            println("... ${Thread.currentThread().name}")// main

            val job = async(Dispatchers.IO) {
                println("...  begin read: ${Thread.currentThread().name}") //IO, 切线程到:DefaultDispatcher-worker-1
                delay(100)
                println("...  after read, 恢复 ${Thread.currentThread().name}")//IO, 切线程到:DefaultDispatcher-worker-1
                "jjjjj"
            }

            println("... 读到结果: ${job.await()}   ${Thread.currentThread().name} ") // main
        }
    }
}
