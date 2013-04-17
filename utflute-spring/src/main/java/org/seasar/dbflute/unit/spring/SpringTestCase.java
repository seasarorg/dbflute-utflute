package org.seasar.dbflute.unit.spring;

import org.seasar.dbflute.unit.core.InjectionTestCase;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;
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
    private static ApplicationContext _cachedApplicationContext;

    // -----------------------------------------------------
    //                                         Spring Object
    //                                         -------------
    /** The current context of application. {Spring Object} */
    private ApplicationContext _currentApplicationContext;

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    @Override
    protected void xprepareTestCaseContainer() {
        if (_cachedApplicationContext != null) {
            _currentApplicationContext = _cachedApplicationContext;
            return;
        }
        final String[] confs = prepareConfigFiles();
        if (confs != null && confs.length > 0) {
            _currentApplicationContext = new ClassPathXmlApplicationContext(confs);
        } else {
            final BeanFactoryLocator locator = ContextSingletonBeanFactoryLocator.getInstance();
            final BeanFactoryReference ref = locator.useBeanFactory("context");
            _currentApplicationContext = (ApplicationContext) ref.getFactory();
        }
        _cachedApplicationContext = _currentApplicationContext;
    }

    protected String[] prepareConfigFiles() { // customize point
        return new String[] {}; // as default
    }
    
    protected ApplicationContext createApplicationContext(String[] confs) {
        return new ClassPathXmlApplicationContext(confs);
    }

    @Override
    protected TransactionResource beginTransaction() { // user method
        final String managerKey = "transactionManager";
        final PlatformTransactionManager manager = getComponent(managerKey);
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        final TransactionStatus status = manager.getTransaction(def);
        final SpringTransactionResource resource = new SpringTransactionResource();
        resource.setTransactionManager(manager);
        resource.setTransactionStatus(status);
        return resource; // for thread-fire's transaction or manual transaction
    }

    @Override
    protected void xclearCachedContainer() {
        _cachedApplicationContext = null;
    }

    // ===================================================================================
    //                                                                     Spring Handling
    //                                                                     ===============
    protected void xdestroyContainer() {
        // TODO: How?
    }

    @SuppressWarnings("unchecked")
    protected <COMPONENT> COMPONENT getComponent(Class<COMPONENT> type) { // user method
        final String name = Srl.initUncap(DfTypeUtil.toClassTitle(type));
        return (COMPONENT) _currentApplicationContext.getBean(name);
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
