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
package info.novatec.micronaut.operaton.bpm.feature.test

import info.novatec.micronaut.operaton.bpm.feature.Configuration
import info.novatec.micronaut.operaton.bpm.feature.initialization.AdminUserCreator
import io.micronaut.context.annotation.Property
import io.micronaut.core.value.PropertyNotFoundException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.operaton.bpm.engine.ProcessEngine
import org.operaton.bpm.engine.authorization.Authorization.ANY
import org.operaton.bpm.engine.authorization.Groups.OPERATON_ADMIN
import org.operaton.bpm.engine.authorization.Groups.GROUP_TYPE_SYSTEM
import org.operaton.bpm.engine.authorization.Resources
import org.operaton.bpm.engine.identity.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

/**
 * Tests for [AdminUserCreator]
 *
 * @author Titus Meyer
 */
class AdminUserCreatorTest {
    @MicronautTest(rebuildContext = true)
    @Nested
    inner class AdminUserCreatorTestWithoutProperties {
        @Inject
        lateinit var processEngine: ProcessEngine

        @Inject
        lateinit var configuration: Configuration

        @Inject
        lateinit var adminUserCreator: Optional<AdminUserCreator>

        @BeforeEach
        fun cleanupDatabase() {
            // Clean up users from previous tests
            processEngine.identityService.createUserQuery().list().forEach {
                processEngine.identityService.deleteUser(it.id)
            }
        }

        @Test
        fun adminUserNotDefined() {
            assertThrows(PropertyNotFoundException::class.java) { configuration.adminUser.id }
            assertFalse(adminUserCreator.isPresent)
            assertEquals(0, processEngine.identityService.createUserQuery().count())
        }
    }

    @MicronautTest(rebuildContext = true)
    @Property(name = "operaton.admin-user.id", value = "admin")
    @Property(name = "operaton.admin-user.password", value = "admin")
    @Property(name = "operaton.admin-user.firstname", value = "Donald")
    @Property(name = "operaton.admin-user.lastname", value = "Duck")
    @Property(name = "operaton.admin-user.email", value = "Donald.Duck@example.org")
    @Nested
    inner class AdminUserCreatorTestWithAllProperties {

        @Inject
        lateinit var processEngine: ProcessEngine

        @Inject
        lateinit var configuration: Configuration

        @Inject
        lateinit var adminUserCreator: Optional<AdminUserCreator>

        @Test
        fun adminUserCreated() {
            assertTrue(adminUserCreator.isPresent)

            assertEquals("admin", configuration.adminUser.id)
            assertEquals("admin", configuration.adminUser.password)
            assertEquals("Donald", configuration.adminUser.firstname.get())
            assertEquals("Duck", configuration.adminUser.lastname.get())
            assertEquals("Donald.Duck@example.org", configuration.adminUser.email.get())

            assertAdminUserExists(processEngine, configuration.adminUser.id)
            val adminUser = queryUser(processEngine, configuration.adminUser.id)
            assertEquals("Donald", adminUser.firstName)
            assertEquals("Duck", adminUser.lastName)
            assertEquals("Donald.Duck@example.org", adminUser.email)
            assertAdminGroupExists(processEngine)
            assertAdminGroupAuthorizationsExist(processEngine)
        }

        @Test
        fun adminUserOnlyCreatedOnce() {
            assertTrue(adminUserCreator.isPresent)

            assertEquals(1, processEngine.identityService.createUserQuery().count())
            assertAdminUserExists(processEngine, configuration.adminUser.id)
            assertAdminGroupExists(processEngine)
            assertAdminGroupAuthorizationsExist(processEngine)

            // Trigger event again and check that it is idempotent
            adminUserCreator.get().execute(processEngine)

            assertEquals(1, processEngine.identityService.createUserQuery().count())
            assertAdminUserExists(processEngine, configuration.adminUser.id)
            assertAdminGroupExists(processEngine)
            assertAdminGroupAuthorizationsExist(processEngine)
        }
    }

    @MicronautTest(rebuildContext = true)
    @Property(name = "operaton.admin-user.id", value = "admin2")
    @Property(name = "operaton.admin-user.password", value = "admin2")
    @Nested
    inner class AdminUserCreatorTestWithOnlyRequiredProperties {

        @Inject
        lateinit var processEngine: ProcessEngine

        @Inject
        lateinit var configuration: Configuration

        @Inject
        lateinit var adminUserCreator: Optional<AdminUserCreator>

        @Test
        fun adminUserCreated() {
            assertTrue(adminUserCreator.isPresent)

            assertEquals("admin2", configuration.adminUser.id)
            assertEquals("admin2", configuration.adminUser.password)

            assertAdminUserExists(processEngine, configuration.adminUser.id)
            val adminUser = queryUser(processEngine, configuration.adminUser.id)
            assertEquals("Admin2", adminUser.firstName)
            assertEquals("Admin2", adminUser.lastName)
            assertEquals("admin2@localhost", adminUser.email)
            assertAdminGroupExists(processEngine)
            assertAdminGroupAuthorizationsExist(processEngine)
        }

    }

    fun queryUser(processEngine: ProcessEngine, userId: String): User {
        return processEngine.identityService.createUserQuery().userId(userId).singleResult()
    }

    fun assertAdminUserExists(processEngine: ProcessEngine, userId: String) {
        val adminUser = queryUser(processEngine, userId)
        assertNotNull(adminUser)
        assertEquals(userId, adminUser.id)
    }

    fun assertAdminGroupExists(processEngine: ProcessEngine) {
        val adminGroup = processEngine.identityService.createGroupQuery().groupId(OPERATON_ADMIN).singleResult()
        assertNotNull(adminGroup)
        assertEquals(GROUP_TYPE_SYSTEM, adminGroup.type)
    }

    fun assertAdminGroupAuthorizationsExist(processEngine: ProcessEngine) {
        for (resource in Resources.values()) {
            assertEquals(
                1,
                processEngine.authorizationService.createAuthorizationQuery()
                    .groupIdIn(OPERATON_ADMIN).resourceType(resource).resourceId(ANY).count()
            )
        }
    }

}