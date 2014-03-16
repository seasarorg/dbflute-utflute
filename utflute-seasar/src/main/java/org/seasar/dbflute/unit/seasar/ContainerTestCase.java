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

import javax.sql.DataSource;

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
     * Get the data source for database. (actually non-wrapped)
     * @return The instance from DI container. (NotNull)
     */
    protected DataSource getDataSource() { // user method
        return _xdataSource;
    }

    /**
     * Get the plain (non-wrapped) data source for database.
     * @return The instance from DI container. (NotNull)
     */
    protected DataSource doGetDataSourcePlainly() {
        return _xdataSource;
    }
}
