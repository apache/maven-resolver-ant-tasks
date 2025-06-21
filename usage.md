# Using Maven Resolver Ant Tasks

See the [Maven Resolver Ant tasks usage documentation](https://maven.apache.org/resolver-ant-tasks/) for the main documentation.

Below are some additional pointers and examples.

To use the Maven Resolver Ant tasks, you need to include the Uber jar in your classpath. Either by copying it into you ant lib dir or by adding it to your classpath in your build script.

The Uber jar can be [downloaded from Maven central](https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-ant-tasks/1.5.2/maven-resolver-ant-tasks-1.5.2-uber.jar) or fetched to your local repository using maven:
```shell
mvn org.apache.maven.plugins:maven-dependency-plugin:3.8.1:get -Dartifact="org.apache.maven.resolver:maven-resolver-ant-tasks:1.5.2:jar:uber"
```

If you are using the Groovy Antbuilder, you can use grab to fetch the jar and add it to your classpath:
```groovy
@Grab('org.apache.maven.resolver:maven-resolver-ant-tasks:1.5.2')
@GrabConfig(systemClassLoader=true)
import groovy.ant.AntBuilder
def ant = new AntBuilder()
ant.with {
    // Your Ant tasks here
}
```

# Dependencies
There are two ways to define dependencies in your ant build using the Maven Resolver Ant tasks:
1. Create a pom file and use the pom task to register it.
2. Define your dependencies in the ant build file (e.g. build.xml).

After one of these is done, you can use the `resolve` task to resolve the dependencies and add them to your classpath.

## Example of defining dependencies in a pom file

## Example of defining dependencies in the ant build file


# Dependency Management

# Install
# Deploy