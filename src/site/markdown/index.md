<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
# Maven Artifact Resolver Ant Tasks

The Maven Artifact Resolver Ant Tasks let [Apache Ant](https://ant.apache.org/) 1.7 and later build
scripts use [Maven Artifact Resolver](https://maven.apache.org/resolver/) together with the
[Apache Maven Artifact Resolver Provider](https://maven.apache.org/ref/current/maven-resolver-provider/)
to resolve dependencies, and to install and deploy locally built artifacts.

To integrate the tasks into your build file, follow these steps:

1. Copy the uber JAR file into the `lib` directory of your project.
1. To load the tasks, add the following snippet to your build file:

   ```xml
   <project xmlns:resolver="antlib:org.apache.maven.resolver.ant" ...>
     <taskdef uri="antlib:org.apache.maven.resolver.ant" resource="org/apache/maven/resolver/ant/antlib.xml"
       classpath="lib/maven-resolver-ant-tasks-${project.version}-uber.jar" />
     ...
   </project>
   ```

For a complete example build script, see the `build.xml` file in the project sources.

## Settings

The Ant tasks use the standard [Apache Maven `settings.xml` file](/settings.html).
By default, the tasks read user settings from the `${user.home}/.m2/settings.xml` file.

For global settings, the tasks try the following paths in order:

* `${ant.home}/etc/settings.xml`
* `${maven.home}/conf/settings.xml`

To use different paths, set the `<settings>` element:

```xml
<settings file="my-settings.xml" globalfile="myglobal-settings.xml"/>
```

You can override some of the settings from the settings file or the POM in the Ant file itself.

<a id="Proxy_Settings"></a>

### Proxy settings

Proxy definitions apply to the whole session, and you can set more than one. For each remote
connection, the tasks evaluate the `nonProxyHosts` attribute of each proxy definition and use the
first proxy that matches.

```xml
<proxy host="proxy.mycorp.com" port="8080" type="http" nonProxyHosts="127.*|localhost|*.mycorp.com"/>
```

### Authentication

Authentication elements provide access to remote repositories. The tasks add every authentication
definition globally and select one based on the `servers` attribute. If you don't set that
attribute, then you must reference the authentication explicitly.

```xml
<authentication username="login" password="pw" id="auth"/>
<authentication privateKeyFile="file.pk" passphrase="phrase" servers="distrepo" id="distauth"/>
```

<a id="Local_Repository"></a>

### Local repository

You can use only one local repository at a time.

```xml
<localrepo dir="someDir"/>
```

<a id="Remote_Repositories"></a>

### Remote repositories

You can define remote repositories directly:

```xml
<remoterepo id="ossrh" url="https://oss.sonatype.org/content/repositories/snapshots/" type="default" releases="false" snapshots="true" updates="always" checksums="fail"/>

<remoterepo id="rao" url="https://repository.apache.org/content/groups/public/">
    <releases enabled="true" updates="daily" checksums="warn"/>
    <snapshots enabled="false"/>
    <authentication refid="auth"/>
</remoterepo>

<remoterepo id="distrepo" url="..." authref="distauth"/>
```

You can also use several repositories as a group anywhere that a single remote repository is valid:

```xml
<remoterepos id="all">
    <remoterepo refid="ossrh"/>
    <remoterepo refid="rao"/>
    <remoterepo refid="distrepo"/>
</remoterepos>
```

**Note:** For remote repositories, the tasks support only the `file`, `http`, and `https` protocols.

### Mirrors

To route requests for one or more repositories through a mirror, define a `<mirror>` element:

```xml
<mirror id="" url="" mirrorOf=""/>
```

<a id="Offline_Mode"></a>

### Offline mode

To suppress network activity and use only cached artifacts and metadata, set the
`resolver.offline` property to `true`:

```xml
<property name="resolver.offline" value="true"/>
```

## Project

Project settings describe the locally available information about the build.

### POM

The POM data type determines the target of the install and deploy tasks. If you define a POM from a
full `pom.xml` file and don't assign an id to it, then the install and deploy tasks use that POM by
default.

```xml
<pom file="pom.xml" id="pom"/>
<pom groupId="g" artifactId="a" version="v"/>
<pom coords="g:a:v"/>
```

#### Properties

If you set a POM through a file parameter, then the tasks expose its effective model as properties
of the Ant project. The property names use the ref id of the `<pom>` element as a prefix. In the
preceding example the id is `pom`, so the version is available as `${pom.version}`. Project
properties defined in the POM use the
`pom.properties.` prefix. If you don't assign an id, then the properties use the `pom.` prefix.

<a id="Output_Artifacts"></a>

### Output artifacts

The `<artifact>` elements define the artifacts that this build produces for installation or
deployment.

```xml
<artifact file="file-src.jar" type="jar" classifier="sources" id="src"/>

<artifacts id="producedArtifacts">
    <artifact refid="src"/>
    <artifact file="file-src.jar"/>
</artifacts>
```

### Dependencies

Dependencies create classpaths or filesets. The `<resolve>` task collects the artifacts of the
dependencies transitively.

```xml
<dependency coords="g:a:v:scope"/>

<dependency groupId="g" artifactId="a" version="v" classifier="c" type="jar" scope="runtime">
    <exclusion coords="g:a"/>
    <exclusion groupId="g" artifactId="a"/>
</dependency>

<dependencies id="deps">
    <dependency refid="first"/>
    <dependency refid="second"/>
    <exclusion coords="g:a"/> <!-- global exclusion for all dependencies of this group -->
</dependencies>

<dependencies>
    <dependency coords="test:artifact:1.0:runtime"/>
    <dependencies refid="deps"/> <!-- nested dependency collection merged into this one -->
</dependencies>

<dependencies id="depsFromPom" pomRef="pom"/>

<dependencies id="depsFromPlainTextFile" file="dependencies.txt"/>
<!--
Each non-empty line of that text file declares one dependency, using the same syntax as for the `coords` attribute
of the `<dependency>` element, that is
<groupId>:<artifactId>:<version>[[:<type>[:<classifier>]]:<scope>]
Everything after the first hash (#) character on a line is a comment.
-->
```

## Tasks

### Install

For the install task to work, set a POM that references a file.

```xml
<install artifactsref="producedArtifacts"/>
```

### Deploy

For the deploy task to work, set a POM that references a file. The task deploys that POM file to
the repository.

```xml
<deploy artifactsref="producedArtifacts">
    <remoterepo refid="distrepo"/>
    <snapshotrepo refid="snaprepo">
</deploy>
```

### Resolve

The `<resolve>` task collects and resolves dependencies from remote servers. If you don't set
repositories for the task, then the task uses the repositories referenced by
`resolver.repositories`, which contains only Maven Central. To override that reference, supply
another repository definition with the same id.

The task can assemble the collected dependencies in three ways:

* **Classpath**: the `<path>` element defines a classpath with all resolved dependencies.
* **Files**: the `<files>` element assembles a resource collection of all resolved dependencies,
  and can also copy the files to a directory.
* **Properties**: the `<properties>` element sets one property per artifact. The property name is
  the given prefix followed by the artifact coordinates, and the value is the path to the resolved
  file.

You can specify each of these targets more than once for the same resolve task, but you can specify
only one `<dependencies>` element.

```xml
<resolve failOnMissingAttachments="true">
    <dependencies>
        <dependency coords="org.apache.maven:maven-profile:2.0.6"/>
        <exclusion artifactId="junit"/>
        <exclusion groupId="org.codehaus.plexus"/>
    </dependencies>
    <path refid="cp" classpath="compile"/>
    <files refid="src.files" attachments="sources" dir="target/sources"
           layout="{artifactId}-{classifier}.{extension}"/>
    <files refid="api.files" attachments="javadoc" dir="target/javadoc"
           layout="{artifactId}-{classifier}.{extension}"/>
    <properties prefix="dep." scopes="provided,system"/>
</resolve>

<resolve dependenciesref="deps">
    <path refid="cp.compile" classpath="compile"/>
    <path refid="cp.test" classpath="test"/>
</resolve>
```

You can set scope filters on every target to list the included and the excluded scope names. To
exclude a scope, prefix its name with `-` or `!`. For example, `provided,!system` includes the
provided scope and excludes the system scope.

The `classpath` attribute is a shortcut for scope filters. For example, `classpath="compile"` is
equivalent to `scope="provided,system,compile"`. The valid values are `compile`, `runtime`, and
`test`.

```xml
<resolve>
    <dependencies pomRef="pom"/>
    <remoterepos refid="all"/>
    <path refid="cp" classpath="compile"/>
    <path refid="tp" classpath="test"/>
</resolve>
```

You can use the `layout` attribute of the `<files>` element only when you also set the `dir`
attribute. The attribute accepts the following placeholders, which refer to the coordinates of the
artifact that the task is processing:

* `{groupId}`: for example, `org.apache.maven.resolver`
* `{groupIdDirs}`: for example, `org/apache/maven/resolver`
* `{artifactId}`: for example, `maven-resolver-api`
* `{version}`: for example, `1.0.0-20140518.181353-123`
* `{baseVersion}`: for example, `1.0.0-SNAPSHOT`
* `{extension}`: for example, `jar`
* `{classifier}`: for example, `sources`

## More information

For more information, see the [usage guide](https://github.com/apache/maven-resolver-ant-tasks/blob/master/usage.md).

The [examples directory](https://github.com/apache/maven-resolver-ant-tasks/tree/master/examples)
contains seven complete examples of ways to use the Maven Resolver Ant Tasks.
