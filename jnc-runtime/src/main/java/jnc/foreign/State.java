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
package jnc.foreign;

/**
 * @author zhanhb
 */
final class State {

    private static final int STATE_FIELD_ADDING = 0;
    private static final int MAJOR_FINISHED = 1;
    private static final int STATE_FINISH = MAJOR_FINISHED << 4;
    private static final int FINISHED_BY_SIZE = STATE_FINISH | 1;
    private static final int FINISHED_BY_GET_ENCLOSING = STATE_FINISH | 2;
    private static final int STATE_ENCLOSING_ASSIGNED = 2 << 4;
    private static final int STATE_MEMORY_ALLOCATED = 3 << 4;

    private static final String[] STATES = {
        null,
        null,
        "enclosing assigned",
        "memory allocated"
    };

    private static final String[] BY = {
        "size",
        "getEnclosing"
    };

    static State newState() {
        return new State();
    }

    private static String toString(int state) {
        int major = state >> 4;
        return major == MAJOR_FINISHED ? "finished by calling method " + BY[state & 0xF] + "()" : STATES[major];
    }

    private static IllegalStateException toException(Struct struct, int state, String operate) {
        String message = "Cannot %s, state of %s is %s";
        return new IllegalStateException(String.format(message, operate, struct.toString(), toString(state)));
    }

    private int state;

    private State() {
        this.state = STATE_FIELD_ADDING;
    }

    void checkAddField(Struct struct) {
        if (state > STATE_FIELD_ADDING) {
            throw toException(struct, state, "add field");
        }
    }

    void checkGetBuildingOffset(Struct struct) {
        if (state > STATE_FIELD_ADDING) {
            throw toException(struct, state, "get next offset");
        }
    }

    void finishBySize() {
        if (state < STATE_FINISH) {
            state = FINISHED_BY_SIZE;
        }
    }

    void finishByGetEnclosing() {
        if (state < STATE_FINISH) {
            state = FINISHED_BY_GET_ENCLOSING;
        }
    }

    void checkAndAssignEnclosing(Struct struct) {
        if (state >= STATE_ENCLOSING_ASSIGNED) {
            throw toException(struct, state, "assign enclosing");
        }
        state = STATE_ENCLOSING_ASSIGNED;
    }

    void memoryAllocated() {
        if (state < STATE_MEMORY_ALLOCATED) {
            state = STATE_MEMORY_ALLOCATED;
        }
    }

    @Override
    public String toString() {
        return toString(state);
    }

}
