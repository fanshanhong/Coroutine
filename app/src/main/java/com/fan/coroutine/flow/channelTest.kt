package com.fan.coroutine.flow

import kotlinx.coroutines.*

/**
 * @Description:
 * @Author: shanhongfan
 * @Date: 2021/3/17 13:53
 * @Modify:
 */

@ExperimentalCoroutinesApi
fun main() {


//    Channel<Int>(5)

//    runBlocking {
//        val channel = Channel<Int>(3)
//
//        val producer = launch {
//            listOf<Int>(1, 2, 3, 4, 5).forEach {
//                channel.send(it)
//                println("send $it")
//            }
//            channel.close()
//            println("close channel. ClosedForSend = ${channel.isClosedForSend} ClosedForReceive = ${channel.isClosedForReceive}")
//        }
//
//        val consumer = launch {
//            for (element in channel) {
//                println("receive: $element")
//                delay(1000)
//            }
//
//            println("After Consuming. ClosedForSend = ${channel.isClosedForSend} ClosedForReceive = ${channel.isClosedForReceive}")
//        }
//    }

    // 作为一种热流，即使没有被监听，渠道（Channel）也会推送数据。如果我们不监听渠道（Channel），那么我们将丢失它产生的所有数据。

    // 即使没有被监听，热流（Hot Stream）也会推送数据。
    // 但是，冷流（Cold Stream）只会在我们开始收集时才开始推送数据。Kotlin流程是基于Kotlin协程的冷流（Cold Stream）的实现。


}