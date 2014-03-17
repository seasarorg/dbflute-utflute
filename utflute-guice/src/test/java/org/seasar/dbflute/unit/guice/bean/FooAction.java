/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.unit.guice.bean;

import javax.transaction.TransactionManager;

import org.seasar.dbflute.unit.guice.dbflute.exbhv.FooBhv;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooAction {

    @javax.inject.Inject
    protected FooBhv fooBhv; // standard annotation

    protected FooController fooController; // no annotation, no setter

    @com.google.inject.Inject
    protected FooController barController; // name wrong: but by-type so injected 

    @com.google.inject.Inject
    protected FooFacade fooHelper; // name wrong: but by-type so injected

    @com.google.inject.Inject
    protected FooLogic fooLogic; // container annotation

    @com.google.inject.Inject
    private FooService fooService; // specify none is unsupported so injected (so private test)

    protected TransactionManager transactionManager; // setter only no annotation

    // no web here
    //@Resource
    //protected HttpServletRequest request; // mocklet

    public String serviceToString() {
        return fooService != null ? fooService.toString() : null;
    }

    public FooService serviceInstance() {
        return fooService;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
