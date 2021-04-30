package com.fan.coroutine.api

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.fan.coroutine.R
import com.fan.coroutine.androidscope.CoroutineActivity
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 将callback 封装成 Coroutine
 */


// 1. 定义一个回调
interface Callback<T> {
    fun onSuccess(value: T)

    fun onError(t: Throwable)
}

class CallbackCoroutine : AppCompatActivity() {
    override fun onStop() {
        super.onStop()
        println("=======CallbackCoroutine onStop")
    }
    override fun onDestroy() {
        super.onDestroy()
        println("=======CallbackCoroutine onDestroy")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_callback_coroutine)

        findViewById<View>(R.id.callback).setOnClickListener {
            startActivity(Intent(this@CallbackCoroutine, CoroutineActivity::class.java))

        }


        // 调用getUserCoroutine()方法, 向 suspendCoroutine  传入的就是 launch 代码块里的内容.
        // 因此, 执行逻辑就是:
        // 1. 启动协程 GlobalScope.launch
        // 2. 在协程中执行 suspendCoroutine 或者 suspendCancellableCoroutine, 传入continuation参数的内容就是launch代码块本身(其实是launch代码块中 getUserCoroutine 之后要执行代码)
        // 3. suspendCoroutine 中请求网络, 成功, 回调 Callback 的 onSuccess, 进而执行continuation.resume(value), 其实就是执行传入的代码块中的正常流程
        // 4. 网络请求失败, 回调 Callback的 onError, 进而执行 continuation.resumeWithException(t), 其实就是执行传入的代码块中的异常流程了

        /// // 挂起函数或挂起 lambda 表达式调用时，都有一个隐式的参数额外传入，这个参数是Continuation类型，封装了协程恢复后的执行的代码逻辑。



        // 4. 启动协程, 使用 suspendCoroutine
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val userStr = getUserCoroutine()
                println("===$userStr")
            } catch (e: Exception) {
                println("Get User Error: $e")
            }
        }
    }

    // 3. 编写 suspendCoroutine 或者 suspendCancellableCoroutine
    // 传入参数是: continuation 后续计算
    // 在这里调用网络请求的方法 , 并传入正确的 回调

    // 挂起函数或挂起 lambda 表达式调用时，都有一个隐式的参数额外传入，这个参数是Continuation类型，封装了协程恢复后的执行的代码逻辑。
    suspend fun getUserCoroutine() = suspendCoroutine<String> { continuation ->
        getUser(object : Callback<String> {
            override fun onSuccess(value: String) {
                // 如果网络请求正常, 调用正常的方法
                continuation.resume(value)

                // 这里  resume 代表的就是  launch代码块中, getUserCoroutine() 方法之后的操作: 赋值, 打印 两个操作
            }

            override fun onError(t: Throwable) {
                // 如果网络请求异常, 调用异常的方法
                continuation.resumeWithException(t)

                // 这里  resumeWithException 代表的就是  launch代码块中, getUserCoroutine() 方法之后catch的操作
            }
        })
    }


    // 2. 正常的网络请求, 传入回调
    fun getUser(callback: Callback<String>) {
        val call = OkHttpClient().newCall(
            Request.Builder()
                .get().url("https://api.github.com/users/bennyhuo")
                .build()
        )

        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 失败调用回调方法
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body()?.let {
                    try {
                        // 成功调用回调方法
                        callback.onSuccess(it.string())
                    } catch (e: Exception) {
                        callback.onError(e) // 这里可能是解析异常
                    }
                } ?: callback.onError(NullPointerException("ResponseBody is null."))
            }
        })
    }



    // 协程内部实现不是使用普通回调的形式，而是使用状态机来处理不同的挂起点，
    // // 编译后生成的内部类大致如下
    //final class postItem$1 extends SuspendLambda ... {
    //    public final Object invokeSuspend(Object result) {
    //        ...
    //        switch (this.label) {
    //            case 0:
    //                this.label = 1;
    //                token = requestToken(this)
    //                break;
    //            case 1:
    //                this.label = 2;
    //                Token token = result;
    //                post = createPost(token, this.item, this)
    //                break;
    //            case 2:
    //                Post post = result;
    //                processPost(post)
    //                break;
    //        }
    //    }
    //}
    // 上面代码中每一个挂起点和初始挂起点对应的 Continuation 都会转化为一种状态，协程恢复只是跳转到下一种状态中。
    // 挂起函数将执行过程分为多个 Continuation 片段，并且利用状态机的方式保证各个片段是顺序执行的。
    //
}



// // 挂起函数或挂起 lambda 表达式调用时，都有一个隐式的参数额外传入，这个参数是Continuation类型，封装了协程恢复后的执行的代码逻辑。
// 实际上在 JVM 中更像下面这样：
//
//Object getToken(Continuation<Token> cont) { ... }

suspend fun getToken(): String {
    suspendCoroutine<String> {continuation: Continuation<String> ->  }
    // delay 也是一个 suspend 函数
    Log.d(
        "AA",
        "before delay getToken 开始执行，时间:  ${System.currentTimeMillis()}  线程：${Thread.currentThread().name} "
    )
    delay(300)
    Log.d(
        "AA",
        "getToken 开始执行，时间:  ${System.currentTimeMillis()}  线程：${Thread.currentThread().name} "
    )
    return "ask"
}
