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
package org.seasar.dbflute.unit.seasar;

import java.lang.reflect.Field;

import javax.sql.DataSource;

import org.seasar.dbflute.unit.seasar.dicheck.SmartDeployDependencyChecker;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public abstract class ContainerTestCase extends SeasarTestCase {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The data source for database. (NotNull: after injection) */
    protected DataSource _xdataSource;

    // ===================================================================================
    //                                                                         JDBC Helper
    //                                                                         ===========
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataSource getDataSource() { // user method
        return _xdataSource;
    }

    // ===================================================================================
    //                                                                  Dependency Checker
    //                                                                  ==================
    protected void checkDependencyToLogic() {
        doCheckDependencyTo("Logic", "Logic");
    }

    protected void checkDependencyToService() {
        doCheckDependencyTo("Service", "Service");
    }

    protected void checkDependencyToHelper() {
        doCheckDependencyTo("Helper", "Helper");
    }

    protected void checkDependencyToBehavior() {
        doCheckDependencyTo("Behavior", "Bhv");
    }

    protected void doCheckDependencyTo(String title, String suffix) {
        policeStoryOfJavaClassChase(createSmartDeployDependencyChecker(title, suffix));
    }

    protected SmartDeployDependencyChecker createSmartDeployDependencyChecker(String title, String suffix) {
        return new SmartDeployDependencyChecker(title, suffix) {
            @Override
            protected void processTargetClass(Class<?> clazz, Field field, Class<?> injectedType) {
                final String injectedClassName = extractInjectedClassName(injectedType);
                log(clazz.getSimpleName() + "." + field.getName() + " depends on " + injectedClassName);
            }
        };
    }
}
