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
package org.seasar.dbflute.unit.spring;

import org.seasar.dbflute.unit.core.transaction.TransactionFailureException;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * @author jflute
 * @since 0.1.1 (2011/07/25 Monday)
 */
public class SpringTransactionResource implements TransactionResource {

    protected PlatformTransactionManager _transactionManager;
    protected TransactionStatus _transactionStatus;

    public void commit() {
        try {
            _transactionManager.commit(_transactionStatus);
        } catch (RuntimeException e) {
            throw new TransactionFailureException("Failed to commit the transaction.", e);
        }
    }

    public void rollback() {
        try {
            _transactionManager.rollback(_transactionStatus);
        } catch (RuntimeException e) {
            throw new TransactionFailureException("Failed to roll-back the transaction.", e);
        }
    }

    public PlatformTransactionManager getTransactionManager() {
        return _transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this._transactionManager = transactionManager;
    }

    public TransactionStatus getTransactionStatus() {
        return _transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this._transactionStatus = transactionStatus;
    }
}
