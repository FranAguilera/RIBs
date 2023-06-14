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
package com.uber.rib.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Emits RIB event information (via [ribEventDataFlow]) for each RIB component (e.g.
 * Interactor/Presenter/Router).
 *
 * Currently it contains duration for each ATTACH/DETACH action of those RIB components
 */
public object RibEventReporter {

  private val mutableRibEventDataFlow =
    MutableSharedFlow<RibEventData>(1, 0, BufferOverflow.DROP_OLDEST)
  public val ribEventDataFlow: SharedFlow<RibEventData>
    get() = mutableRibEventDataFlow

  /**
   * Emits emission of ATTACHED/DETACHED events for each RIB component.
   *
   * @param className The full class name of the RIB component
   * @param ribComponentType The RIB component type (e.g. Interactor, Router, Presenter)
   * @param ribEventType RIB event type (e.g. ATTACH/DETACH)
   * @param totalBindingDurationMilli Total duration (in ms) of each ATTACH/DETACH events
   */
  internal fun emitRibEventInfo(
    className: String,
    ribComponentType: RibComponentType,
    ribEventType: RibEventType,
    totalBindingDurationMilli: Long,
  ) {
    if (className.isEmpty()) {
      // There's no point to emit emission if we don't know which RIB component event was triggered
      return
    }

    val ribEventData =
      RibEventData(
        className,
        ribComponentType,
        ribEventType,
        Thread.currentThread().name,
        totalBindingDurationMilli,
      )
    mutableRibEventDataFlow.tryEmit(ribEventData)
  }
}

/** Holds relevant RIB event information */
public data class RibEventData(
  /** Related RIB class name */
  val className: String,

  /** The current RIB event type being bound (e.g. Interactor/Presenter/Router) */
  val ribComponentType: RibComponentType,

  /** RIB component event type ATTACHED/DETACHED */
  val ribEventType: RibEventType,

  /** Reports the current thread name where Rib Event happen (should mainly be main thread) */
  val threadName: String,

  /** Total binding duration in milliseconds of Worker.onStart/onStop */
  val totalBindingDurationMilli: Long,
)
