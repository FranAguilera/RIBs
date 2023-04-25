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

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Opinionated abstraction over original [Worker] definition and its threading model.
 *
 * As opposite with the original [Worker] definition where the caller thread being used at [WorkerBinder.bind] defined the thread
 * where Worker.onStart/onStop is executed (unless [coroutineDispatcher] is overriden with a value different than [RibDispatchers.Unconfined]).
 *
 * This extension will be defaulting [coroutineDispatcher] to [RibDispatchers.Default] (instead of [RibDispatchers.Unconfined].
 *
 * The reasoning for using [RibDispatchers.Default] is to prevent past common performance pitfalls due to calling expensive logic at onStart/onStop on the UI thread.
 *
 * Moving forward, new RIB Worker additions should be using this extension to guaranteed Workers won't be running
 * by default on [RibDispatchers.Unconfined] (given most of the times Workers are bound on UI thread at Interactor's didBecomeActive)
 *
 * Still if needed, [coroutineDispatcher] can be overriden in concrete implementation with values other than [RibDispatchers.Default]
 */
public interface RibWorker : Worker {

    @JvmDefault
    override val coroutineDispatcher: CoroutineDispatcher get() = RibDispatchers.Default
}
