/*
 * Copyright 2021 original authors
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
package info.novatec.micronaut.operaton.bpm.feature;

import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;
import org.operaton.bpm.engine.ProcessEngine;

import java.util.Optional;

/**
 * @author Paty Alonso
 */

//Implementation based on https://github.com/camunda/camunda-bpm-platform/blob/master/spring-boot-starter/starter/src/main/java/org/operaton/bpm/spring/boot/starter/util/CamundaBpmVersion.java
@Singleton
public class OperatonVersion {

    private final Optional<String> version;

    public OperatonVersion() {
        this(ProcessEngine.class.getPackage());
    }

    protected OperatonVersion(Package pkg) {
        version = Optional.ofNullable(pkg.getImplementationVersion())
                .map(String::trim);
    }

    public OperatonVersion(@Nullable String version) {
        this.version = Optional.ofNullable(version);
    }

    public Optional<String> getVersion() {
        return version;
    }
}
