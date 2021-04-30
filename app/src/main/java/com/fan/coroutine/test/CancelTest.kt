package com.fan.coroutine.test

import kotlinx.coroutines.*
import kotlin.coroutines.suspendCoroutine

/**
 *
 *
 */
fun main(args: Array<String>) {

//    cancelTest();

    runBlocking {
        scopeTest()
    }
}

/**
 * 1. 演示协程cancel相关使用
 *
 * 协程的取消只是状态的变化，并不会取消协程的实际运算逻辑
 */
private fun cancelTest() = runBlocking {
    val job1 = launch(Dispatchers.Default) {
        repeat(5) {
            println("job1 sleep ${it + 1} times")
            delay(500)
        }
    }
    delay(700)
    job1.cancel()
    // 上面代码中 job1 取消后，delay()会检测协程是否已取消，所以 job1 之后的运算就结束了；

    val job2 = launch(Dispatchers.Default) {
        var nextPrintTime = 0L
        var i = 1
        while (i <= 4) {
            //println("i=$i")
            val currentTime = System.currentTimeMillis()
            //println("currentTime:$currentTime   next:$nextPrintTime")
            if (currentTime >= nextPrintTime) {
                println("job2 sleep ${i++} ...")
                nextPrintTime = currentTime + 500L
            }
        }
    }
    delay(700)
    println("after delay")
    job2.cancel()
    println("after job2 cancel")
    //  job2 取消后，没有检测协程状态的逻辑，都是计算逻辑，所以 job2 的运算逻辑还是会继续运行。直到执行完毕
}


/**
 * 2. coroutineScope and supervisorScope
 *
 * coroutineScope and supervisorScope let you safely launch coroutines from suspend functions.
 * coroutineScope 和 supervisorScope 允许你从 suspend 方法中安全的启动协程.
 * 在 suspend 方法中, 是不可以直接调用 launch 或者  async 来创建协程
 * 因此, 在 suspend 方法中, 需要使用coroutineScope或者supervisorScope 来创建协程并启动.
 *
 * 我们平常写的 launch{}  其实就是: CoroutineScope.launch
 *
 * coroutineScope and supervisorScope will wait for child coroutines to complete.
 * coroutineScope 和 supervisorScope 会等待所有的子协程完成, 自己才会结束
 *
 * What’s really cool is coroutineScope will create a child scope.  So if the parent scope gets cancelled, it will pass the cancellation down to all the new coroutines.
 * No matter what calling scope it is, the coroutineScope builder will use it as the parent to the new scope it creates.
 *
 * 无论在哪里调用 coroutineScope, 无论调用 coroutineScope 的作用域是哪个,  coroutineScope都会创建一个子作用域, 并把外面的 Scope 作为父作用域.
 * 如果父作用域被取消掉了, 将会把 cancel 事件传递给所有的子作用域.
 *
 */
suspend fun scopeTest() {
    println("begin of f")

    coroutineScope {
        launch {
            delay(1000)
            println(" launch ")
        }
        async {
            delay(100)
            println("async")
        }
    }

    supervisorScope { }

    // 区别:
    // coroutineScope 内部的异常会向上传播，子协程未捕获的异常会向上传递给父协程，任何一个子协程异常退出，会导致整体的退出。
    // supervisorScope 内部的异常不会向上传播，一个子协程异常退出，不会影响父协程和兄弟协程的运行。

    suspendCoroutine<String> { con -> println("1") }

    println("end of f")
}


// 3.
// launch 与 runBlocking 都能在全局开启一个协程，但 launch 是非阻塞的 而 runBlocking 是阻塞的, runBlocking 会阻塞当前线程

// withContext 与 async 都可以返回耗时任务的执行结果。 一般来说，多个 withContext 任务是串行的， 且 withContext 可直接返回耗时任务的结果。 多个 async 任务是并行的，async 返回的是一个Deferred<T>，需要调用其await()方法获取结果。

// 总结:
// launch 和 async 都是非阻塞, launch 无法拿到结果,  async 可以拿到结果, 需要调用await
// runBlocking 和 withContext 都是阻塞, runBlocking 无法拿到结果, withContext 可以直接拿到结果, 不需要await
// 这几个都是可以指定线程的