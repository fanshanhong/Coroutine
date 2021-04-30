package com.fan.coroutine.api

import com.fan.coroutine.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubServiceApi {
    @GET("users/{login}")
    fun getUser(@Path("login") login: String): Call<User>
}

