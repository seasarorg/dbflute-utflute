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
package org.seasar.dbflute.unit.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Resource;

import org.seasar.dbflute.unit.core.InjectionTestCase;
import org.seasar.dbflute.unit.core.binding.BindingAnnotationRule;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.seasar.dbflute.util.DfReflectionUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.annotation.Autowired;
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
    /** The cached configuration files of DI container. (NullAllowed: null means beginning or ending) */
    protected static String[] _xcachedConfigFiles;

    /** The cached application context for DI container. (NullAllowed: null means beginning or ending) */
    protected static ApplicationContext _xcachedContext;

    // -----------------------------------------------------
    //                                         Spring Object
    //                                         -------------
    /** The current active context of application. {Spring Object} */
    protected ApplicationContext _xcurrentActiveContext;

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    // -----------------------------------------------------
    //                                     Prepare Container
    //                                     -----------------
    @Override
    protected void xprepareTestCaseContainer() {
        final String[] configFiles = xdoPrepareTestCaseContainer();
        xsaveCachedInstance(configFiles);
    }

    protected String[] xdoPrepareTestCaseContainer() {
        if (isUseOneTimeContainer()) {
            xdestroyContainer();
        }
        final String[] configFiles = prepareConfigFiles();
        if (xisInitializedContainer()) { // already exists
            if (xcanRecycleContainer(configFiles)) { // no change
                log("...Recycling spring: " + Arrays.asList(configFiles));
                xrecycleContainerInstance(configFiles);
                return configFiles; // no need to initialize
            } else { // changed
                xdestroyContainer();
            }
        }
        xinitializeContainer(configFiles);
        return configFiles;
    }

    protected boolean xcanRecycleContainer(String[] configFiles) {
        return xconfigCanAcceptContainerRecycle(configFiles);
    }

    protected boolean xconfigCanAcceptContainerRecycle(String[] configFiles) {
        return configFiles != null && Arrays.asList(configFiles).equals(Arrays.asList(_xcachedConfigFiles));
    }

    protected void xrecycleContainerInstance(String[] configFiles) {
        _xcurrentActiveContext = _xcachedContext;
    }

    protected void xsaveCachedInstance(String[] configFiles) {
        _xcachedConfigFiles = configFiles;
    }

    /**
     * Prepare configuration files for Spring Framework.
     * @return The array of pure file name. (NotNull)
     */
    protected String[] prepareConfigFiles() { // customize point
        return new String[] {}; // as default
    }

    /**
     * Create application context for Spring Framework.
     * @param confs The array of configuration file names. (NotNull)
     * @return The new-created instance of application context. (NotNull)
     */
    protected ApplicationContext createApplicationContext(String[] confs) {
        return new ClassPathXmlApplicationContext(confs);
    }

    @Override
    protected void xclearCachedContainer() {
        _xcachedContext = null;
    }

    // ===================================================================================
    //                                                                         Transaction
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    @Override
    protected TransactionResource beginNewTransaction() { // user method
        final String managerKey = "transactionManager";
        if (!hasComponent(managerKey)) {
            return null;
        }
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
    //                                                                   Component Binding
    //                                                                   =================
    @Override
    protected Map<Class<? extends Annotation>, BindingAnnotationRule> xprovideBindingAnnotationRuleMap() {
        final Map<Class<? extends Annotation>, BindingAnnotationRule> ruleMap = newHashMap();
        ruleMap.put(Resource.class, new BindingAnnotationRule().byNameOnly());
        ruleMap.put(Autowired.class, new BindingAnnotationRule().byTypeOnly());
        return ruleMap;
    }

    // ===================================================================================
    //                                                                     Spring Handling
    //                                                                     ===============
    // -----------------------------------------------------
    //                                            Initialize
    //                                            ----------
    protected boolean xisInitializedContainer() {
        return _xcachedContext != null;
    }

    protected void xinitializeContainer(String[] configFiles) {
        if (configFiles != null && configFiles.length > 0) {
            log("...Initializing spring by configuration files: " + Arrays.asList(configFiles));
            _xcurrentActiveContext = createApplicationContext(configFiles);
        } else {
            log("...Initializing spring by provided default context.");
            _xcurrentActiveContext = provideDefaultApplicationContext();
        }
        _xcachedContext = _xcurrentActiveContext;
    }

    protected ApplicationContext provideDefaultApplicationContext() {
        final BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance();
        final BeanFactoryReference ref = locator.useBeanFactory("context");
        return (ApplicationContext) ref.getFactory();
    }

    // -----------------------------------------------------
    //                                               Destroy
    //                                               -------
    protected void xdestroyContainer() {
        xreleaseClassPathContext();
        xreleaseLocatorContextCache();
        _xcachedContext = null;
        _xcurrentActiveContext = null;
    }

    protected void xreleaseClassPathContext() {
        final ApplicationContext cachedContext = _xcachedContext;
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

    // -----------------------------------------------------
    //                                             Component
    //                                             ---------
    /**
     * {@inheritDoc}
     */
    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) { // user method
        return _xcurrentActiveContext.getBean(type);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected <COMPONENT> COMPONENT getComponent(String name) { // user method
        return (COMPONENT) _xcurrentActiveContext.getBean(name);
    }

    /**
     * {@inheritDoc}
     */
    protected boolean hasComponent(Class<?> type) { // user method
        try {
            getComponent(type);
            return true;
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected boolean hasComponent(String name) { // user method
        try {
            getComponent(name);
            return true;
        } catch (NoSuchBeanDefinitionException e) {
            return false;
        }
    }
}
