package com.fan.coroutine.androidscope

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class ScopedActivity: AppCompatActivity(), CoroutineScope by MainScope(){
    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}