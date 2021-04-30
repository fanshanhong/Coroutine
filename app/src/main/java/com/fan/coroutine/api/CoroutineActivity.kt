package com.fan.coroutine.api

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.fan.coroutine.R
import com.fan.coroutine.User
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.coroutines.*

/**
 * 划掉
 *
2021-03-26 13:35:25.288 2945-2945/com.fan.coroutine I/System.out: =======MainActivity onStop
2021-03-26 13:35:26.345 2945-2945/com.fan.coroutine I/System.out: =======CoroutineActivity onStop
2021-03-26 13:35:33.229 2945-2945/com.fan.coroutine I/System.out: =======CallbackCoroutine onStop
2021-03-26 13:35:41.869 2945-2945/com.fan.coroutine I/System.out: =======MainActivity onDestroy



 * 清理全部clear all
2021-03-26 13:36:44.357 3195-3195/com.fan.coroutine I/System.out: =======MainActivity onStop
2021-03-26 13:36:45.634 3195-3195/com.fan.coroutine I/System.out: =======CoroutineActivity onStop
2021-03-26 13:36:47.804 3195-3195/com.fan.coroutine I/System.out: =======CallbackCoroutine onStop
2021-03-26 13:36:53.449 3195-3195/com.fan.coroutine I/System.out: =======MainActivity onDestroy
2021-03-26 13:36:53.474 3195-3195/com.fan.coroutine I/System.out: =======CoroutineActivity onDestroy


2021-03-26 13:43:33.471 3953-3953/com.fan.coroutine I/System.out: =======MainActivity onStop
2021-03-26 13:43:37.786 3953-3953/com.fan.coroutine I/System.out: =======CoroutineActivity onStop
2021-03-26 13:43:48.201 3953-3953/com.fan.coroutine I/System.out: =======CallbackCoroutine onStop
2021-03-26 13:43:54.198 3953-3953/com.fan.coroutine I/System.out: =======MainActivity onDestroy
 */


/**
 * 演示 Coroutine 进行网络请求
 */
class CoroutineActivity : AppCompatActivity() {
    override fun onDestroy() {
        super.onDestroy()
        println("=======CoroutineActivity onDestroy")
    }

    override fun onStop() {
        super.onStop()
        println("=======CoroutineActivity onStop")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)


        findViewById<TextView>(R.id.to_coroutine_activity).setOnClickListener {
            startActivity(Intent(this@CoroutineActivity, CallbackCoroutine::class.java))
        }

        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()


        // 1. 使用 callback 的 retrofit 网络请求
        // 调用是在main线程, 真正的网络请求在子线程执行, onFailure 和 onResponse 回调都切换回到了 main线程
        val gitHubServiceApi = retrofit.create(GitHubServiceApi::class.java)

        gitHubServiceApi.getUser("bennyhuo").enqueue(object : retrofit2.Callback<User> {
            override fun onFailure(call: Call<User>, t: Throwable) {
                println("gitHubServiceApi.getUser:onFailure currentThreadName:  ${Thread.currentThread().name}   ${t.message}")// main线程
            }

            override fun onResponse(call: Call<User>, response: Response<User>) {
                println("gitHubServiceApi.getUser:onResponse currentThreadName:  ${Thread.currentThread().name}") // main线程
                println(response.body())
            }
        })


        // 2. 使用 协程的 retrofit
        val gitHubServiceApi2 = retrofit.create(GithubServiceApi2::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            // 指定launch代码块在主线程执行
            try {
                println(" GlobalScope.launch(Dispatchers.Main):  ${Thread.currentThread().name}") // main线程

                // getUserAsync 内部是在子线程发起请求
                val result = gitHubServiceApi2.getUserAsync("bennyhuo").await()

                // 请求完成后,在主线程恢复
                println("result: $result")

                println("after gitHubServiceApi2.getUser : ${Thread.currentThread().name}") // main线程

            } catch (e: Exception) {
                println(" GlobalScope.launch(Dispatchers.Main) error:  ${Thread.currentThread().name}") // main线程
            }
        }

        // 3. launch 是异步, 因此下面这个会先执行
        println("test launch async")


        // 4. 通过 OkHttp 完成网络请求
        testOkHttp()

        // 5. 使用suspend的方式
        val gitHubServiceApi3 = retrofit.create(GithubServiceApi3::class.java)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                println("gitHubServiceApi3 ${Thread.currentThread().name}")
                var user = gitHubServiceApi3.getUserAsync("bennyhuo")
                println("gitHubServiceApi3 user: $user  ${Thread.currentThread().name}")
            } catch (e: Exception) {
                println("gitHubServiceApi3 error: $e")
            }
        }


        // 5. 几种启动的方式
        //DEFAULT	立即执行协程体
        //ATOMIC	立即执行协程体，但在开始运行之前无法取消
        //UNDISPATCHED	立即在当前线程执行协程体，直到第一个 suspend 调用
        //LAZY	只有在需要的情况下运行

        runBlocking {
            var job = GlobalScope.launch(start = CoroutineStart.ATOMIC) {
                println("111")
                delay(1000)
                println("222")
            }

            job.cancel()
            delay(3000)
            job.start()

        }


        // 6. 自己创建的线程池作为协程的线程调度器
        val myDispatcher =
            Executors.newSingleThreadExecutor { r -> Thread(r, "MyThread") }.asCoroutineDispatcher()

        myDispatcher.use {
            // 这个还是 main
            println("==== my thread is:" + Thread.currentThread().name)

            // 这个必须在 suspend 或者 协程中使用, launch 和 async 也一样
            //            coroutineScope {
            //                launch {  }
            //                async {  }
            //            }

            //            withContext()

            // 这两个可以用
            runBlocking { }

            GlobalScope.launch {}

            // 应该这样写:
            CoroutineScope(myDispatcher).launch { }

            // 与下面这两个都是效果一样
            // coroutineScope {  }
            // launch

            // supervisorScope {  }


        }

        runBlocking {
            launch(myDispatcher) {
                println("==== my thread is:myDispatcher" + Thread.currentThread().name) // 这个就是默认线程池了
            }
        }
    }


    /**
     * 使用 OkHttp 完成网络请求
     */
    private fun testOkHttp() {

        val okHttpClient = OkHttpClient()
        val request = Request.Builder().url("https://api.github.com/users/bennyhuo").build()
        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                println(" testOkHttp onFail ${Thread.currentThread().name}") // 子线程, 以url命名的thread
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                println("testOkHttp onResponse ${Thread.currentThread().name}")// 子线程, 以url命名的thread
            }
        })
    }


    fun <T> coroutineDo(block: suspend () -> T) {
        block.startCoroutine(object : Continuation<T> {                             // 创建并启动协程
            override val context: CoroutineContext =
                EmptyCoroutineContext          // 协程上下文，如不作处理使用EmptyCoroutineContext即可

            override fun resumeWith(result: Result<T>) {                            // 协程结果统一处理
            }
        })
    }

}

// @SinceKotlin("1.1")
// public interface Continuation<in T> {
//    public val context: CoroutineContext
//    public fun resume(value: T)
//    public fun resumeWithException(exception: Throwable)
//}

// await 方法可以这样理解
// 1. 在await()方法中, 比如, 指定了调度器是Dispatchers.Main, 此时还是在 Main线程中, 把任务enqueue
// 2. 然后任务会在对应的线程中得到执行. 比如 retrofit 的请求, 就在子线程执行. 如果在 GlobalScope.launch 中又启动 协程, 并指定了调度器,那就在对应的线程执行了
// 3. 任务执行完之后, 回调 Continuation的方法.如果正常, 就执行 resume(恢复), 否则就是 Exception
// 4. cont.resume(response.body()!!)会把结果返回, 切回 Main 线程, 然后 await() 方法的返回值就能拿到了.(此时是在Main线程中)
//
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

// //注意以下并不是真实的实现，仅供大家理解协程使用
// fun await(continuation: Continuation<User>): Any {
//    ... // 切到非 UI 线程中执行，等待结果返回
//    try {
//        val user = ...
//        handler.post{ continuation.resume(user) }
//    } catch(e: Exception) {
//        handler.post{ continuation.resumeWithException(e) }
//    }
//}