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
    private DataSource _dataSource;

    // ===================================================================================
    //                                                                         JDBC Helper
    //                                                                         ===========
    protected DataSource getDataSource() { // user method
        return _dataSource;
    }

    protected DataSource doGetDataSourcePlainly() {
        return _dataSource;
    }
}
