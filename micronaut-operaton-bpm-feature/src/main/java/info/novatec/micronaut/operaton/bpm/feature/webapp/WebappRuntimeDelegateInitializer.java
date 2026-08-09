/*
 * Copyright 2020-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.novatec.micronaut.operaton.bpm.feature.webapp;

import io.micronaut.context.annotation.Requires;
import io.micronaut.transaction.SynchronousTransactionManager;
import jakarta.inject.Singleton;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.operaton.bpm.admin.Admin;
import org.operaton.bpm.admin.impl.DefaultAdminRuntimeDelegate;
import org.operaton.bpm.cockpit.Cockpit;
import org.operaton.bpm.cockpit.impl.DefaultCockpitRuntimeDelegate;
import org.operaton.bpm.engine.impl.interceptor.Command;
import org.operaton.bpm.webapp.db.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.function.Supplier;

/**
 * Replaces the runtime delegates of the webapps so that their queries are executed within a Micronaut transaction.
 * <p>
 * The Cockpit and Admin webapps bypass the process engine's command interceptors: they run their plugin queries
 * through an own {@link org.operaton.bpm.webapp.impl.db.CommandExecutorImpl} which obtains a JDBC connection on its
 * own - even in its constructor, which checks the transaction isolation level. The transaction aware data source of
 * Micronaut Data rejects this outside of a transaction with a
 * {@code NoConnectionException: No current connection present}. Affected are e.g. Cockpit's process definition
 * statistics and Admin's metrics.
 * <p>
 * The default delegates are installed by {@code CockpitContainerBootstrap} and {@code AdminContainerBootstrap} when
 * the servlet context is initialized. Therefore {@link JettyServerCustomizerRuntimeWebapp} triggers the replacement
 * from a {@code ServletContextListener} registered after those bootstraps - otherwise the replacement would be
 * overwritten again.
 * <p>
 * Set a breakpoint in {@link JdbcTransaction#openConnection()} to see which data source is being used.
 *
 * @author Tobias Schäfer
 */
@Singleton
@Requires(property = "operaton.webapps.enabled", value = "true")
public class WebappRuntimeDelegateInitializer {

    private static final Logger log = LoggerFactory.getLogger(WebappRuntimeDelegateInitializer.class);

    protected final SynchronousTransactionManager<Connection> transactionManager;

    public WebappRuntimeDelegateInitializer(SynchronousTransactionManager<Connection> transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void installRuntimeDelegates() {
        Cockpit.setCockpitRuntimeDelegate(new DefaultCockpitRuntimeDelegate() {
            @Override
            protected org.operaton.bpm.cockpit.db.CommandExecutor createCommandExecutor(String processEngineName) {
                return transactional(() -> super.createCommandExecutor(processEngineName));
            }
        });
        Admin.setAdminRuntimeDelegate(new DefaultAdminRuntimeDelegate() {
            @Override
            protected CommandExecutor createCommandExecutor(String processEngineName) {
                return transactional(() -> super.createCommandExecutor(processEngineName));
            }
        });
        log.debug("Replaced the Cockpit and Admin runtime delegates to enable transactions for the webapps");
    }

    /**
     * Creates the command executor within a transaction and executes each of its commands within a transaction.
     * The returned executor implements the Cockpit interface which is merely a marker for the Admin one, so that it
     * can be used by both webapps.
     */
    protected org.operaton.bpm.cockpit.db.CommandExecutor transactional(Supplier<CommandExecutor> commandExecutorSupplier) {
        // The constructor of the command executor already requires a connection, see class comment.
        CommandExecutor commandExecutor = transactionManager.executeWrite(transactionStatus -> commandExecutorSupplier.get());
        return new org.operaton.bpm.cockpit.db.CommandExecutor() {
            @Override
            public <T> T executeCommand(Command<T> command) {
                return transactionManager.executeWrite(transactionStatus -> {
                    T result = commandExecutor.executeCommand(command);
                    Connection connection = transactionManager.getConnection();
                    if (connection.getAutoCommit()) {
                        // We disable auto-commit here, otherwise PostgreSQL will fail with exception "org.postgresql.util.PSQLException: Cannot commit when autoCommit is enabled."
                        // See also JdbcTransaction#resetAutoCommit where MyBatis enables auto-commit
                        // See also DataSourceTransactionManager#doBegin which does the same as we do here. It seems that Micronaut always wants to take full control.
                        log.debug("Switching JDBC Connection [{}] to manual commit", connection);
                        connection.setAutoCommit(false);
                    }
                    return result;
                });
            }
        };
    }
}
