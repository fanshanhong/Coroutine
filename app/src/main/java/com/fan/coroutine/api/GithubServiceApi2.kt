package com.fan.coroutine.api
import com.fan.coroutine.User
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Path
/**
 * @Description:
 * @Author: shanhongfan
 * @Date: 2021/3/15 10:47
 * @Modify:
 */
interface GithubServiceApi2 {

    // 方式的本质是让接口的方法返回一个协程的 Job：, 注意 Deferred 是 Job 的子接口。
    @GET("users/{login}")
    fun getUserAsync(@Path("login") login: String): Deferred<User>
}