#【Android】锁屏后CPU休眠线程冻结现象观察 #

-------------------------------------------------------

`Android`手机为了省电，在锁屏后会尽可能`CPU`休眠，下面进行观测休眠对线程的影响 。

## 线程任务 ##

见代码[MainActivity.java](https://github.com/sodino/PostDelay/blob/master/app/src/main/java/com/sodino/postdelay/MainActivity.java)

只是简单的在`UI`线程和子线程中不断地每间隔`1s`就打印日志
```
06-14 14:16:47.291 14277-14543/com.sodino.postdelay D/BgTest: bgMessage 2
06-14 14:16:47.643 14277-14277/com.sodino.postdelay D/BgTest: uiMessage 2
```

----------------------------------------------------

## 操作

1. 运行`app`点击`Start`，启动日志打印
2. 在手机后台启动日志记录进程：
```
logcat -v threadtime > /sdcard/test.log &
```
3. 拨掉数据线并锁屏
4. 等待几分钟后，先点亮屏蔽并解锁屏幕，再插上数据线，提取`/sdcard/test.log`文件观察日志打印情况。


> `logcat -v threadtime`记录日志的线程及时间信息。
> `logcat`不使用`-s`过滤无关日志，因为还需要系统的锁屏与解屏日志来提供时间点。

---------------------------------------------------
## 待解决的疑问

1. 锁屏之后，`CPU`休眠，线程是否还会每秒打印一次日志？
1. 锁屏之后，`CPU`休眠，对`UI`线程的影响是否和子线程的影响一样？

---------------------------------------------------

## 分析日志

锁屏时间点：`14:55:38`    

```
06-14 14:55:38.139   874   874 V ActivityManager: Broadcast: Intent { act=android.intent.action.SCREEN_OFF flg=0x50000010 } ordered=true userid=-1 callerApp=ProcessRecord{2de77b41 874:system/1000}
```

在锁屏时间点附近，`UI`线程打印的计数为`8`
```
06-14 14:55:37.850 32424 32424 D BgTest  : uiCount 7
```

在锁屏时间点附近，子线程打印的计数为`7`
```
06-14 14:55:38.143 32424 32489 D BgTest  : bgCount 7
```

经历了6分40秒，即`400`秒后，

解屏时间点：`15:02:18` 
```
06-14 15:02:18.027   874   874 V ActivityManager: Broadcast: Intent { act=android.intent.action.SCREEN_ON flg=0x50000010 } ordered=true userid=-1 callerApp=ProcessRecord{2de77b41 874:system/1000}
```

在解屏时间点附近，`UI`线程打印的计数为`101`
```
06-14 15:02:18.024 32424 32424 D BgTest  : uiCount 89  +3
```

在解屏时间点时，子线程打印的计数为`97`
```
06-14 15:02:18.350 32424 32489 D BgTest  : bgCount 86
```

发现两个线程的计数都远远小于预计的`400+`。
从下图中可以看出在锁屏大概`10s`后，即`14:55:46`开始，`CPU`就进入了休眠，计数开始不规律。

![sleep.count](http://wx4.sinaimg.cn/large/e3dc9ceagy1fgkr61oex8j20rs0fqam2.jpg)


另外，子线程的计数小于`UI`线程，所以子线程受`CPU`休眠的影响稍稍大一些。

## 结论

在锁屏后，`CPU`会尽可能休眠，即会导致线程尽可能不工作，像是冻结了，而且子线程受影响比`UI`线程要稍微多一些。