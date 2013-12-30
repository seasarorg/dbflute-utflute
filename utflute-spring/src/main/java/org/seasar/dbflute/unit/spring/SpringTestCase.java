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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import org.seasar.dbflute.unit.core.InjectionTestCase;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author jflute
 * @since 0.1.1 (2011/07/25 Monday)
 */
public abstract class SpringTestCase extends InjectionTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // -----------------------------------------------------
    //                                          Static Cache
    //                                          ------------
    private static String[] _preparedConfigFiles;
    private static ApplicationContext _cachedApplicationContext;

    // -----------------------------------------------------
    //                                         Spring Object
    //                                         -------------
    /** The current context of application. {Spring Object} */
    private ApplicationContext _currentApplicationContext;

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    // -----------------------------------------------------
    //                                     Prepare Container
    //                                     -----------------
    @Override
    protected void xprepareTestCaseContainer() {
        if (isUseOneTimeContainer()) {
            xdestroyContainer();
        }
        final String[] configFiles = prepareConfigFiles();
        if (xisInitializedContainer()) { // already exists
            if (xisEqualWithPreparedConfigurations(configFiles)) { // no change
                _currentApplicationContext = _cachedApplicationContext;
                return; // no need to initialize
            } else { // changed
                xdestroyContainer();
            }
        }
        xinitializeContainer(configFiles);
        _preparedConfigFiles = configFiles;
    }

    protected boolean xisEqualWithPreparedConfigurations(String[] configFiles) {
        return configFiles != null && Arrays.asList(configFiles).equals(Arrays.asList(_preparedConfigFiles));
    }

    protected String[] prepareConfigFiles() { // customize point
        return new String[] {}; // as default
    }

    protected ApplicationContext createApplicationContext(String[] confs) {
        return new ClassPathXmlApplicationContext(confs);
    }

    @Override
    protected void xclearCachedContainer() {
        _cachedApplicationContext = null;
    }

    // ===================================================================================
    //                                                                         Transaction
    //                                                                         ===========
    @Override
    protected TransactionResource beginNewTransaction() { // user method
        final String managerKey = "transactionManager";
        final PlatformTransactionManager manager = getComponent(managerKey);
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        final TransactionStatus status = manager.getTransaction(def);
        final SpringTransactionResource resource = new SpringTransactionResource();
        resource.setTransactionManager(manager);
        resource.setTransactionStatus(status);
        return resource; // for thread-fire's transaction or manual transaction
    }

    // ===================================================================================
    //                                                                     Spring Handling
    //                                                                     ===============
    protected boolean xisInitializedContainer() {
        return _cachedApplicationContext != null;
    }

    protected void xinitializeContainer(String[] configFiles) {
        if (configFiles != null && configFiles.length > 0) {
            _currentApplicationContext = new ClassPathXmlApplicationContext(configFiles);
        } else {
            final BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance();
            final BeanFactoryReference ref = locator.useBeanFactory("context");
            _currentApplicationContext = (ApplicationContext) ref.getFactory();
        }
        _cachedApplicationContext = _currentApplicationContext;
    }

    protected void xdestroyContainer() {
        xreleaseClassPathContext();
        xreleaseLocatorContextCache();
        _currentApplicationContext = null;
        _cachedApplicationContext = null;
    }

    protected void xreleaseClassPathContext() {
        final ApplicationContext cachedContext = _cachedApplicationContext;
        if (cachedContext != null && cachedContext instanceof ClassPathXmlApplicationContext) {
            ((ClassPathXmlApplicationContext) cachedContext).destroy();
        }
    }

    protected void xreleaseLocatorContextCache() {
        final Class<ContextSingletonBeanFactoryLocator> locatorType = ContextSingletonBeanFactoryLocator.class;
        final String cacheMapName = "instances";
        final Field cacheMapField = DfReflectionUtil.getWholeField(locatorType, cacheMapName);
        final Map<?, ?> instances = (Map<?, ?>) DfReflectionUtil.getValueForcedly(cacheMapField, null);
        instances.clear();
    }

    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) { // user method
        return _currentApplicationContext.getBean(type);
    }

    @SuppressWarnings("unchecked")
    protected <COMPONENT> COMPONENT getComponent(String name) { // user method
        return (COMPONENT) _currentApplicationContext.getBean(name);
    }

    protected boolean hasComponent(Class<?> type) { // user method
        try {
            getComponent(type);
            return true;
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
    }

    protected boolean hasComponent(String name) { // user method
        try {
            getComponent(name);
            return true;
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
    }
}
