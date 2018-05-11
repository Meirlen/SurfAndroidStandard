/*
  Copyright (c) 2018-present, SurfStudio LLC.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package ru.surfstudio.android.core.ui.bus;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Шина сообшений работающая на Rx. Может истользоваться только в контексте одной активити
 */
public class RxBus {

    private PublishSubject<Object> internalBus = PublishSubject.create();

    public RxBus() {
        //empty
    }

    public <T> Observable<T> observeEvents(Class<T> eventClass) {
        return internalBus.filter(eventClass::isInstance)
                .map(eventClass::cast);
    }

    public <T> void emitEvent(T event) {
        internalBus.onNext(event);
    }
}
