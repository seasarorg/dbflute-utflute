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
package org.seasar.dbflute.unit.guice;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.seasar.dbflute.unit.guice.dbflute.exbhv.FooBhv;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.nonxa.AtomikosNonXADataSourceBean;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public abstract class MockGuiceTestCase extends ContainerTestCase {

    @Override
    protected List<Module> prepareModuleList() {
        final Module appModule = new Module() {
            public void configure(Binder binder) {
                binder.bind(FooBhv.class).toInstance(new FooBhv());
            }
        };
        final List<Module> moduleList = new ArrayList<Module>();
        moduleList.add(appModule);
        final TransactionModule transactionModule = createTransactionModule();
        if (transactionModule != null) {
            moduleList.add(transactionModule);
        }
        return moduleList;
    }

    protected TransactionModule createTransactionModule() {
        return new TransactionModule(createDataSource());
    }

    protected DataSource createDataSource() {
        final AtomikosNonXADataSourceBean bean = new AtomikosNonXADataSourceBean();
        bean.setUniqueResourceName("NONXADBMS");
        bean.setDriverClassName("org.h2.jdbcx.JdbcDataSource");
        final EmbeddedH2UrlFactoryBean factoryBean = new EmbeddedH2UrlFactoryBean();
        factoryBean.setUrlSuffix("/exampledb/exampledb");
        factoryBean.setReferenceClassName(EmbeddedH2UrlFactoryBean.class.getName());
        final String url;
        try {
            url = factoryBean.getObject().toString();
        } catch (Exception e) {
            String msg = "The factoryBean was invalid: " + factoryBean;
            throw new IllegalStateException(msg, e);
        }
        bean.setUrl(url.toString());
        bean.setUser("sa");
        bean.setPassword("");
        bean.setPoolSize(20);
        bean.setBorrowConnectionTimeout(60);
        return bean;
    }

    protected static class TransactionModule extends AbstractModule {

        protected final DataSource _dataSource;

        public TransactionModule(DataSource dataSource) {
            if (dataSource == null) {
                String msg = "The argument 'dataSource' should not be null!";
                throw new IllegalArgumentException(msg);
            }
            _dataSource = dataSource;
        }

        @Override
        protected void configure() {
            try {
                final UserTransactionImp userTransactionImp = new UserTransactionImp();
                userTransactionImp.setTransactionTimeout(300);
                UserTransactionManager userTransactionManager = new UserTransactionManager();
                userTransactionManager.setForceShutdown(true);
                userTransactionManager.init();
                bind(UserTransaction.class).toInstance(userTransactionImp);
                bind(TransactionManager.class).toInstance(userTransactionManager);
                bind(DataSource.class).toInstance(_dataSource);
            } catch (SystemException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
