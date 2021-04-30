package com.fan.coroutine.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.fan.coroutine.R
import com.fan.coroutine.api.CoroutineActivity
import kotlinx.coroutines.*

/**
 * 一. 简单协程使用
 */
class MainActivity1 : AppCompatActivity() {

    override fun onDestroy() {
        super.onDestroy()
        println("=======MainActivity onDestroy")
    }

    override fun onStop() {
        super.onStop()
        println("=======MainActivity onStop")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.to_coroutine_activity).setOnClickListener {
            startActivity(Intent(this@MainActivity1, CoroutineActivity::class.java))
        }

//        test1();

//        test2();

//        test3()

        test4();

    }


    /**
     * 1. 单协程体内多 suspend 函数运行
     */
    private fun test1() {
        // 1. 单协程内多 suspend 函数运行
        // 运行代码


        GlobalScope.launch() {
            Log.d(
                "AA",
                "协程 开始执行，时间:  ${System.currentTimeMillis()} 线程：${Thread.currentThread().name} "
            )

            // getToken 和 getResponse 中都是用了 delay. 所以这两个方法是串行执行的.  具体在哪个线程执行, 先不看
            // getToken打印
            // getToken打印delay(300)
            // getToken打印
            // getResponse打印
            // getResponse delay
            // getResponse打印
            val token = getToken()
            val response = getResponse(token)
            setText(response)
        }

        // 在 GlobalScope.launch 后面代码, 是否比 launch()中的第一句代码执行的快, 主要是看 GlobalScope.launch() 否切换线程.
        // 如果GlobalScope.launch() 需要切换线程, 那它需要花点时间, 因此 下面的代码先执行
        Log.d(
            "AA",
            "in MainActivity:  ${System.currentTimeMillis()} 线程：${Thread.currentThread().name} "
        )
    }


    /**
     * 2. 多协程间 suspend 函数运行
     */
    private fun test2() {
        GlobalScope.launch(Dispatchers.Unconfined) {
            Log.d("AA", "协程 开始执行，时间:  ${System.currentTimeMillis()}")

            // async 是创建一个带有返回值的协程, 返回值通过 suspend 方法 await()方法拿到.
            // 因为这里执行了 await() 方法, await() 方法 是用 suspend 修饰kotlin 自带方法, 所以协程会被挂起
            var token = GlobalScope.async(Dispatchers.Unconfined) {
                return@async getToken()
            }.await()

            // 等 getToken()执行完, 上面的 await()拿到执行结果, 协程会恢复, 继续执行下面的代码
            // 同样因为这里执行了 await()方法, 协程会被挂起
            var response = GlobalScope.async(Dispatchers.Unconfined) {
                return@async getResponse(token)
            }.await()

            // 等 getResponse() 方法执行完成, 协程恢复, 继续执行下面的 setText()
            setText(response)
        }
        // 在 GlobalScope.launch 后面代码, 是否比 launch()中的第一句代码执行的快, 主要是看 GlobalScope.launch() 否切换线程.
        // 如果GlobalScope.launch() 需要切换线程, 那它需要花点时间, 因此 下面的代码先执行
        // 好像只有指定 Dispatchers.Unconfined, 下面这句代码后执行...
        Log.d(
            "AA",
            "GlobalScope.launch 后面的代码好像总是比 GlobalScope.launch 跑的快..  ${System.currentTimeMillis()}"
        )
    }


    /**
     * 3. 协程挂起后再恢复时在哪个线程运行
     */
    private fun test3() {
        // Job，任务，封装了协程中需要执行的代码逻辑。Job 可以取消并且有简单生命周期，它有三种状态：
        // Job 完成时是没有返回值的，如果需要返回值的话，应该使用 Deferred，它是 Job 的子类
        // launch 方法返回 Job.
        // async 方法返回 Deferred
        //When using the default dispatcher worker thread, there is no guarantee one will use the same worker thread pre and post yield.
        val job = CoroutineScope(Dispatchers.Unconfined).launch {
            Log.d(
                "AA",
                "协程测试 开始执行，时间:  ${System.currentTimeMillis()}  线程：${Thread.currentThread().name}"
            )
            // async 返回值是 Deferred类型.
            // 再调用 await()方法, 拿到的返回值就是 {} 代码块中返回的值
            var token = async() {
                return@async getToken()
            }.await()

            Log.d("AA", "after getToken: token = $token   线程：${Thread.currentThread().name}")

            var response = async(Dispatchers.Default) {
                return@async getResponse("token")
            }.await()

            // async的返回值, 就是 内部 lambda 表达式的返回值
            Log.d(
                "AA",
                "after getResponse: response = $response   线程：${Thread.currentThread().name}"
            )


            var token2 = async() {
                return@async getToken()
            }.await()
            setText(response)
        }

        Log.d("AA", "主线程协程后面代码执行，线程：${Thread.currentThread().name}")// main


        // 全 Dispatchers.Main
        //
        // D/AA: 主线程协程后面代码执行，线程：main
        // D/AA: 协程测试 开始执行，时间:  1615450879529  线程：main
        // D/AA: getToken 开始执行，时间:  1615450879990  线程：main
        // D/AA: getResponse 开始执行，时间:  1615450880092  线程：main
        // D/AA: setText 执行，时间:  1615450880093  线程：main


        // GlobalScope.launch(Dispatchers.IO)  GlobalScope.async(Dispatchers.Main) GlobalScope.async(Dispatchers.Main)
        //
        // D/AA: 主线程协程后面代码执行，线程：main
        // D/AA: 协程测试 开始执行，时间:  1615450945658  线程：DefaultDispatcher-worker-1
        // D/AA: getToken 开始执行，时间:  1615450946181  线程：main
        // D/AA: getResponse 开始执行，时间:  1615450946289  线程：main
        // D/AA: setText 执行，时间:  1615450946290  线程：DefaultDispatcher-worker-1
        // 总结:
        //  指定了: CoroutineScope(Dispatchers.IO), 那 launch 代码块中都运行在 IO 线程.
        //  async(Dispatchers.Main) 明确指定了要切线程, 那 async 代码块中都运行在 Main 线程. 包括挂起之前和恢复.
        //  另外: When launching on Dispatchers.IO or Dispatchers.Default, there are using the same pool of worker thread (for now). 如果指定了 Dispatchers.IO 或者 Dispatchers.Default, 都运行在一个相同的线程池中:DefaultDispatcher-worker 线程池. 有可能是运行在该线程池的不同线程上


        // GlobalScope.launch(Dispatchers.Main)  GlobalScope.async(Dispatchers.IO) GlobalScope.async(Dispatchers.IO)
        //
        // D/AA: 主线程协程后面代码执行，线程：main
        // D/AA: 协程测试 开始执行，时间:  1615451045420  线程：main
        // D/AA: getToken 开始执行，时间:  1615451045729  线程：DefaultDispatcher-worker-2
        // D/AA: getResponse 开始执行，时间:  1615451045845  线程：DefaultDispatcher-worker-1
        // D/AA: setText 执行，时间:  1615451045847  线程：main
        // 分析:
        // 原理同上, 明确指定了协程运行在哪个线程, 那挂起之前和恢复都会运行在这个指定的线程上, 没问题


        // GlobalScope.launch(Dispatchers.Main)  GlobalScope.async(Dispatchers.Unconfined) GlobalScope.async(Dispatchers.Unconfined)
        //
        // D/AA: 主线程协程后面代码执行，线程：main
        // D/AA: 协程测试 开始执行，时间:  1619678369111  线程：main
        // D/AA: before delay getToken 开始执行，时间:  1619678369114  线程：main
        // D/AA: getToken 恢复执行，时间:  1619678369418  线程：kotlinx.coroutines.DefaultExecutor
        // D/AA: after getToken: token = ask   线程：main
        // D/AA: before getResponse 开始执行，时间:  1619678369426  线程：main
        // D/AA: getResponse 恢复执行，时间:  1619678369527  线程：kotlinx.coroutines.DefaultExecutor
        // D/AA: after getResponse: response = response   线程：main
        // D/AA: setText 执行，时间:  1619678369528  线程：main
        // 分析: Unconfined 表示无限制的
        // 最外层 GlobalScope.launch(Dispatchers.Main) 指定 Main, 则这个协程代码块都运行在 main.
        // getToken 挂起之前运行在 main, 是因为 GlobalScope.async(Dispatchers.Unconfined) 指定的 Unconfined,表示不限制, 就默认在调用它的线程上执行了
        // 但是恢复, 都是恢复在 DefaultExecutor 线程池上
        // getResponse 和 getToken 的原理一样


        // GlobalScope.launch(Dispatchers.Unconfined)  GlobalScope.async(Dispatchers.Undefined) GlobalScope.async(Dispatchers.IO)
        //
        // D/AA: 协程测试 开始执行，时间:  1619678489168  线程：main
        // D/AA: before delay getToken 开始执行，时间:  1619678489171  线程：main
        // D/AA: 主线程协程后面代码执行，线程：main
        // D/AA: getToken 恢复执行，时间:  1619678489473  线程：kotlinx.coroutines.DefaultExecutor
        // D/AA: after getToken: token = ask   线程：kotlinx.coroutines.DefaultExecutor
        // D/AA: before getResponse 开始执行，时间:  1619678489505  线程：DefaultDispatcher-worker-1
        // D/AA: getResponse 恢复执行，时间:  1619678489607  线程：DefaultDispatcher-worker-1
        // D/AA: after getResponse: response = response   线程：DefaultDispatcher-worker-1
        // D/AA: setText 执行，时间:  1619678489608  线程：DefaultDispatcher-worker-1
        // 分析: Unconfined 表示无限制的
        // 最外层指定: GlobalScope.launch(Dispatchers.Unconfined), 因此代码默认执行在调用它的线程, 也就是 main
        // getToken 挂起前, 也是在 main 上执行, 是因为 第一个 async 指定了 GlobalScope.async(Dispatchers.Undefined)
        // 但是 getToken 恢复在 DefaultExecutor
        // getResponse 由于 第二个 async 明确指定了IO 线程, 因此, getResponse 的执行和恢复都发生在 IO 线程
        // 最后, setText 也是执行在 IO 线程, 是因为在外层指定的 Unconfined, 这里没有再切换线程, 就默认跟 getResponse 在一个线程上执行了

        // 规律: Unconfined 是不限制, 不管, 当前是啥线程, 就在啥线程上执行.
        // Dispatchers.Unconfined，会在协程挂起后把协程当做一个任务 DelayedResumeTask 放到默认线程池 DefaultExecutor 队列的最后，在延迟的时间到达才会执行恢复协程任务。
        // 因此, 我们看到, 如果指定了 Unconfined, 恢复都是在 DefaultExecutor上的.

        //====================================================================================================
        // 虽然多个协程之间可能不是在同一个线程上运行的，但是协程内部的机制可以保证我们书写的协程是按照我们指定的顺序或者逻辑自行
        // 如果指定了 IO 或者 Main, 不会放到默认线程池的

        // When there is no dispatchers provided, the coroutine will be dispatched on the same processes of the calling function.
        // 如果自始至终都没有指定过 Dispatchers, 那这个协程就被派发到调用它的线程上去.

        // The launch is now also on DefaultDispatcher as it follows its parent.
        // 如果父协程指定了 Dispatchers, 子协程没有指定Dispatchers, 子协程的Dispatchers就跟父一样

        // 看懂这个输出, 就明白挂起和恢复的线程切换了
        // 四个, 分别是 Unconfined, 不指定, Default 不指定
        //03-16 13:44:46.831 11193-11193/com.fan.coroutine D/AA: 协程测试 开始执行，时间:  1615873486831  线程：main
        // Dispatchers.Unconfined: It executes initial continuation of tnt cahe coroutine in the currell-frame, 因此是main
        //
        //03-16 13:44:46.834 11193-11193/com.fan.coroutine D/AA: before delay getToken 开始执行，时间:  1615873486834  线程：main
        // 因为没有指定, 子协程就使用和父协程指定的相同的 Dispatchers. 因为父指定了 Unconfined, 因此是在调用它的线程上执行, 因此是main

        //03-16 13:44:47.138 11193-11207/com.fan.coroutine D/AA: getToken 开始执行，时间:  1615873487138  线程：kotlinx.coroutines.DefaultExecutor
        // 由于上面父指定了 Dispatchers.Unconfined, 子协程未指定, 因此子也是Dispatchers.Unconfined,  getToken 的恢复是在DefaultExecutor上
        //03-16 13:44:47.139 11193-11207/com.fan.coroutine D/AA: after getToken: token = ask
        // 获取到结果

        //03-16 13:44:47.148 11193-11245/com.fan.coroutine D/AA: before getResponse 开始执行，时间:  1615873487148  线程：DefaultDispatcher-worker-1
        // 由于指定了 async(Dispatchers.Default), 因此是在默认的线程池中执行, 并且在线程池中恢复.
        //03-16 13:44:47.249 11193-11246/com.fan.coroutine D/AA: getResponse 开始执行，时间:  1615873487249  线程：DefaultDispatcher-worker-2
        // 恢复


        //03-16 13:44:47.250 11193-11246/com.fan.coroutine D/AA: before delay getToken 开始执行，时间:  1615873487250  线程：DefaultDispatcher-worker-2
        // 这次又由于 async 没有指定Dispatchers, 就使用和父协程相同的Dispatchers, Unconfined, 在调用方法的线程上执行, 因此是 DefaultDispatcher-worker-2, 与上面的 getResponse 指定的 Dispatcher.IO一样
        //03-16 13:44:47.557 11193-11207/com.fan.coroutine D/AA: getToken 开始执行，时间:  1615873487557  线程：kotlinx.coroutines.DefaultExecutor
        // 由于和父协程相同的Dispatchers,  不限制 Dispatchers.Unconfined, 因此恢复是在 DefaultExecutor
        //03-16 13:44:47.557 11193-11207/com.fan.coroutine D/AA: setText 执行，时间:  1615873487557  线程：kotlinx.coroutines.DefaultExecutor
        // 由于最外面父是不限制, 因此这行代码就与上面最后一个相同, 不切线程

        // 最终总结:
        // 1. Dispatchers.Default、Dispatchers.IO和Dispatchers.Main属于Confined dispatcher，都指定了协程所运行的线程或线程池，挂起函数恢复后协程也是运行在指定的线程或线程池上的
        // 2. Dispatchers.Unconfined属于Unconfined dispatcher，协程启动并运行在 Caller Thread 上，但是只是在第一个挂起点之前是这样的，挂起恢复后运行在DefaultExecutor
        // 3. 如果父协程指定了 Dispatchers, 子协程没有指定 Dispatchers, 子协程的 Dispatchers就跟父一样
        // 4. 如果自始至终都没有指定过 Dispatchers, 那这个协程就被派发到调用它的线程上去.  也就是说, 不明确指定的话, 默认值是: Dispatchers.Undefined

        // Default默认的线程池一般用于计算型任务。注意它和IO共享线程池，只不过限制了最大并发数不同
        // Main 所谓的Ui线程，在Android中进行UI绘制的线程，或者Swing中invokeLater。此处根据平台实现，利用serviceLoader加载
        // Unconfined 未定义的线程，使用这个启动的协程会立即在当前的线程执行，并且遇到第一个挂起点后根据其挂起点回调线程决定后续的代码在哪运行。（后文再讲）
        // IO 一个用于经常IO操作的线程池，高并行量。与Default共享线程池

    }


    /**
     * 4. 测试 yield
     */
    private fun test4(){

        runBlocking {
            println("runBlocking pre-yield ${Thread.currentThread()}")
            launch {
                println("launch pre-yield ${Thread.currentThread()}")
                yield()
                println("launch pos-yield ${Thread.currentThread()}")
            }
            yield()
            println("runBlocking pos-yield ${Thread.currentThread()}")
        }
        runBlocking {
            this.launch {
                delay(1000)
                println("3. coroutine ONE")
            }

            this.launch {
                delay(500)
                println("2. coroutine TWO")
            }
        }

        println("4. Only when the children inside runBlocking complete, execution follows on this line")
    }

    /**
     * 用于测试的方法1
     */
    private suspend fun getToken(): String {
        // delay 也是一个 suspend 函数
        Log.d(
            "AA",
            "before delay getToken 开始执行，时间:  ${System.currentTimeMillis()}  线程：${Thread.currentThread().name} "
        )
        delay(300)
        Log.d(
            "AA",
            "getToken 恢复执行，时间:  ${System.currentTimeMillis()}  线程：${Thread.currentThread().name} "
        )
        return "ask"
    }


    /**
     * 用于测试的方法2
     */
    private suspend fun getResponse(token: String): String {
        Log.d(
            "AA",
            "before getResponse 开始执行，时间:  ${System.currentTimeMillis()}  线程：${Thread.currentThread().name}"
        )
        delay(100)
        Log.d(
            "AA",
            "getResponse 恢复执行，时间:  ${System.currentTimeMillis()}  线程：${Thread.currentThread().name}"
        )
        return "response"
    }

    /**
     * 用于测试的方法3
     */
    private fun setText(response: String) {
        Log.d(
            "AA",
            "setText 执行，时间:  ${System.currentTimeMillis()}  线程：${Thread.currentThread().name}"
        )


    }

//    suspend fun <T> Call<T>.await(): T = suspendCoroutine { cont ->
//        enqueue(object : Callback<T> {
//            override fun onResponse(call: Call<T>, response: Response<T>) {
//                if (response.isSuccessful) {
//                    cont.resume(response.body()!!)
//                } else {
//                    cont.resumeWithException(ErrorResponse(response))
//                }
//            }
//            override fun onFailure(call: Call<T>, t: Throwable) {
//                cont.resumeWithException(t)
//            }
//        })
//    }
}