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
package org.seasar.dbflute.unit.spring.bean;

import javax.annotation.Resource;

import org.seasar.dbflute.unit.spring.dbflute.exbhv.FooBhv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class FooAction {

    @Resource
    protected FooBhv fooBhv; // standard annotation

    protected FooController fooController; // no annotation, no setter

    @Autowired
    protected FooController barController; // name wrong: Autowired is type-only so injected

    @Resource
    protected FooFacade fooHelper; // name wrong: Resource is name-only so not injected

    @Resource
    private FooFacade fooFacade; // private field

    @Autowired
    protected FooLogic fooLogic; // container annotation

    @Autowired
    protected FooService fooService; // specify none is unsupported so injected (so private test)

    protected PlatformTransactionManager transactionManager; // setter only no annotation

    // no web here
    //@Resource
    //protected HttpServletRequest request; // mocklet

    public FooFacade facadeInstance() {
        return fooFacade;
    }

    public String serviceToString() {
        return fooService != null ? fooService.toString() : null;
    }

    public FooService serviceInstance() {
        return fooService;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
