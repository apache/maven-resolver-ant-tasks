# Using Maven Resolver Ant Tasks

See the [Maven Resolver Ant tasks usage documentation](https://maven.apache.org/resolver-ant-tasks/) for the main documentation.

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

# Dependency Management

# Install
# Deploy