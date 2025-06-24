<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
# Using Maven Resolver Ant Tasks

See the [Maven Resolver Ant tasks usage documentation](https://maven.apache.org/resolver-ant-tasks/) for the main documentation.

The javadoc for the Maven Resolver Ant tasks can be found [here](https://maven.apache.org/resolver-ant-tasks/apidocs/).

Below are some additional pointers and examples.

To use the Maven Resolver Ant tasks, you need to include the Uber jar in your classpath. Either by copying it into you ant lib dir or by adding it to your classpath in your build script.

The Uber jar can be [downloaded from Maven central](https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-ant-tasks/1.5.2/maven-resolver-ant-tasks-1.5.2-uber.jar) or fetched to your local repository using maven:
```shell
mvn org.apache.maven.plugins:maven-dependency-plugin:3.8.1:get -Dartifact="org.apache.maven.resolver:maven-resolver-ant-tasks:1.5.2:jar:uber"
```

If you are using the Groovy Antbuilder, you can use grab to fetch the normal jar and its dependencies add them to your classpath:
```groovy
@Grab('org.apache.maven.resolver:maven-resolver-ant-tasks:1.5.2')
@Grab('org.codehaus.plexus:plexus-xml:4.1.0')
@Grab(group='org.slf4j', module='slf4j-simple', version='2.0.17', scope='test')
@GrabConfig(systemClassLoader=true)
import groovy.ant.AntBuilder
def ant = new AntBuilder()
ant.with {
  /* Calling antlib.xml to register the Maven Resolver Ant tasks does not work 
  with Groovy AntBuilder. You need to use the taskdef and typedef elements 
  directly.*/
  typedef(name:"dependency", classname:"org.apache.maven.resolver.internal.ant.types.Dependency")
  typedef(name:"dependencies", classname:"org.apache.maven.resolver.internal.ant.types.Dependencies")
  taskdef(name:"resolve", classname:"org.apache.maven.resolver.internal.ant.tasks.Resolve")

  property(name:'mainBuildDir', value: '${basedir}/target/classes')

  dependencies(id:'compile') {
    dependency groupId: 'org.apache.commons', artifactId: 'commons-lang3', version: '3.17.0'
  }
  resolve(dependenciesref:'compile') {
    path refId: 'compilePath', classpath:'compile'
  }
  mkdir(dir: '${mainBuildDir}')
  javac srcdir:'${basedir}/src/main/java', destdir:'${mainBuildDir}', classpathref:'compilePath', includeantruntime:'false'
  // etc...
}
```
[example7](examples/example7) shows how to use the Maven Resolver Ant tasks 
in a Groovy script using AntBuilder.

# Dependencies
There are two ways to define dependencies in your ant build using the Maven Resolver Ant tasks:
1. Create a pom file and use the pom task to register it.
2. Define your dependencies in the ant build file (e.g. build.xml).

After one of these is done, you can use the `resolve` task to resolve the dependencies and add them to your classpath.

You can find fully working examples in the [examples directory](examples) but here is a brief overview:

## Example of defining dependencies in a pom file
1. Create a minimal pom file with the dependencies you want to use. For example, create a file named `pom.xml` with the following content:
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.mygroup</groupId>
  <artifactId>example1</artifactId>
  <version>1.0-SNAPSHOT</version>
  <name>Example 1</name>
  <dependencies>
    <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-lang3</artifactId>
      <version>3.17.0</version>
    </dependency>
  </dependencies>
</project>
```

2. In your ant build file, use the `pom` task to register the pom file:
```xml
<project xmlns:repo="antlib:org.apache.maven.resolver.ant">
  <!-- define the maven resolver ant tasks types and tasks -->
  <taskdef uri="antlib:org.apache.maven.resolver.ant" resource="org/apache/maven/resolver/ant/antlib.xml"/>
  
  <target name="configurePaths" description="Configure paths for compilation using the pom file for dependencies">
    <repo:pom file="${basedir}/pom.xml"/>
    <repo:resolve>
      <path refId='compilePath' classpath='compile'/>
    </repo:resolve>
  </target>
  
  <target name='compile' depends='init' description="Compile the main classes using the path we set up with resolve">
    <property name="mainBuildDir" value="${basedir}/target/classes"/>
    <mkdir dir="${mainBuildDir}"/>
    <javac srcdir="${basedir}/src/main/java" destdir="${mainBuildDir}" classpathref="compilePath" includeantruntime="false"/>
  </target>
</project>
```
See [example1](examples/example1) for a complete example of using a pom file to define dependencies.

## Example of defining dependencies in the ant build file
```xml
<project xmlns:repo='antlib:org.apache.maven.resolver.ant'>

  <!-- define the maven resolver ant tasks types and tasks -->
  <taskdef uri='antlib:org.apache.maven.resolver.ant' resource='org/apache/maven/resolver/ant/antlib.xml'/>
  
  <repo:dependencies id='compile'>
    <repo:dependency groupId='org.apache.commons' artifactId='commons-lang3' version='3.17.0'/>
  </repo:dependencies>

  <target name='configurePaths' description='Configure paths for compilation using the pom file for dependencies'>
    <repo:resolve dependenciesref='compile'>
      <path refId='compilePath' classpath='compile'/>
    </repo:resolve>
  </target>

  <target name='compile' depends='init' description='Compile the main classes using the path we set up with resolve'>
    <property name="mainBuildDir" value="${basedir}/target/classes"/>
    <mkdir dir='${mainBuildDir}'/>
    <javac srcdir='${basedir}/src/main/java' destdir='${mainBuildDir}' classpathref='compilePath' includeantruntime='false'/>
  </target>
</project>
```
See [example2](examples/example2) for a complete example of defing dependencies in your ant build file.

# Dependency Management
There are two ways to handle bill of materials (BOM's) using mavens  dependency management in your Ant build:
1. **Using a POM file**: You can create a POM file that includes the BOM in its dependency management section. Then, use the `pom` task to register this POM file in your Ant build script.
2. **Using the `dependencyManagement` tyope**: You can define the BOM directly in your Ant build script using the `dependencyManagement` type.

The syntax for both methods is similar, but the first method requires a separate POM file, while the second method allows you to define the BOM directly in your Ant build script.

[Example 5](examples/example5) shows how to use the `dependencyManagement` type directly in the Ant build script.

[Example 6](examples/example6) shows how to use a separate POM file for dependency management and dependencies in your project to use a BOM, and then register it with the `pom` task.

# Notes
## Using XML Namespaces in Apache Ant
Apache Ant does not enforce XML namespaces for nested elements of custom tasks in the same way XML parsers typically would. This is because Ant uses a custom XML parsing and object instantiation model, not a strict XML namespace-aware DOM or SAX parser. Here's how and why that works:
1. Ant uses introspection-based configuration
Ant's core processing looks for addXyz() or createXyz() methods in the task class to match nested elements (e.g., addLicenses() for `<licenses>`). These methods are matched by local element name, not by namespace.

2. Namespace is only used at task level (taskdef/typedef)
When you define a namespace like:

```xml
<project xmlns:repo='antlib:org.apache.maven.resolver.ant'>
  
</project>
```
you're associating the prefix repo: with a task library (a group of task definitions). This only matters for top-level tasks or types like:

```xml
<repo:createPom ... />
```
3. Nested elements are resolved contextually
Once a task like <repo:createPom> is instantiated, Ant assumes any nested elements are part of its internal structure, and it uses method names (like addLicenses) on the task class to resolve them. Namespaces are ignored here — Ant does not scope nested elements using XML namespaces.

### Example
This works:

```xml
<repo:createPom ...>
  <licenses>...</licenses> <!-- No namespace needed -->
</repo:createPom>
```
This also works:

```xml
<repo:createPom ...>
  <repo:licenses>...</repo:licenses> <!-- But this is not necessary -->
</repo:createPom>
```