package com.fan.coroutine.test

import kotlin.coroutines.*
import kotlinx.coroutines.*

/**
 * @Description: 演示拦截器的使用
 * @Author: shanhongfan
 * @Date: 2021/3/16 15:02
 * @Modify:
 */


/**
 * 演示拦截器使用1
 */
fun main() {
    runBlocking {
        testContinuationInterceptor()
    }

}

fun testContinuationInterceptor() {
    val interception = object : ContinuationInterceptor {
        override val key: CoroutineContext.Key<*>
            get() = ContinuationInterceptor

        override fun <T> interceptContinuation(
            continuation: Continuation<T>
        ): Continuation<T> {
            println("  ## Interception Setup for ${continuation.context[Job]} ##")
            return Continuation(continuation.context) {
                println("  ~~ Interception for {continuation.context[Job]} ~~")
                continuation.resumeWith(it)
            }
        }
    }

    runBlocking(CoroutineName("runBlocker") + interception) {
        println("Started runBlocking")
        launch(CoroutineName("launcher")) {
            println("Started launch")
            delay(10)
            println("End launch")
        }
        delay(10)
        println("End runBlocking")
    }
}


/**
 * 演示拦截器使用2
 */
class MyContinuationInterceptor : ContinuationInterceptor {
    override val key = ContinuationInterceptor
    override fun <T> interceptContinuation(continuation: Continuation<T>) =
        MyContinuation(continuation)
}

class MyContinuation<T>(val continuation: Continuation<T>) : Continuation<T> {
    override val context = continuation.context
    override fun resumeWith(result: Result<T>) {
        println("<MyContinuation> $result")
        continuation.resumeWith(result)
    }
}