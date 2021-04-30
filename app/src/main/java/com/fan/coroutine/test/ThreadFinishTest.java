package com.fan.coroutine.test;

/**
 * @Description: 测试, 如果有子线程未结束, 当前程序 是一直不结束的
 * 在 wait() 处打断点, 然后在 Frames 处查看, 可以看到有两个状态为  RUNNING 的线程, 并且 当前程序 一直不结束
 * @Author: shanhongfan
 * @Date: 2021/3/15 10:23
 * @Modify:
 */
class ThreadFinishTest {

    public static void main(String[] args) {

        System.out.println("测试开始");

        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread1.setName("子线程thread1");
        thread1.start();


        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread2.setName("子线程thread2");
        thread2.start();
    }

}
