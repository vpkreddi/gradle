/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.tasks.javadoc

import org.apache.commons.io.FileUtils
import org.gradle.api.internal.file.TestFiles
import org.gradle.api.tasks.javadoc.internal.JavadocToolAdapter
import org.gradle.jvm.toolchain.JavaInstallationMetadata
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.test.fixtures.AbstractProjectBuilderSpec
import org.gradle.util.TestUtil

class JavadocTest extends AbstractProjectBuilderSpec {
    def testDir = temporaryFolder.getTestDirectory()
    def destDir = new File(testDir, "dest")
    def srcDir = new File(testDir, "srcdir")
    def configurationMock = TestFiles.fixed(new File("classpath"))
    def tool = Mock(JavadocToolAdapter)

    Javadoc task

    def setup() {
        task = TestUtil.createTask(Javadoc, project)
        task.setClasspath(configurationMock)
        task.getJavadocTool().set(tool)
        tool.metadata >> Mock(JavaInstallationMetadata) {
            getLanguageVersion() >> JavaLanguageVersion.of(11)
        }
        FileUtils.touch(new File(srcDir, "file.java"))
    }

    def defaultExecution() {
        task.setDestinationDir(destDir)
        task.source(srcDir)

        when:
        execute(task)

        then:
        1 * tool.execute(!null)
    }

    def usesToolchainIfConfigured() {
        task.setDestinationDir(destDir)
        task.source(srcDir)

        when:
        task.getJavadocTool().set(tool)

        and:
        execute(task)

        then:
        1 * tool.metadata >> toolMetadata
        1 * toolMetadata.languageVersion >> JavaLanguageVersion.of(11)
        1 * tool.execute(!null)
    }

    def executionWithOptionalAttributes() {
        when:
        task.setDestinationDir(destDir)
        task.source(srcDir)
        task.setMaxMemory("max-memory")
        task.setVerbose(true)

        and:
        execute(task)

        then:
        1 * tool.execute(_)
    }
}
