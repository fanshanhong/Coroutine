package com.fan.coroutine.androidscope;

import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.launch

class CoroutinePresenter(val scope: CoroutineScope) : CoroutineScope by scope {
    fun getUserData() {
        launch {
            println("打印啦111")
        }
    }
}