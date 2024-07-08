package com.klejdis.services.util

import kotlinx.coroutines.*

/**
 * A utility class for background tasks.
 * @author Klejdis Beshi
 */
class BackgroundTasksUtil {
    companion object {
        /**
         * Runs a task in the background at a specified time frequency. The task will run indefinitely, until the program is stopped.
         * @param timeFrequencySeconds The time frequency in seconds at which the task will run.
         * @param task The task to run.
         * @return The job that is running the task.
         */
        @OptIn(DelicateCoroutinesApi::class)
        fun run(timeFrequencySeconds: Long, task: suspend () -> Unit): Job {
            return GlobalScope.launch {
                while (true) {
                    task()
                    delay(timeFrequencySeconds * 1000)
                }
            }
        }
    }
}