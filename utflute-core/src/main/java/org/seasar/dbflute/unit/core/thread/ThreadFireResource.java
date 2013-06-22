/*
 * Copyright 2004-2013 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dbflute.unit.core.thread;

/**
 * @author jflute
 * @since 0.1.7 (2012/08/30 Thursday)
 */
public class ThreadFireResource {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final long _threadId;
    protected final Object _parameter;
    protected final ThreadFireLatch _yourLatch;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ThreadFireResource(long threadId, Object parameter, ThreadFireLatch yourLatch) {
        _threadId = threadId;
        _parameter = parameter;
        _yourLatch = yourLatch;
    }

    // ===================================================================================
    //                                                                     CountDown Latch
    //                                                                     ===============
    /**
     * Await until all threads come here.
     */
    public void await() {
        _yourLatch.await();
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the thread ID of the current thread.
     * @return The long value of thread ID.
     */
    public long getThreadId() {
        return _threadId;
    }

    /**
     * Get the parameter of the current thread.
     * @return The instance of parameter. (NullAllowed)
     */
    @SuppressWarnings("unchecked")
    public <PARAMETER> PARAMETER getParameter() {
        return (PARAMETER) _parameter;
    }

    public ThreadFireLatch getYourLatch() {
        return _yourLatch;
    }
}
