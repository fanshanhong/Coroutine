package com.fan.coroutine.test

import kotlinx.coroutines.*
import java.lang.Exception
import java.lang.IllegalStateException
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor

/**
 * 演示 Coroutine 异常相关处理
 */
fun main() {
    runBlocking {

        //1. launch 默认就是 coroutineScope,  子协程抛出异常, 父收到, 父先cancel 它的子, 然后cancel自己

        val pjob = launch {
            try {
                val job1 = async {
                    delay(1000)
                    println(" launch ")
                    throw Exception()//子协程抛出异常, 父收到, 父先cancel 它的子, 然后cancel自己
                }
                val job2 = async {
                    try {
                        println("async")
                        delay(2000)
                    } catch (e: CancellationException) {
                        println("in async:$e")// 先进入这里
                    }
                }

                delay(3000)
            } catch (e: CancellationException) {
                println("in outer launch:$e")// 再进入这里
            }
        }

        // 2. coroutineScope 与1相同, launch{} 就是 CoroutineScope.launch
        coroutineScope {
            try {
                val job1 = async {
                    delay(1000)
                    println(" launch ")
                    throw Exception()
                }
                val job2 = async {
                    try {
                        println("async")
                        delay(2000)
                    } catch (e: CancellationException) {
                        println("in async:$e")
                    }
                }

                delay(3000)
            } catch (e: CancellationException) {
                println("in outer launch:$e")
            }
        }


        // 3. supervisorScope: 子协程抛出异常, 只结束自己, 不影响父和兄弟
        supervisorScope {
            try {
                val job1 = async {
                    delay(1000)
                    println(" launch ")
                    throw Exception()
                }
                val job2 = async {
                    try {
                        println("async")
                        delay(2000)
                    } catch (e: CancellationException) {
                        println("in async:$e")
                    }
                }

                delay(3000)
            } catch (e: CancellationException) {
                println("in outer launch:$e")
            }
        }


        // 4. 父抛出异常, 子也跟着 cancel 了
        val parentJob = launch {
            try {
                println("launch parent job")
                val childJob = launch {
                    try {
                        println("launch child job")
                        delay(1000)
                        println("finish child job")
                    } catch (e: CancellationException) {
                        println("cancel child job")
                    }
                }
                delay(500)
                throw Exception()
                println("finish parent job")
            } catch (e: CancellationException) {
                println("cancel parent job")
            }
        }


        // 5. 这个跟1一样, 子抛出异常, 父也cancel了.
        val parentJob7 = launch {
            try {
                println("launch parent job")
                val childJob = launch {
                    try {
                        println("launch child job")
                        delay(500)
                        throw Exception()
                    } catch (e: CancellationException) {
                        println("cancel child job")
                    }
                }
                delay(1000)
                println("finish parent job")
            } catch (e: CancellationException) {
                println("cancel parent job")
            }
        }


        // 6. launch join , 子协程抛出异常, 父也会收到的, 并不是直接吞了一说
        // 异常捕获了
        launch {
            try {
                var job = launch {
                    delay(200)
                    throw  Exception()
                }
                job.join()
                println("111")
            } catch (e: Exception) {
                println("22 $e")
            }
        }

        // 7. async join ,子抛出异常, 父也是会cancel的, 因为 这里launch 和 async 都是 CoroutineScope, 要遵循这个作用域
        // 异常捕获了
        launch {
            try {
                var job = async {
                    delay(200)
                    throw  Exception()
                }
                job.join()
                println("111")
            } catch (e: Exception) {
                println("22 $e")
            }
        }

        // 8. CoroutineScope 中, async 的await 也肯定是这样的.
        // 异常捕获了
        launch {
            try {
                var job = async {
                    delay(200)
                    throw  Exception()
                }
                job.await()
                println("111")
            } catch (e: Exception) {
                println("22 $e")
            }
        }


        // 试试在 SuperVerionScope中


        // 9. job.join() 只关心是否完成. 期间, launch , 子协程抛出异常, launch 的默认异常处理只是打印堆栈, 不会通知父, 因此父无法捕获
        // 子协程执行完了, 会继续执行后面的111
        supervisorScope {
            try {
                val job = launch {
                    delay(200)
                    throw  Exception()
                }

                job.join()
                println("111")
            } catch (e: Exception) {
                println("22 $e")
            }
        }

        // 10. job.join() 只关心是否完成. 期间, async子协程抛出异常, (准确说是不会抛出?), 要等await的时候才抛出?
        // 反正 job 执行完了, 后面继续执行

        supervisorScope {
            try {
                val job = async {
                    delay(200)
                    throw  Exception()
                }

                job.join()
                println("111")
            } catch (e: Exception) {
                println("22 $e")
            }
        }

        // 11. 印证了10的问题.  在 supervisorScope  中的 async 抛出异常, 就是要等await的时候才抛出
        // println("aas") 正常打印
        // job.await() 会出现异常, 然后被捕获, 111不打印
        supervisorScope {
            try {
                val job = async {
                    delay(200)
                    throw  Exception()
                }
                println("aas")
                job.await()
                println("111")
            } catch (e: Exception) {
                println("22 $e")
            }
        }
    }
}