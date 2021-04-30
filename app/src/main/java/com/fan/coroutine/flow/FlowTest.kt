package com.fan.coroutine.flow

import com.fan.coroutine.test.scopeTest
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import kotlin.system.measureTimeMillis

/**
 * @Description: 演示 Kotlin Coroutines Flow 相关使用
 * @Author: shanhongfan
 * @Date: 2021/4/29 16:58
 * @Modify:
 */

/**
 * 参考: https://www.raywenderlich.com/9799571-kotlin-flow-for-android-getting-started#toc-anchor-007
 */
fun main(args: Array<String>) {

    runBlocking {

        // 1. 异步返回多个值的第一种方式  suspend方法
        // suspending functions can return a single value asynchronously.
        // This function computes values and adds those values into a List. delay() simulates a long-running operation, like you would have using a remote API.
        suspend fun getValues(): List<Int> {
            delay(1000)
            return listOf(1, 2, 3)
        }

        // 处理上面产生的值
        val values = getValues()
        for (value in values) {
            println(value)
        }


        // 上面的例子, 是一次性返回 3 个值, 然后一次性处理
        // 如果要产生 1000 个值,每产生一个值花费 1 秒,那就要花费很多时间来产生值,然后再一次性处理.
        // 能不能产生一个就处理一个?

        // 2. 使用 sequence
        // this time you won’t have to wait for all the values. You’ll produce and consume them, one at a time:

        // 这里创建 sequence, 但是并不会发射值, 只有等收集使用的时候才发射
        val sequence = sequence {
            Thread.sleep(250)
            println("A")
            yield(1)

            Thread.sleep(250)
            println("B")
            yield(2)


            println("Done")
        }

        // 虽然这里睡眠,但是 sequence 依然不会发射值
        Thread.sleep(2000)
        println("before sequence")

        // 必须等这里开始使用的时候, 才一次发射一个值
        // 也就是 sequence 中每个 yield, 都是要等有人用才发射呢.
        // 比如, 我这里只使用第一个值, 不使用第二个, 第二个应该就不会发射出来.  (确实是这样)
        for (item in sequence) {
            println("Got $item")
            break
        }

        // sequence 是冷流, 只能当取的时候(访问值,或者调用终止操作符的时候), 才发射值.


        // 3. channel 是热流
        // A channel, which is a hot stream, will produce values even if aren’t listening to them on the other side.
        // And if you are not listening to the stream, you are losing values.


        // Hot streams push values even when there is no one consuming(消耗) them. However, cold streams, start pushing values only when you start collecting!
        //
        // And Kotlin Flow is an implementation of cold streams, powered by Kotlin Coroutines!


        // 4. flow 的创建   flowOf
        flowOf(1, 2, 3, 4, 5)
            .onEach {
                delay(100)
            }
            .collect {
                println(it)
            }

        // flowOf 的实现如下:
        // public fun <T> flowOf(vararg elements: T): Flow<T> = flow {
        //    for (element in elements) {
        //        emit(element)
        //    }
        //}


        // flow的创建 : asFlow
        listOf(1, 2, 3, 4, 5).asFlow()
            .onEach {
                delay(100)
            }.collect {
                println(it)
            }


        //  channelFlow builder 跟 flow builder 是有一定差异的。
        //
        //flow 是 Cold Stream。在没有切换线程的情况下，生产者和消费者是同步非阻塞的。
        //channel 是 Hot Stream。而 channelFlow 实现了生产者和消费者异步非阻塞模型。
        channelFlow {
            for (i in 1..5) {
                delay(100)
                send(i)
            }
        }.collect {
            println(it)
        }


        // 观察调用顺序和时间
        // emit 一次, 进一次 collect
        // 在 collect 里不执行东西也没问题, 但是不调用 collect, 就不发射
        println("======================")
        val time1 = measureTimeMillis {
            flow {
                for (i in 1..5) {
                    println(" in for, before delay , $i")
                    delay(1000)
                    println(" in for, after delay , $i")
                    emit(i)
                }
            }.collect {
                println(" in collect, before delay , $it")
                delay(1000)
                println(" in collect, after delay , $it")
                println(it)
            }
        }
        print("cost $time1") //
        println("======================")

        // 只有调用了 (终止运算符)collect, 才会执行 flow 中的 emit
        // 但是, 并不是说 collect 中带代码会比flow 里的先执行. 执行还是 flow 里的先执行, 先  emit , 再  收到


        val time2 = measureTimeMillis {
            channelFlow {
                for (i in 1..5) {
                    println("channelflow1 $i  ${System.currentTimeMillis()} ${Thread.currentThread().name}")
                    delay(1000)
                    println("channelflow2 $i  ${System.currentTimeMillis()}  ${Thread.currentThread().name}")
                    send(i)
                }
            }.collect {
                println("collect1 $it  ${System.currentTimeMillis()} ${Thread.currentThread().name}")
                delay(1000)
                println("collect2 $it  ${System.currentTimeMillis()}  ${Thread.currentThread().name}")
                println(it)
            }
        }

        print("cost $time2") // 6197
        // channelFlow 和 collect 是相同的线程 上面都是main
        // 但是不会互相等待, 速度快一点


        // 6. 使用 flowOn 切换线程

        println("AAAAAAAAAAAAAAAAAAAAAAA")
        flow {
            for (i in 1..5) {
                println(" in flow ${Thread.currentThread().name}") // flow builder 和 map 操作符都会受到 flowOn 的影响。 运行在IO
                emit(i)
            }
        }.map {
            println(" in map ${Thread.currentThread().name}") // flow builder 和 map 操作符都会受到 flowOn 的影响。
            it * it
        }
            .flowOn(Dispatchers.IO)
            .collect {
                println(" in collect ${Thread.currentThread().name}") // collect 应该是在main, 因为 runBlocking的原因
                println(it)
            }
        // 而 collect() 指定哪个线程，则需要看整个 flow 处于哪个 CoroutineScope 下。
        println("AAAAAAAAAAAAAAAAAAAAAAA")

        // 比如下面这个代码, 指定 collect 在 Default
        launch(Dispatchers.Default) {
            flow<String> {
                emit("哈哈哈")
            }.flowOn(Dispatchers.Main)
                .collect {
                    println("测试 collect 运行在 Default 线程")
                }
        }


        // 值得注意的地方，不要使用 withContext() 来切换 flow 的线程。


        // 如果 flow 是在一个挂起函数内被挂起了，那么 flow 是可以被取消的，否则不能取消。
        // 超过 2500ms 就取消. 这里应该也是设置取消的状态, 必有有 delay 等挂起方法检测到被取消了, 才取消
        launch {
            withTimeoutOrNull(2500) {
                flow {
                    for (i in 1..5) {
                        delay(1000)
                        emit(i)
                    }
                }.collect {
                    println(it)
                }
            }
        }


        // 7.终止操作符
        //  // 整理一下 Flow 的 Terminal 运算符
        //        //
        //        //collect
        //        //single/first
        //        //toList/toSet/toCollection
        //        //count
        //        //fold/reduce
        //        //launchIn/produceIn/broadcastIn
        //        //


        // 8. Flow 完成时（正常或出现异常时），如果需要执行一个操作，它可以通过两种方式完成：imperative、declarative。

        // 8.1  imperative
        // 通过使用 try ... finally 实现
        //    try {
        //        flow {
        //            for (i in 1..5) {
        //                delay(100)
        //                emit(i)
        //            }
        //        }.collect { println(it) }
        //    } finally {
        //        println("Done")
        //    }


        // 8.2  declarative
        // 通过 onCompletion() 函数实现
        flow {
            for (i in 1..5) {
                delay(100)
                emit(i)
                println("in for, after emit $i")
            }
        }.onCompletion { println("Done") } // 当所有的都执行完了, 就回调这个
            .collect {
                println("in collect, before print $it")
                println(it)
            }

        // 但是 onCompletion 不能捕获异常，只能用于判断是否有异常。他无法 catch 住异常,该上报还是要上报
        // 像下面这样
        flow {
            emit(1)
            throw RuntimeException()
        }.onCompletion { cause ->
            if (cause != null)
                println("Flow completed exceptionally")
            else
                println("Done")
        }.collect { println(it) }


        // 9.  buffer 实现并发操作

        var start: Long = 0
        (1..5)
            .asFlow()
            .onStart {
                println("enter onStart")
                start = System.currentTimeMillis()
            }
            .onEach {
                delay(100)
                println("Emit $it (${System.currentTimeMillis() - start}ms) ")
            }
            .buffer()// 这里
            .collect {
                println("Collect $it starts (${System.currentTimeMillis() - start}ms) ")
                delay(500)
                println("Collect $it ends (${System.currentTimeMillis() - start}ms) ")
            }


        //catch 操作符可以捕获来自上游的异常


        flow {
            emit(1)
            throw RuntimeException()
        }
            .onCompletion { cause ->
                if (cause != null)
                    println("Flow completed exceptionally")
                else
                    println("Done")
            }
            .catch { println("catch exception") }
            .collect { println(it) }


        // 上面的代码如果把 onCompletion、catch 交换一下位置，则 catch 操作符捕获到异常后，不会影响到下游。因此，onCompletion 操作符不再打印"Flow completed exceptionally"
        //


        println("11111111111111111111111")
        // RxJava 可以借助 flatMap 操作符实现并行，
        // Flow 也有相应的操作符 flatMapMerge 可以实现并行。
        val result = arrayListOf<Int>()
        for (index in 1..100) {
            result.add(index)
        }

        flow {
            for (i in 1..100) {
                println("first emit $i")
                emit(i)
            }
        }.flatMapMerge {
            flow {
                println("second emit $it")
                emit(it)
            }.flowOn(Dispatchers.IO)
        }.collect {
            println(it)
        }
        println("11111111111111111111111")

        val startTime = System.currentTimeMillis()
           //  flatMapConcat 他们等待内部流程完成，然后开始收集下一个示例，如以下示例所示：
            (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
                .flatMapConcat { requestFlow(it) }
                .collect { value -> // collect and print
                    println("$value at ${System.currentTimeMillis() - startTime} ms from start")
                }

            // 是同时收集所有传入流并将其值合并为单个流
            // 请注意，flatMapMerge顺序调用其代码块（在此示例中为*{ requestFlow(it) }），但同时并发收集结果流
            (1..3).asFlow().onEach { delay(100) } // a number every 100 ms
                .flatMapMerge { requestFlow(it) }
                .collect { value -> // collect and print
                    println("$value at ${System.currentTimeMillis() - startTime} ms from start")
                }

        runBlocking {
//            val sum = (1..5).asFlow()
//                .map { it * it }
//                .reduce { a, b ->
//
//                    println("===a=$a b=$b")
//                    a + b }
//
//            println(sum)
            // 03-17 17:44:43.486 17627-17627/com.fan.coroutine I/System.out: ===a=1 b=4
            //03-17 17:44:43.486 17627-17627/com.fan.coroutine I/System.out: ===a=5 b=9
            //03-17 17:44:43.486 17627-17627/com.fan.coroutine I/System.out: ===a=14 b=16
            //03-17 17:44:43.486 17627-17627/com.fan.coroutine I/System.out: ===a=30 b=25

            // 第一次进入 reduce, 就是第一个值的map返回值 作为a, 第二个值map方法返回值作为b
            // 然后a+b作为a, 第三个 map的返回值作为b


//            val sum = (1..5).asFlow()
//                .map { it * it }
//                .fold(0) { a, b ->
//                    println("===a=$a b=$b")
//                    a + b }
//
//            println(sum)


//            val start = System.currentTimeMillis()
//            val flowA = (1..5).asFlow().onEach {
//                println("flowA $it  ${System.currentTimeMillis()-start} ms")
//                delay(100)  }
//            val flowB = flowOf("one", "two", "three","four","five").onEach {
//                println("flowB $it  ${System.currentTimeMillis()-start} ms")
//                delay(200)  }
//            flowA.combine(flowB) { a, b -> "$a and $b" }
//                .collect { println(it) }

//            flow {
//                println("===emit")
//                emit(1)
//                throw RuntimeException()
//            }.map{
//                println("===first map")
//            }
//                .onEach{
//                println("===on Each")
//            }.map{
//                println("===map")
//            }
//                .onCompletion { cause ->
//
//                    println("===onCompletion")
//
//                    if (cause != null)
//                        println("===Flow completed exceptionally")
//                    else
//                        println("===Done")
//                }
//                .catch{ println("===catch exception") }
//                .collect {
//                    println("===collect $it") }

//            (1..3).asFlow().onEach { value ->
//                if (value <= 1) {
//                    throw  Exception("asda")
//                }
//                println("Got $value")
//            }.catch { e ->
//                println("Caught $e")
//            }.onCompletion {e->
//                if(e != null) {
//                    println(e.message)
//                } else {
//
//                }
//                println("Done")
//            }.collect()


//            (1..3).asFlow().onEach { value ->
//                if (value <= 1) {
//                    throw  Exception("asda")
//                }
//                println("Got $value")
//            }.onCompletion {e->
//                if(e != null) {
//                    println(e.message)
//                } else {
//
//                }
//                println("Done")
//            }.catch { e ->
//                println("Caught $e")
//            }.collect()


            // 观察调用顺序
            (1..3).asFlow().onEach { value ->
                if (value <= 1) {
                    println("---- $value")
//                    throw Exception()
                }
                println("Got $value")
            }.onCompletion { cause ->
                println("Done")
                if (cause != null) {
                    println("oncompletion cause != null : $cause")
                }
                throw IllegalStateException("Throw during onCompletion")
            }.catch { e ->
                println("Caught $e")
            }.map {
                println("middle map")
            }.onEach {
                println("middle oneach")
            }.onCompletion {
                println("Done again :P")
            }.onEach {
                println("the end oneach")

            }.collect()

            val flowA = (1..5).asFlow()
            val flowB = flowOf("one", "two", "three", "four", "five")

            flowOf(flowA, flowB)
                .flattenConcat()
                .collect { println(it) }

            flowOf(flowA, flowB).flattenMerge(2)


        }


        CoroutineScope(Dispatchers.Unconfined).launch {
            val broadcastChannel = BroadcastChannel<Int>(5)
            println("=========12131")

            val producer = launch {
                delay(1000)
                List(5) {
                    broadcastChannel.send(it)
                    println("send $it")
                }
                broadcastChannel.close()
            }
            List(3) { index ->
                launch {
                    // 订阅broadcast, 返回 ReceiveChannel
                    val receiveChannel = broadcastChannel.openSubscription()
                    for (element in receiveChannel) {
                        println("[$index] receive: $element")
                        delay(1000)
                    }
                }
            }.forEach { it.join() }
            producer.join()
        }
    }
}

fun requestFlow(i: Int): Flow<String> = flow {
    emit("$i: First")
    delay(500) // wait 500 ms
    emit("$i: Second")
}
