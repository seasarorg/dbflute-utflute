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
