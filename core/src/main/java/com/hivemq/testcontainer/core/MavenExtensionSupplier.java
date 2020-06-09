/*
 * Copyright 2020 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hivemq.testcontainer.core;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.PomEquippedEmbeddedMaven;
import org.testcontainers.shaded.com.google.common.io.Files;

import java.io.File;
import java.util.function.Supplier;

/**
 * @author Yannick Weber
 */
public class MavenExtensionSupplier implements Supplier<File> {

    private final @NotNull String pomFile;

    public static @NotNull MavenExtensionSupplier direct() {
        return new MavenExtensionSupplier("pom.xml");
    }

    public MavenExtensionSupplier(final @NotNull String pomFile) {
        this.pomFile = pomFile;
    }

    @Override
    public @NotNull File get() {
        final PomEquippedEmbeddedMaven embeddedMaven = EmbeddedMaven.forProject(pomFile);
        final BuiltProject aPackage = embeddedMaven.setGoals("package").build();
        final File targetDirectory = aPackage.getTargetDirectory();
        final String version = aPackage.getModel().getVersion();
        final String artifactId = aPackage.getModel().getArtifactId();

        final ZipFile zipFile = new ZipFile(new File(targetDirectory, artifactId + "-" + version + "-distribution.zip"));
        final File tempDir = Files.createTempDir();

        try {
            zipFile.extractAll(tempDir.getAbsolutePath());
        } catch (final ZipException e) {
            e.printStackTrace();
        }
        return new File(tempDir, artifactId);
    }
}