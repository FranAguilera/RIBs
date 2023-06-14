/*
 * Copyright (C) 2023. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.rib.workers.root.monitoring

import android.util.Log
import com.uber.rib.core.RibEventData
import com.uber.rib.core.RibEventReporter.ribEventDataFlow
import com.uber.rib.workers.BuildConfig

/**
 * Sample of consuming [ribEventDataFlow] possibilities.
 * 1. Can pipe Interactor/Router/Presenter/Worker information to backend
 * 2. Could report expensive workers on Ui thread and crash on Debug builds for early detection
 * 3. More tailored aggregation if needed.
 *
 * IMPORTANT: Given that the logic within [report] will be running upon each ATTACHED/DETACHED RIB
 * component operation, the added logic monitoring login should be minimal and guaranteed that we
 * are not impacting app performance
 */
class RibMonitor {

  fun report(ribEventData: RibEventData) {
    val message = ribEventData.buildWorkerDurationMessage()
    Log.d(LOG_TAG, message)

    if (BuildConfig.DEBUG && ribEventData.isExpensiveUiWorker()) {
      throw ExpensiveUiWorkerException(message)
    }
  }

  private fun RibEventData.isExpensiveUiWorker(): Boolean {
    return this.threadName.contains(MAIN_THREAD_IDENTIFIER) &&
      this.totalBindingDurationMilli > MAIN_THRESHOLD_MILLI
  }

  private fun RibEventData.buildWorkerDurationMessage(): String {
    return "RibEventData: ${this.className} ${this.ribComponentType} ${this.ribEventType} took ${this.totalBindingDurationMilli} ms. - [Thread: ${this.threadName}] [Total threads: ${Thread.activeCount()}]"
  }

  companion object {
    private const val LOG_TAG = "RibWorkerMonitor"
    private const val MAIN_THREAD_IDENTIFIER = "main"
    private const val MAIN_THRESHOLD_MILLI = 16
  }
}

class ExpensiveUiWorkerException(message: String) : Exception(message)
