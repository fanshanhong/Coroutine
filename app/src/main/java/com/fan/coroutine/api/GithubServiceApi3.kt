package com.fan.coroutine.api
import com.fan.coroutine.User
import retrofit2.http.GET
import retrofit2.http.Path
/**
 * @Description:
 * @Author: shanhongfan
 * @Date: 2021/3/15 10:47
 * @Modify:
 */
interface GithubServiceApi3 {

    // 使用suspend的方式。
    @GET("users/{login}")
    suspend fun getUserAsync(@Path("login") login: String): User
}