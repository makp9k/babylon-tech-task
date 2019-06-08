package com.example.babylon.common.android

import io.reactivex.Scheduler

class ApplicationSchedulers(
    val ioScheduler: Scheduler,
    val computationScheduler: Scheduler
)
