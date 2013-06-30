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
package org.seasar.dbflute.unit.core.transaction;

/**
 * The performer's callback of transaction.
 * <pre>
 * performNewTransaction(new TransactionPerformer() {
 *     public boolean perform() { <span style="color: #3F7E5E">// transaction scope</span>
 *         ...
 *         return false; <span style="color: #3F7E5E">// true: commit, false: roll-back</span>
 *     }
 * });
 * </pre>
 * @author jflute
 */
public interface TransactionPerformer {

    /**
     * Perform the process in new transaction.
     * @return Does it commit the transaction? (false: roll-back)
     */
    boolean perform();
}
