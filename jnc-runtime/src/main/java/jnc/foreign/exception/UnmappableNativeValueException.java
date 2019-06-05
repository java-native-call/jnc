/*
 * Copyright 2019 zhanhb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jnc.foreign.exception;

import jnc.foreign.internal.NotFinal;

/**
 * @author zhanhb
 */
@NotFinal(NotFinal.Reason.API)
public class UnmappableNativeValueException extends IllegalArgumentException {

    private static final long serialVersionUID = 0L;

    private final Class<?> type;
    private final long value;

    public UnmappableNativeValueException(Class<?> type, long value) {
        super("type=" + type.getName() + ",value=" + value, null);
        this.type = type;
        this.value = value;
    }

    @NotFinal(NotFinal.Reason.API)
    public Class<?> getType() {
        return type;
    }

    @NotFinal(NotFinal.Reason.API)
    public long getValue() {
        return value;
    }

}
