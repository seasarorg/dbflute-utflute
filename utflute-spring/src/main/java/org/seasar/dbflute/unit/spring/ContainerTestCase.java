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

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.seasar.dbflute.jdbc.DataSourceHandler;
import org.seasar.dbflute.jdbc.HandlingDataSourceWrapper;
import org.seasar.dbflute.jdbc.NotClosingConnectionWrapper;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * @author jflute
 * @since 0.1.1 (2011/07/25 Monday)
 */
public abstract class ContainerTestCase extends SpringTestCase {

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
        if (_xdataSource == null) {
            return null;
        }
        return wrapSpringTransactionalDataSource(_xdataSource); // as default
    }

    /**
     * Wrap the data source for Spring transaction. (basically for commons-DBCP)
     * @param dataSource The data source provided from DI container. (NotNull)
     * @return The wrapped data source for Spring transaction. (NotNull)
     */
    protected DataSource wrapSpringTransactionalDataSource(DataSource dataSource) {
        // same way as DBFlute does because it may use, e.g. Commons DBCP
        final SpringTransactionalDataSourceHandler handler = new SpringTransactionalDataSourceHandler();
        return new HandlingDataSourceWrapper(_xdataSource, new DataSourceHandler() {
            public Connection getConnection(DataSource dataSource) throws SQLException {
                return handler.getConnection(dataSource);
            }
        });
    }

    protected static class SpringTransactionalDataSourceHandler implements DataSourceHandler {

        public Connection getConnection(DataSource ds) throws SQLException {
            final Connection conn = getConnectionFromUtils(ds);
            if (isConnectionTransactional(conn, ds)) {
                return new NotClosingConnectionWrapper(conn);
            } else {
                return conn;
            }
        }

        public Connection getConnectionFromUtils(DataSource ds) {
            return DataSourceUtils.getConnection(ds);
        }

        public boolean isConnectionTransactional(Connection conn, DataSource ds) {
            return DataSourceUtils.isConnectionTransactional(conn, ds);
        }

        @Override
        public String toString() {
            return "SpringDBCPDataSourceHandler(for Spring and Commons-DBCP)";
        }
    }
}
