package com.fan.coroutine.flow

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * @Description:
 * @Author: shanhongfan
 * @Date: 2021/3/18 17:49
 * @Modify:
 */


fun main() {

    runBlocking {

        // flow 操作符


        // 1. map
        // map里不能发射(emit) , 在map里写flow 也不太行

//        (1..3).asFlow()
//            .map {
//                flow {
//                    emit(it)
//                }
//            }
//            .collect { println(it) }


        // 2. transform: 在使用 transform 操作符时，可以任意多次调用 emit ，这是 transform 跟 map 最大的区别：
//        (1..3).asFlow()
//            .transform {
//                emit(it * 2)
//                delay(100)
//                emit(it * 100)
//            }
//            .collect { println(it) }


        // 2. onEach
        // 3. zip
        // 4. combine
        // 5. flattenConcat  (两个flow, 串行跑)
//        val flowA = (1..5).asFlow()
//        val flowB = flowOf("one", "two", "three", "four", "five")

//        flowOf(flowA, flowB)
//            .flattenConcat()
//            .collect { println(it) }

        // 6. flattenMerge (两个flow, 并行跑)

//        val flowA = (1..5).asFlow().onEach { delay(100) }
//        val flowB = flowOf("one", "two", "three", "four", "five").onEach { delay(200) }
//        flowOf(flowA, flowB)
//            .flattenMerge(2)
//            .collect { println(it) }

        // 7. flatMapConcat
        // 在调用 flatMapConcat 后，collect 函数在收集新值之前会等待 flatMapConcat 内部的 flow 完成。
//        var start1: Long = 0
//        (1..5).asFlow()
//            .onStart { start1 = System.currentTimeMillis()  }
//            .onEach { delay(100) }
//                // flatMapConcat 的参数: transform: suspend (value: T) -> Flow<R>
//                // 比如传入一个方法或者 lambda, 入参是value, 返回值是 Flow 对象
//            .flatMapConcat {
//                flow {
//                    emit("$it: First")
//                    delay(500)
//                    emit("$it: Second")
//                }
//            }
//            .collect {
//                println("$it at ${System.currentTimeMillis() - start1} ms from start")
//            }


        // 8. flatMapMerge
        //    flatMapMerge 是顺序调用内部代码块，并且并行地执行 collect 函数。也就是说不等待 flatMapMerge里的flow执行完, 就开始执行collect了
//        var start: Long = 0
//        (1..5).asFlow()
//            .onStart { start = System.currentTimeMillis() }
//            .onEach { delay(100) }
//            .flatMapMerge {
//                flow {
//                    emit("$it: First")
//                    delay(500)
//                    emit("$it: Second")
//                }
//            }
//            .collect {
//                println("$it at ${System.currentTimeMillis() - start} ms from start")
//            }


        // 9. flatMapLatest
        // 当发射了新值之后，上个 flow 就会被取消。
//        var start3:Long  =0;
//        (1..5).asFlow()
//            .onStart { start3 = System.currentTimeMillis() }
//            .onEach { delay(100) }
//            .flatMapLatest {
//                flow {
//                    emit("$it: First")
//                    delay(500)
//                    emit("$it: Second")
//                }
//            }
//            .collect {
//                println("$it at ${System.currentTimeMillis() - start3} ms from start")
//            }

        // 9. onCompletion
        // 10. catch
        // 11. collect


        // 12. 线程变换 flowOn

        // 13. buffer 实现并发
        // 不带buffer, 就是正常的,一次一次来, 串行
//        var start: Long = System.currentTimeMillis()
        //val time = measureTimeMillis {
//        flow {
//            for (i in 1..5) {
//                println("before delay $i ${System.currentTimeMillis() - start}")
//                delay(1000)
//                println("before emit $i  ${System.currentTimeMillis() - start}")
//                emit(i)
//            }
//        }
//            .buffer()
//            .collect { value ->
//                println("collect before delay  $value  ${System.currentTimeMillis() - start}")
//                delay(3000)
//                println("collect after delay  $value  ${System.currentTimeMillis() - start}")
//                println(value)
//            }
        // }
        //println("Collected in $time ms")

        // 不带 buffer, 就是串行的, 全部执行需要 20秒+
        // 带 buffer, 并发, 全部执行需要 16秒+
        // 执行过程

        // 时间 0------1--------2-------3-------4------5-----6------7------8------9------10-----11-----12------13------14----15---16
        //            发1      发2      发3     发4    发5
        //            收到1                    打印1               打印2                   打印3                 打印4               打印5
        //                                    (收到2)              (收到3)                 (收到4)               (收到5)


        // 14. 并行  flatMapMerge, 指定线程
//        var start: Long = System.currentTimeMillis()
//        flow {
//            for (i in (1..5)) {
//                println("in for $i ${System.currentTimeMillis() - start}  ${Thread.currentThread().name}")
//                emit(i)
//            }
//        }.flatMapMerge {
//            flow {
//                println("before delay $it ${System.currentTimeMillis() - start}  ${Thread.currentThread().name}")
//                delay(1000)
//                println("before emit $it  ${System.currentTimeMillis() - start}   ${Thread.currentThread().name}")
//                emit(it)
//            }.flowOn(Dispatchers.IO)
//        }
//            .collect {
//                println("collect before delay  $it  ${System.currentTimeMillis() - start} ${Thread.currentThread().name}")
//                delay(3000)
//                println("collect after delay  $it  ${System.currentTimeMillis() - start}  ${Thread.currentThread().name}")
//                println(it)
//            }
        // 并行, 16秒+  16100
        // 说明:  flowOn 只给 flatMapMerge指定了,因此 flatMapMerge是运行在IO线程的

        // 在 30毫秒左右, 在main线程全部发射
        //in for 1 33  main
        //in for 2 41  main
        //in for 3 42  main
        //in for 4 42  main
        //in for 5 42  main


        //在第100毫秒的时候,  进入flatMapMerge, 是运行在IO线程池的, 5个值,就开了5个线程,分别在各自的线程睡1秒,1秒醒来的时候,全部发射
        //before delay 1 60  DefaultDispatcher-worker-1
        //before delay 2 60  DefaultDispatcher-worker-2
        //before delay 3 60  DefaultDispatcher-worker-3
        //before delay 4 61  DefaultDispatcher-worker-4
        //before delay 5 61  DefaultDispatcher-worker-5
        //before emit 2  1064   DefaultDispatcher-worker-1
        //before emit 1  1064   DefaultDispatcher-worker-2
        //before emit 4  1064   DefaultDispatcher-worker-4
        //before emit 5  1064   DefaultDispatcher-worker-5
        //before emit 3  1064   DefaultDispatcher-worker-3


        //发射后, 大约过了20毫秒, 立马就收到, 然后在main上每3秒打印一下
        //collect before delay  2  1081 main
        //collect after delay  2  4087  main
        //2
        //collect before delay  4  4087 main
        //collect after delay  4  7088  main
        //4
        //collect before delay  5  7088 main
        //collect after delay  5  10092  main
        //5
        //collect before delay  3  10093 main
        //collect after delay  3  13096  main
        //3
        //collect before delay  1  13097 main
        //collect after delay  1  16100  main
        //1

        // 现在, 瓶颈是在 collect上, 不知道能不能给collect指定在线程池上执行, 这样就快了

        // flowOn 简单点理解就是flowOn之前的操作符运行在flowOn指定的线程之内，flowOn之后的操作符运行在整个flow运行的CoroutineContext内。
        // 试试

        withContext(Dispatchers.Unconfined) {
            var start: Long = System.currentTimeMillis()
            flow {
                for (i in (1..5)) {
                    println("in for $i ${System.currentTimeMillis() - start}  ${Thread.currentThread().name}")
                    emit(i)
                }
            }.flatMapMerge {
                flow {
                    println("before delay $it ${System.currentTimeMillis() - start}  ${Thread.currentThread().name}")
                    delay(1000)
                    println("before emit $it  ${System.currentTimeMillis() - start}   ${Thread.currentThread().name}")
                    emit(it)
                }.flowOn(Dispatchers.IO)
            }
                .collect {
                    Thread{
                        println("collect before delay  $it  ${System.currentTimeMillis() - start} ${Thread.currentThread().name}")
                        Thread.sleep(3000)
                        println("collect after delay  $it  ${System.currentTimeMillis() - start}  ${Thread.currentThread().name}")
                        println(it)
                    }.start()
                }
        }

        // in for 1 23  DefaultDispatcher-worker-3
        //in for 2 33  DefaultDispatcher-worker-3
        //before delay 1 34  DefaultDispatcher-worker-1
        //in for 3 34  DefaultDispatcher-worker-3
        //in for 4 34  DefaultDispatcher-worker-3
        //before delay 2 34  DefaultDispatcher-worker-2
        //before delay 3 34  DefaultDispatcher-worker-5
        //in for 5 34  DefaultDispatcher-worker-3
        //before delay 4 35  DefaultDispatcher-worker-6
        //before delay 5 35  DefaultDispatcher-worker-7
        //before emit 3  1043   DefaultDispatcher-worker-5
        //before emit 2  1044   DefaultDispatcher-worker-2
        //before emit 1  1044   DefaultDispatcher-worker-6
        //before emit 5  1044   DefaultDispatcher-worker-7
        //before emit 4  1044   DefaultDispatcher-worker-9
        //collect before delay  3  1050 DefaultDispatcher-worker-10
        //collect after delay  3  4052  DefaultDispatcher-worker-6
        //3
        //collect before delay  2  4052 DefaultDispatcher-worker-6
        //collect after delay  2  7056  DefaultDispatcher-worker-6
        //2
        //collect before delay  1  7056 DefaultDispatcher-worker-6
        //collect after delay  1  10061  DefaultDispatcher-worker-6
        //1
        //collect before delay  5  10061 DefaultDispatcher-worker-6
        //collect after delay  5  13067  DefaultDispatcher-worker-6
        //5
        //collect before delay  4  13067 DefaultDispatcher-worker-6
        //collect after delay  4  16071  DefaultDispatcher-worker-6
        //4


        // collect 中使用线程是最快的.
        // in for 1 46  main
        //in for 2 60  main
        //in for 3 60  main
        //in for 4 60  main
        //in for 5 60  main
        //before delay 1 77  DefaultDispatcher-worker-4
        //before delay 3 77  DefaultDispatcher-worker-5
        //before delay 2 77  DefaultDispatcher-worker-1
        //before delay 4 77  DefaultDispatcher-worker-6
        //before delay 5 77  DefaultDispatcher-worker-2
        //before emit 1  1085   DefaultDispatcher-worker-4
        //before emit 2  1085   DefaultDispatcher-worker-1
        //before emit 5  1085   DefaultDispatcher-worker-6
        //before emit 3  1085   DefaultDispatcher-worker-5
        //before emit 4  1085   DefaultDispatcher-worker-2
        //collect before delay  5  1089 Thread-9
        //collect before delay  2  1089 Thread-10
        //collect before delay  1  1089 Thread-11
        //collect before delay  3  1089 Thread-12
        //collect before delay  4  1089 Thread-13

    }


}