# micronaut-operaton-platform

This project has been migrated from Camunda 7 to [Operaton](https://operaton.org/), a community-driven, open-source BPMN engine and fork of Operaton 7.

[![Release]](https://github.com/operaton/operaton/releases)
[![License](https://img.shields.io/:license-apache-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Continuous Integration](https://github.com/camunda-community-hub/micronaut-camunda-platform-7/workflows/Continuous%20Integration/badge.svg)](https://github.com/camunda-community-hub/micronaut-camunda-platform-7/actions)
[![GitHub Discussions](https://img.shields.io/badge/Forum-GitHub_Discussions-blue)](https://github.com/camunda-community-hub/micronaut-camunda-platform-7/discussions)

[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
![Compatible with: Operaton](https://img.shields.io/badge/Compatible%20with-Operaton-26d07c)
[![](https://img.shields.io/badge/Lifecycle-Stable-brightgreen)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#stable-)

This open source project allows you to easily integrate [Operaton](https://operaton.org/) into [Micronaut](https://micronaut.io) projects.

The Micronaut Framework is known for its efficient use of resources. With this integration you embed the [BPMN 2.0](https://www.bpmn.org/) compliant and developer friendly Operaton process engine with minimal memory footprint into your application.

The integration is preconfigured with sensible defaults, so that you can get started with minimal configuration: simply add a dependency in your Micronaut project!

If you are interested in using a cloud-native workflow engine like Camunda's Zeebe on a Micronaut application, have a look at the open source project [micronaut-zeebe-client](https://github.com/camunda-community-hub/micronaut-zeebe-client).

---
_We're not aware of all installations of our Open Source project. However, we love_
* _listening to your feedback,_
* _discussing possible use cases with you,_
* _aligning the roadmap to your needs!_

📨 _Please [contact](#contact) us!_

---

Do you want to try it out? Please jump to the [Getting Started](#getting-started) section.

Do you want to contribute to our open source project? Please read the [Contribution Guidelines](CONTRIBUTING.md) and [contact us](#contact).

If you also want to run an External Task Client on a Micronaut application, have a look at the open source project [micronaut-camunda-external-client](https://github.com/camunda-community-hub/micronaut-camunda-external-client) (originally for Camunda, may require adaptation for Operaton).

Micronaut Framework + Operaton = :heart:

# Table of Contents

* ✨ [Features](#features)
* 🚀 [Getting Started](#getting-started)
  * [Create Project with Micronaut Launch](#create-project-with-micronaut-launch)
  * [Deploying Models](#deploying-models)
  * [Operaton Integration](#operaton-integration)
  * [Configuration](#configuration)
  * [Examples](#examples)
  * [Supported JDKs](#supported-jdks)
* 🏆 [Advanced Topics](#advanced-topics)
  * [Dependency Management](#dependency-management)
  * [Operaton REST API and Webapps](#operaton-rest-api-and-webapps)
  * [Operaton Enterprise Edition (EE)](#operaton-enterprise-edition-ee)
  * [Process Engine Plugins](#process-engine-plugins)
  * [Custom Process Engine Configuration](#custom-process-engine-configuration)
  * [Custom Job Executor Configuration](#custom-job-executor-configuration)
  * [Transaction Management](#transaction-management)
  * [Performance](#performance)
  * [Architectural Design](#architectural-design)
  * [Keycloak](#keycloak)
  * [Eventing Bridge](#eventing-bridge)
  * [Process Tests](#process-tests)
  * [Docker](#docker)
  * [Updating Operaton](#updating-operaton)
  * [Pitfalls](#pitfalls)
* 📚 [Releases](#releases)
* 📆 [Publications](#publications)
* 📨 [Contact](#contact)

# ✨Features
* Operaton can be integrated as an embedded process engine into a Micronaut project by simply [adding a dependency](#dependency-management) in build.gradle (Gradle) or pom.xml (Maven).
* Using H2 as an in-memory database is as simple as [adding a dependency](#dependency-management). Other [data sources can be configured](#data-source) via properties.
* BPMN process models, DMN decision tables, and Operaton Forms are [automatically deployed](#deploying-models) for all configured locations.
* The Operaton process engine with its job executor is started automatically - but the job executor is disabled for tests by default.
* The process engine and related services, e.g. RuntimeService, RepositoryService, ..., are provided as lazy initialized beans and [can be injected](#operaton-integration).
* Micronaut beans are resolved from the application context if they are [referenced by expressions or Java class names](#java-delegates) within the process models.
* The process engine [integrates with Micronaut transaction manager](#transaction-management). Optionally, micronaut-data-jdbc or micronaut-data-jpa are supported.
* Eventing Bridge that maps Operaton Events to Micronaut ApplicationEvents.
* The process engine can be configured with [generic properties](#generic-properties).
* The [Operaton REST API and the Webapps](#operaton-rest-api-and-webapps) are supported (currently only for Jetty).
* The [Operaton Enterprise Edition (EE)](#operaton-enterprise-edition-ee) is supported.
* [Process Engine Plugins](#process-engine-plugins) are automatically activated on start.
* The job executor uses the Micronaut IO Executor's [thread pools](https://docs.micronaut.io/latest/guide/index.html#threadPools).
* The [process engine configuration](#custom-process-engine-configuration) and the [job executor configuration](#custom-job-executor-configuration) can be customized programmatically.
* An Operaton admin user is created if configured by [properties](#properties) and not present yet (including admin group and authorizations).

# 🚀Getting Started

This section describes what needs to be done to use `micronaut-operaton-bpm-feature` in a Micronaut project.

## Create Project with Micronaut Launch

This will take care of the following:
* If you don't explicitly select any database then an in-memory H2 will be included by default.
* The configuration file `application.yml`
  * enables the Webapps and the REST-API
  * is configured to create an admin user with credentials `admin`/`admin` with which you can login to http://localhost:8080
* Jetty will be pre-configured (instead of Netty) to support the Webapps and REST-API by default

All you need to do is save a process model in the resources, see the following section.

##  Deploying Models
BPMN process models (`*.bpmn`), DMN decision tables (`*.dmn`), and Operaton Forms (`*.form`) should be created with the [Operaton Modeler](https://operaton.com/products/operaton-bpm/modeler) (which is compatible with Operaton) and saved in the resources.

By default, only the root of the resources will be scanned, but with the [property](#properties) `operaton.locations` you can configure the locations.

When starting the application you'll see the log output: `Deploying model: classpath:xxxxxxx.bpmn`

If you deploy [Operaton Forms](https://docs.operaton.org/docs/documentation/reference/forms/) then you can reference these from your user tasks by either
* defining the form as type "Operaton Form" and setting the "Form Ref", e.g. `ExampleForm` (Preferred solution supported by Camunda Modeller 4.11 and newer).
* defining the form as type "Embedded or External Task Forms" and setting the "Form Key", e.g. `camunda-forms:deployment:example.form` (Supported by Camunda Modeller 4.10 and newer).

## Operaton Integration

### Process Engine and Services

Inject the process engine or any of the Operaton services using constructor injection:
```java
import jakarta.inject.Singleton;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.RuntimeService;

@Singleton
public class MyComponent {

    private final ProcessEngine processEngine;
    private final RuntimeService runtimeService;
    
    public MyComponent(ProcessEngine processEngine, RuntimeService runtimeService) {
        this.processEngine = processEngine;
        this.runtimeService = runtimeService;
    }

    // ...
}
```

Alternatively to constructor injection, you can also use field injection, Java bean property injection, or method parameter injection.

You can then for example use the `runtimeService` to start new processes instances or correlate existing process instances.

### Java Delegates

To invoke a Java delegate create a bean and reference it in your process model using an expression, e.g. `${loggerDelegate}`:

```java
import jakarta.inject.Singleton;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;

@Singleton
public class LoggerDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(LoggerDelegate.class);

    @Override
    public void execute(DelegateExecution delegateExecution) {
        log.info("Hello World: {}", delegateExecution);
    }
}
```

Internally, the bean will be resolved using `io.micronaut.inject.qualifiers.Qualifiers.byName(...)`.
Therefore, you can use the annotation `jakarta.inject.Named` to define an explicit bean name and use that name in your expression.

## Configuration

### Data Source

By default, an in-memory H2 data source is preconfigured. Remember to add the runtime dependency `com.h2database:h2` mentioned in [Dependency Management](#dependency-management).

However, you can configure any other database supported by Operaton, e.g. in `application.yml`:

```yaml
datasources:
  default:
    url: jdbc:postgresql://localhost:5432/postgres
    username: postgres
    password: secret
    driver-class-name: org.postgresql.Driver
```

after adding the appropriate driver as a dependency:

```groovy
runtimeOnly("org.postgresql:postgresql:42.3.1")
```

### Connection Pool with HikariCP

This integration uses HikariCP as a database connection pool to optimize performance. By default, the following configuration is applied:
* `datasources.default.minimum-idle: 10`
* `datasources.default.maximum-pool-size: 50`

### Properties

You may use the following properties (typically in application.yml) to configure the Operaton integration.

| Prefix                |Property          | Default                                                                          | Description            |
|-----------------------|------------------|----------------------------------------------------------------------------------|------------------------|
| operaton               | .locations       | classpath:.                                                                      | List of locations to scan for model files (default is the resources's root only) |
| operaton.admin-user    | .id              |                                                                                  | If present, an Operaton admin account will be created by this id (including admin group and authorizations) |
|                       | .password        |                                                                                  | Admin's password (mandatory if the id is present)  |
|                       | .firstname       |                                                                                  | Admin's first name (optional, defaults to the capitalized id) |
|                       | .lastname        |                                                                                  | Admin's last name (optional, defaults to the capitalized id) |
|                       | .email           |                                                                                  | Admin's email address (optional, defaults to &lt;id&gt;@localhost) |
| operaton.rest          | .enabled         | false                                                                            | Enable the REST API |
|                       | .context-path    | /engine-rest                                                                     | Context path for the REST API |
|                       | .basic-auth-enabled | false                                                                            | Enables basic authentication for the REST API |
|                       | .authentication-provider | org.operaton.bpm.engine. rest.security.auth.impl. HttpBasicAuthenticationProvider | Authentication Provider to use for the REST API |
| operaton.webapps       | .enabled         | false                                                                            | Enable the Webapps (Cockpit, Task list, Admin) |
|                       | .context-path    | /operaton                                                                         | Context path for the Webapps |
|                       | .index-redirect-enabled | true                                                                             | Registers a redirect from / to the Webapps |
| operaton.filter        | .create          |                                                                                  | Name of a "show all" filter for the task list |
| operaton               | .license-file    |                                                                                  | Provide a URL to a license file; if no URL is present it will check your classpath for a file called "operaton-license.txt" |

### Generic Properties

The process engine can be configured using generic properties listed in Operaton's Documentation: [Configuration Properties](https://docs.operaton.org/docs/documentation/user-guide/spring-boot-integration/configuration).

The properties can be set in kebab case (lowercase and hyphen separated) or camel case (indicating the separation of words with a single capitalized letter as written in Operaton's documentation). Kebab case is preferred when setting properties.

Some of the most relevant properties are:
* database-schema-update (databaseSchemaUpdate)
* history

Example:

```yaml
operaton:
  generic-properties:
    properties:
      history: audit
```

## Examples

Here are some example applications:
* [Onboarding Process](https://github.com/tobiasschaefer/micronaut-camunda-example-onboarding) with service tasks, user tasks, and message correlation.
* Simple [application with Java/Maven](https://github.com/tobiasschaefer/micronaut-camunda-example-java-maven)
* Simple [application with Kotlin/Gradle](https://github.com/tobiasschaefer/micronaut-camunda-example-kotlin-gradle)
* [Internal example application](/micronaut-camunda-bpm-example) used during development

## Supported JDKs

We officially support the following JDKs:
* JDK 17 (LTS)
* JDK 21 (LTS)
* JDK 25 (LTS)

# 🏆Advanced Topics

## Dependency Management

The Operaton integration works with both Gradle and Maven, but we recommend using Gradle because it has better Micronaut Support.

<details>
<summary>Click to show Gradle configuration</summary>

Add the dependency to the build.gradle file:
```groovy
implementation("info.novatec:micronaut-operaton-bpm-feature:2.20.0")
runtimeOnly("com.h2database:h2")
```
</details>

<details>
<summary>Click to show Maven configuration</summary>

Add the dependency to the pom.xml file:
```xml
<dependency>
  <groupId>info.novatec</groupId>
  <artifactId>micronaut-operaton-bpm-feature</artifactId>
  <version>2.19.o</version>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
</dependency>
```
</details>

Note: The module `micronaut-operaton-bpm-feature` includes the dependency `org.operaton.bpm:operaton-engine` which will be resolved transitively.

## Operaton REST API and Webapps

Currently, the Operaton REST API and Webapps (Cockpit, Task list, and Admin) are only supported on the server runtime Jetty.

If you create your project with Micronaut Launch the `jetty` feature will be preselected for you.

However, if you have an existing project, you have to set the micronaut runtime of your project to `jetty`, e.g.

<details>
<summary>Click to show Gradle configuration</summary>

micronaut-gradle-plugin configuration in build.gradle:

```groovy
micronaut {
    runtime("jetty")
    [...]
}
```
</details>

<details>
<summary>Click to show Maven configuration</summary>

micronaut-maven-plugin configuration in pom.xml:

```xml
<properties>
  [...]
  <micronaut.runtime>jetty</micronaut.runtime>
</properties>
```

You have to remove this dependency in the pom.xml:
```xml
<dependency>
  <groupId>io.micronaut</groupId>
  <artifactId>micronaut-http-server-netty</artifactId>
  <scope>compile</scope>
</dependency>
```
and replace it with
```xml
<dependency>
  <groupId>io.micronaut.servlet</groupId>
  <artifactId>micronaut-http-server-jetty</artifactId>
</dependency>
```
</details>

### Configuration of REST API and Webapps
By default, REST API and the Webapps are not enabled. You have to configure them e.g. in the application.yaml as follows:

```yaml
operaton:
  webapps:
    enabled: true
  rest:
    enabled: true
```

Further Information:
* The Webapps are by default available at `/operaton`. By default, `/` will redirect you there.
* The REST API is by default available at `/engine-rest`, e.g. to get the engine name use `GET /engine-rest/engine`.
* See [Configuration Properties](#properties) on how to enable basic authentication for REST, create a default user, or disable the redirect.
* Enabling the REST API or the Webapps impacts the startup time. Depending on your hardware it increases by around 500-1000 milliseconds.

### Advanced Webapps Configuration
The security of the Webapps can be configured with the following properties:
<details>
  <summary>Click to show configuration options.</summary>

| Prefix                | Property          | Default                                      | Description            |
|-----------------------|------------------|----------------------------------------------|------------------------|
| operaton.webapps.header-security | .xss-protection-disabled | false | The header can be entirely disabled if set to true. |
|                       | .xss-protection-option| BLOCK | The allowed set of values: BLOCK - If the browser detects a cross-site scripting attack, the page is blocked completely; SANITIZE - If the browser detects a cross-site scripting attack, the page is sanitized from suspicious parts (value 0). Note: Is ignored when xss-protection-disabled is set to true and cannot be set in conjunction with xss-protection-value |
|                       | .xss-protection-value| 1; mode=block | A custom value for the header can be specified. Is ignored when xss-protection-disabled is set to true and cannot be set in conjunction with xss-protection-option. |
|                       | .content-security-policy-disabled | false| The header can be entirely disabled if set to true. |
|                       | .content-security-policy-value | base-uri 'self' | A custom value for the header can be specified. Note: Property is ignored when content-security-policy-disabled is set to true. |
|                       | .content-type-options-disabled | false | The header can be entirely disabled if set to true. |
|                       | .content-type-options-value | | A custom value for the header can be specified. Note: Property is ignored when content-security-policy-disabled is set to true. |
|                       | .hsts-disabled | true | Set to false to enable the header. |
|                       | .hsts-max-age | 31536000 | Amount of seconds, the browser should remember to access the webapp via HTTPS. Note: Is ignored when hstsDisabled is true, Cannot be set in conjunction with hstsValue, and allows a maximum value of 2^31-1. |
|                       | .hsts-include-subdomains-disabled | true | HSTS is additionally to the domain of the webapp enabled for all its subdomains. Note: Is ignored when hstsDisabled is true and cannot be set in conjunction with hstsValue. |
|                       | .hsts-value | max-age=31536000 | A custom value for the header can be specified. Note: Is ignored when hstsDisabled is true and cannot be set in conjunction with hstsMaxAge or hstsIncludeSubdomainsDisabled. |
| operaton.webapps.csrf  | .target-origin | | Sets the application expected deployment domain. |
|                       | .deny-status | | Sets the HTTP response status code used for a denied request. |
|                       | .random-class | | Sets the name of the class used to generate tokens. |
|                       | .entry-points | | Sets additional URLs that will not be tested for the presence of a valid token. |
|                       | .enable-secure-cookie | false | If true, the cookie flag Secure is enabled. |
|                       | .enable-same-site-cookie | true | If set to false, the cookie flag SameSite is disabled. The default value of the cookie is LAX and it can be changed via same-site-cookie-option configuration property. |
|                       | .same-site-cookie-option | | Can be configured either to STRICT or LAX. Note: Is ignored when enable-same-site-cookie is set to false and cannot be set in conjunction with same-site-cookie-value. |
|                       | .same-site-cookie-value | | A custom value for the cookie property. Note: Is ignored when enable-same-site-cookie is set to false and cannot be set in conjunction with same-site-cookie-option. |
|                       | .cookie-name | | A custom value to change the cookie name. Default ist 'XSRF-Token'. Note: Please make sure to additionally change the cookie name for each webapp (e.g. Cockpit) separately. |

</details>

## Operaton Enterprise Edition (EE)
### Add Maven Coordinates

Note: Operaton is fully open-source. If you previously used Camunda Enterprise Edition, you may need to migrate your dependencies. Contact the [Operaton community](https://forum.operaton.org/) for guidance on enterprise features.

## Process Engine Plugins
Every bean that implements the interface `org.operaton.bpm.engine.impl.cfg.ProcessEnginePlugin` is automatically added to the process engine's configuration on start.

You can either
* implement a bean factory with `@io.micronaut.context.annotation.Factory` and add one or more methods returning `ProcessEnginePlugin` instances and annotate each with a bean scope annotation
* annotate your class with `@jakarta.inject.Singleton` and implement the `ProcessEnginePlugin` interface

Example with the LDAP plugin:

```groovy
implementation("org.operaton.bpm.identity:operaton-identity-ldap:1.0.3")
```

```java
import io.micronaut.context.annotation.Factory;
import org.operaton.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.operaton.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;
import jakarta.inject.Singleton;

@Factory
public class PluginConfiguration {

    @Singleton
    public ProcessEnginePlugin ldap() {
        // Using a public online LDAP:
        // https://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/
        LdapIdentityProviderPlugin ldap = new LdapIdentityProviderPlugin();
        ldap.setServerUrl("ldap://ldap.forumsys.com:389");
        ldap.setManagerDn("cn=read-only-admin,dc=example,dc=com");
        ldap.setManagerPassword("password");
        ldap.setBaseDn("dc=example,dc=com");
        return ldap;
    }
}
```

You can now log in with "einstein" / "password". Note: the automatic creation of the admin user with the property `operaton.admin-user` conflicts with a read-only LDAP and must not be set!

## Custom Process Engine Configuration
With the following bean it's possible to customize the process engine configuration:

```java
import info.novatec.micronaut.operaton.bpm.feature.MnProcessEngineConfiguration;
import info.novatec.micronaut.operaton.bpm.feature.ProcessEngineConfigurationCustomizer;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;

@Singleton
@Replaces(ProcessEngineConfigurationCustomizer.class)
public class MyProcessEngineConfigurationCustomizer implements ProcessEngineConfigurationCustomizer {
    @Override
    public void customize(MnProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setProcessEngineName("CustomizedEngine");
    }
}
```

## Custom Job Executor Configuration
With the following bean it's possible to customize the job executor:

```java
import info.novatec.micronaut.operaton.bpm.feature.JobExecutorCustomizer;
import info.novatec.micronaut.operaton.bpm.feature.MnJobExecutor;
import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;

@Singleton
@Replaces(JobExecutorCustomizer.class)
public class MyJobExecutorCustomizer implements JobExecutorCustomizer {
    @Override
    public void customize(MnJobExecutor jobExecutor) {
        jobExecutor.setWaitTimeInMillis(300);
    }
}
```

## Transaction management

By default the process engine integrates with Micronaut's transaction manager and uses a Hikari connection pool:
* When interacting with the process engine, e.g. starting or continuing a process, the existing transaction will be propagated.
* JavaDelegates and Listeners will have the surrounding Operaton transaction propagated to them allowing the atomic persistence of data.

Optionally, `micronaut-data-jdbc` or `micronaut-data-jpa` are supported.

### Using micronaut-data-jdbc

To enable embedded transactions management support **with micronaut-data-jdbc** please add the following dependencies to your project:

<details>
<summary>Click to show Gradle dependencies</summary>

```groovy
annotationProcessor("io.micronaut.data:micronaut-data-processor")
implementation("io.micronaut.data:micronaut-data-jdbc")
```
</details>

<details>
<summary>Click to show Maven dependencies</summary>

```xml
<dependency>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-jdbc</artifactId>
</dependency>
```

And also add the annotation processor to every (!) `annotationProcessorPaths` element:

```xml
<path>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-processor</artifactId>
  <version>${micronaut.data.version}</version>
</path>
```
</details>

and then configure the JDBC properties as described [micronaut-sql documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/#jdbc).

### Using micronaut-data-jpa

To enable embedded transactions management support **with micronaut-data-jpa** please add the following dependencies to your project:

<details>
<summary>Click to show Gradle dependencies</summary>

```groovy
annotationProcessor("io.micronaut.data:micronaut-data-processor")
implementation("io.micronaut.data:micronaut-hibernate-jpa")
```
</details>

<details>
<summary>Click to show Maven dependencies</summary>

```xml
<dependency>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-hibernate-jpa</artifactId>
</dependency>
```

And also add the annotation processor to every (!) `annotationProcessorPaths` element:

```xml
<path>
  <groupId>io.micronaut.data</groupId>
  <artifactId>micronaut-data-processor</artifactId>
  <version>${micronaut.data.version}</version>
</path>
```
</details>

and then configure JPA as described in [micronaut-sql documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/#hibernate).

## Performance

The Time to First Response (TTFR) is mainly influenced by the (slow) bootstrapping of the process engine - the bottleneck is the sequential parsing of over 50 MyBatis mappings. But the REST api and WebApps also take time.

This Micronaut Operaton Integration includes some optimizations that come into play especially in multi-core environments.

Some hints:
* Newer JDKs (JDK 17) are about 20% faster than older ones (JDK 8)
* More CPU cores are better to take advantage of parallelization during startup (so don't limit Docker to one or two CPUs).
* The selected vendor for the JDK has a small influence (Java SE Development Kit from Oracle is one of the faster ones) 

Most discussions regarding startup time discuss the relevance during deployment or scaling up applications.

However, there is more: for a developer startup times are also relevant when test suites are executed - either locally or in a CI environment. If the application context is created often (e.g. @MockBean dirties the context in Spring Boot...) then integration tests run quite long.

If unit and integration tests are more of a conceptional difference (and not so much regarding performance) then the developer has more freedom of choice to decide if a unit or integration test is more appropriate for his scenario.

The documentation of [Micronaut Test](https://micronaut-projects.github.io/micronaut-test/latest/guide/) actually says: "One of the design goals of Micronaut was to eliminate the artificial separation imposed by traditional frameworks between function and unit tests due to slow startup times and memory consumption."

## Architectural Design

### Separating Process Engine from Webapps/REST

If you want to activate the Webapps and/or REST it might be an option to do this in a separate application. Both are connected via a common database.

Possible aspects:
* Your main process engine can run on Netty while the Webapps and/or REST run on Jetty (with a disabled Job Executor).
* You can scale these applications independently.

### Embedding External Workers

If you're intending to use the [External Task Pattern](https://docs.camunda.org/manual/latest/user-guide/process-engine/external-tasks/#the-external-task-pattern) it might be an option embedding them (at first) in your main application - and having them to communicate via REST on localhost.

Possible aspects:
* Having a lot of separate applications for each external worker from the beginning increases the complexity. You can put all external workers in one application - or even in the main application which provides the process engine.
* If you decide later to extract single modules to a separate microservice then this is straight forward

Here is an example application: https://github.com/tobiasschaefer/micronaut-embedded-worker

## Keycloak

You can enable the [Keycloak](https://www.keycloak.org/) integration to
* log into the Webapps (Tasklist, Cockpit, and Admin)
* get responses from the REST API with basic-auth enabled

1. Start Keycloak, e.g. `docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -e DB_VENDOR="h2" quay.io/keycloak/keycloak:12.0.4`
2. Configure Keycloak and add a test user.
3. Add a compatible Keycloak identity provider dependency to your Micronaut project (check [Operaton documentation](https://docs.operaton.org/) for available plugins)
4. Add the plugin:
```java
@Singleton
@ConfigurationProperties("plugin.identity.keycloak")
public class KeyCloakPlugin extends KeycloakIdentityProviderPlugin {
}
```
4. Configure the application.yml
```yaml
plugin.identity.keycloak:
  keycloakIssuerUrl: http://localhost:8080/auth/realms/master
  keycloakAdminUrl: http://localhost:8080/auth/admin/realms/master
  clientId: operaton-identity-service
  clientSecret: 42aa42bb-1234-4242-a24a-42a2b420cde1 # you get this from keycloak
  useEmailAsOperatonUserId: true
  administratorGroupName: operaton-admin
```
5. Start the application and log in with your created test user. Keep in mind that your user needs an e-mail address.

## Eventing Bridge

The Eventing Bridge maps Operaton Events to Micronaut [ApplicationEvents](https://docs.micronaut.io/latest/api/io/micronaut/context/event/ApplicationEvent.html). It's possible to configure three different
event streams:
* Task: All events depending on UserTasks (UserTasks are Created, Assigned, Completed)
* Execution: All execution events (Activities are Started, Ended and Transitions are being taken)
* History: All history events

### Configuration of Eventing Bridge
```yaml
operaton:
  eventing:
    task: true
    execution: true
    history: true
```

### Event Listener Implementation
To consume Micronaut ApplicationEvents you can implement the interface ApplicationEventListener or use the
@EventListener annotation.

<details>
<summary>Click to show example with ApplicationEventListener interface</summary>

```java
public class SampleEventListener implements ApplicationEventListener<TaskEvent> {
  private static final Logger log = LoggerFactory.getLogger(SampleEventListener.class);

  @Override
  public void onApplicationEvent(TaskEvent event) {
    log.info("new TaskEvent: EventName={}, Assignee={}", event.getEventName(), event.getAssignee());
  }
}
```
</details>

<details>
<summary>Click to show example with @EventListener</summary>

```java
@Singleton
public class SampleEventListener { 
  private static final Logger log = LoggerFactory.getLogger(SampleEventListener.class);

  @EventListener
  public void onExecutionEvent(ExecutionEvent event) {
    log.info("new ExecutionEvent: {}", event.getEventName());
  }

  @EventListener
  public void onTaskEvent(TaskEvent event) {
    log.info("new TaskEvent: {}", event.getEventName());
  }

  @EventListener
  public void onTaskEvent(HistoryEvent event) {
    log.info("new HistoryEvent: {}", event.getEventType());
  }
}
```
</details>

## Process Tests

Process tests can easily be implemented with JUnit 5 by adding the `operaton-bpm-assert` library as a dependency:

<details>
<summary>Click to show Gradle dependencies</summary>

```groovy
testImplementation("org.operaton.bpm:operaton-bpm-assert:1.0.3")
testImplementation("org.assertj:assertj-core")
```
</details>

<details>
<summary>Click to show Maven dependencies</summary>

```xml
<dependency>
  <groupId>org.operaton.bpm</groupId>
  <artifactId>operaton-bpm-assert</artifactId>
  <version>1.0.3</version>
  <scope>test</scope>
</dependency>
<dependency>
<groupId>org.assertj</groupId>
  <artifactId>assertj-core</artifactId>
  <version>3.21.0</version>
  <scope>test</scope>
</dependency>
```
</details>

and then implement the test using the usual `@MicronautTest` annotation:

```java
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;

@MicronautTest
class HelloWorldProcessTest {

    @Inject
    ProcessEngine processEngine;

    @Inject
    RuntimeService runtimeService;

    @BeforeEach
    void setUp() {
        init(processEngine);
    }

    @Test
    void happyPath() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HelloWorld");
        assertThat(processInstance).isStarted();

        assertThat(processInstance).isWaitingAt("TimerEvent_Wait");
        execute(job());

        assertThat(processInstance).isEnded();
    }
}
```

See also a test in our example application: [HelloWorldProcessTest](/micronaut-operaton-bpm-example/src/test/java/info/novatec/micronaut/operaton/bpm/example/HelloWorldProcessTest.java)

## Docker

When using Gradle we recommend the [Micronaut Application Plugin](https://github.com/micronaut-projects/micronaut-gradle-plugin/blob/master/README.md#micronaut-application-plugin)'s `dockerBuild` task to create a layered Docker image.

Build the Docker image:

`./gradlew dockerBuild`

Run the Docker image:

`docker run -p 8080:8080 <IMAGE>`

## Updating Operaton

Generally, follow Operaton's instructions regarding database updates in the [Operaton documentation](https://docs.operaton.org/docs/documentation/user-guide/process-engine/database/).

If you want to automate the database schema migration you can use [Liquibase](https://micronaut-projects.github.io/micronaut-liquibase/latest/guide/) or [Flyway](https://micronaut-projects.github.io/micronaut-flyway/latest/guide/) together with the migration SQL scripts available in the [Operaton GitHub repository](https://github.com/operaton/operaton/tree/main/database) or from [Maven Central](https://central.sonatype.com/search?q=org.operaton).

The following examples are based on Liquibase.

When starting on an empty database, e.g. when using H2 for tests:
```xml
<changeSet author="Tobias" id="1a" >
  <comment>Create common baseline Operaton 7.24 for H2 based on SQL scripts from https://github.com/operaton/operaton/tree/main/database or Maven Central</comment>
  <sqlFile path="operaton/h2_engine_1.0.3.sql" relativeToChangelogFile="true" dbms="h2" />
  <sqlFile path="operaton/h2_identity_1.0.3.sql" relativeToChangelogFile="true" dbms="h2" />
</changeSet>
```

If you already have a persistent database with the database schema which is not yet managed by Liquibase, e.g. PostgreSQL:
```xml
<changeSet author="Tobias" id="1b" >
  <comment>Create common baseline Operaton 7.24 for PostgreSQL (even if schema already exists) based on SQL scripts from https://github.com/operaton/operaton/tree/main/database</comment>
  <preConditions onFail="MARK_RAN">
    <not>
      <tableExists tableName="ACT_RU_JOB" />
    </not>
  </preConditions>
  <sqlFile path="operaton/postgres_engine_1.0.3.sql" relativeToChangelogFile="true" dbms="postgresql" />
  <sqlFile path="operaton/postgres_identity_1.0.3.sql" relativeToChangelogFile="true" dbms="postgresql" />
</changeSet>
```

When updating to a new Operaton version first apply all patch updates (if available) and then update to the next minor version:
```xml
<changeSet author="Tobias" id="2" >
  <comment>Update to Operaton version based on SQL scripts from https://github.com/operaton/operaton/tree/main/database</comment>
  <!-- Apply patch files if available, then upgrade to next version -->
  <sqlFile path="operaton/h2_engine_upgrade.sql" relativeToChangelogFile="true" dbms="h2" />
  <sqlFile path="operaton/postgres_engine_upgrade.sql" relativeToChangelogFile="true" dbms="postgresql" />
</changeSet>
```

## Pitfalls

### No version information in Fat/Uber/Shadow JAR

If you create a Fat/Uber/Shadow JAR and run that you will see a warning:

`WARN  i.n.m.c.b.f.MnProcessEngineConfiguration - The Operaton version cannot be determined. If you created a Fat/Uber/Shadow JAR then please consider using the Micronaut Application Plugin's 'dockerBuild' task to create a Docker image.`

This is because the repackaging of the jars implicitly removes the META-INF information.

Instead of creating a Fat/Uber/Shadow JAR, please see instructions on creating a [Docker](#docker) image and use the resulting image to run a Docker container.

### Executing Blocking Operations on Netty's I/O Thread Pool
When using the default server implementation Netty, blocking operations must be performed on I/O instead of Netty threads to avoid possible deadlocks. Therefore, as soon as Operaton ["borrows a client thread"](https://docs.operaton.org/docs/documentation/user-guide/process-engine/transactions-in-processes/)  you have to make sure that the [event loop is not blocked](https://objectcomputing.com/resources/publications/sett/june-2020-micronaut-2-dont-let-event-loops-own-you).
A frequently occurring example is the implementation of a REST endpoint which interacts with the process engine. By default, Micronaut would use a Netty thread for this blocking operation. To prevent the use of a Netty thread it is recommended to use the annotation [`@ExecuteOn(TaskExecutors.IO)`](https://docs.micronaut.io/latest/guide/index.html#reactiveServer). This will make sure that an I/O thread is used.

```java
@Post("/hello-world-process")
@ExecuteOn(TaskExecutors.IO)
public String startHelloWorldProcess() {
    return runtimeService.startProcessInstanceByKey("HelloWorld").getId();
}
```

### Operaton Platform Assertions - Multiple process tests
If you create multiple process tests, you need to add the following initialisation code in each test:

```java
@Inject
ProcessEngine processEngine;

@BeforeEach
void setUp() {
    init(processEngine);
}
```

This makes the assertions aware of your process engine. Otherwise, it tries to reuse the engine of the test that got
executed first and that may already be shut down, see [Operaton documentation](https://docs.operaton.org/docs/documentation/user-guide/testing/) for more details.

Here is a complete example: [HelloWorldProcessTest](/micronaut-operaton-bpm-example/src/test/java/info/novatec/micronaut/operaton/bpm/example/HelloWorldProcessTest.java).

# 📚Releases

The list of [releases](https://github.com/operaton/operaton/releases) contains a detailed changelog.

We use [Semantic Versioning](https://semver.org/).


Download of Releases:
* [GitHub Artifacts](https://github.com/operaton/operaton/releases)

# 📆Publications

Note: The following publications refer to the original Camunda 7 integration. This project has been migrated to use Operaton, a fork of Camunda 7.

* 2021-11: [Automating Processes with Camunda and Micronaut](https://www.youtube.com/watch?app=desktop&v=PwgrAb2z0YU&t=17m45s))
  Recording of the Novatec Summit by Tobias Schäfer (45 Minutes)
* 2021-10: [Camunda Question Corner](https://www.youtube.com/watch?v=o2-sgtXGIls&t=320s)
  Recording of the Community Contribution Special with Niall Deehan and Tobias Schäfer (10 minutes)
* 2021-07: [Automate any Process on Micronaut](https://camunda.com/blog/2021/07/automate-any-process-on-micronaut/)
  Blogpost by Tobias Schäfer
* 2021-02: [Automating Processes with Microservices on Micronaut and Camunda](https://micronaut.io/2021/02/25/webinar-micronaut-and-camunda/)
  Webinar by Tobias Schäfer, Bernd Rücker, and Sergio del Amo
* 2020-04: [Micronaut meets Camunda BPM](https://www.novatec-gmbh.de/en/blog/micronaut-meets-camunda-bpm/)
  Blogpost by Tobias Schäfer

# 📨Contact

If you have any questions or ideas feel free to create a topic in the [Operaton Forum](https://forum.operaton.org/) or contact us via [GitHub Discussions](https://github.com/camunda-community-hub/micronaut-camunda-platform-7/discussions).

We love listening to your feedback, and of course also discussing the project roadmap and possible use cases with you!

This open source project is being developed by [envite consulting GmbH](https://envite.de/) and [Novatec Consulting GmbH](https://www.novatec-gmbh.de/en/) with the support of the open source community.

---
![envite consulting GmbH](envite-black.png#gh-light-mode-only)
![envite consulting GmbH](envite-white.png#gh-dark-mode-only)
---
![Novatec Consulting GmbH](novatec.jpeg)
---
