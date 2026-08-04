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

The Maven Artifact Resolver Ant Tasks enable [Apache Ant](https://ant.apache.org/) 1.7+ build scripts to use
the [Maven Artifact Resolver](https://maven.apache.org/resolver/).
The tasks combine Maven Artifact Resolver with the
[Apache Maven Artifact Resolver Provider](https://maven.apache.org/ref/current/maven-resolver-provider/)
to resolve dependencies.
The tasks also install and deploy locally built artifacts.

To integrate the tasks into your build file, copy the Über JAR into your project's lib directory.
Use the following snippet to load the tasks:

```xml
<project xmlns:resolver="antlib:org.apache.maven.resolver.ant" ...>
  <taskdef uri="antlib:org.apache.maven.resolver.ant" resource="org/apache/maven/resolver/ant/antlib.xml"
    classpath="lib/maven-resolver-ant-tasks-${project.version}-uber.jar" />
  ...
</project>
```

See the `build.xml` in the project sources for a complete example build script.

## Settings

The Ant tasks use the usual [Apache Maven settings.xml](/settings.html).
By default, the tasks use the `${user.home}/.m2/settings.xml` file for user settings.

For global settings, the tasks try different paths:

* `${ant.home}/etc/settings.xml`
* `${maven.home}/conf/settings.xml`

The `<settings/>` definition changes these paths:

```xml
<settings file="my-settings.xml" globalfile="myglobal-settings.xml"/>
```

You can also change some settings in the Ant file.
These settings come from the settings file or the POM.

### Proxy Settings

Proxy definitions apply to the whole session.
You can set multiple proxies.
The tasks evaluate the `nonProxyHosts` attribute on each proxy definition to choose the proxy.
The first proxy that matches is used for a given remote connection.

```xml
<proxy host="proxy.mycorp.com" port="8080" type="http" nonProxyHosts="127.*|localhost|*.mycorp.com"/>
```

### Authentication

Authentication elements provide access to remote repositories.
Every authentication definition is added globally.
The tasks choose the definition by the `servers` attribute.
If you do not set this attribute, the authentication must be referenced explicitly.

```xml
<authentication username="login" password="pw" id="auth"/>
<authentication privateKeyFile="file.pk" passphrase="phrase" servers="distrepo" id="distauth"/>
```

### Local Repository

Only one local repository can be used at a time.

```xml
<localrepo dir="someDir"/>
```

### Remote Repositories

Remote repositories can be defined directly:

```xml
<remoterepo id="ossrh" url="https://oss.sonatype.org/content/repositories/snapshots/" type="default" releases="false" snapshots="true" updates="always" checksums="fail"/>

<remoterepo id="rao" url="https://repository.apache.org/content/groups/public/">
    <releases enabled="true" updates="daily" checksums="warn"/>
    <snapshots enabled="false"/>
    <authentication refid="auth"/>
</remoterepo>

<remoterepo id="distrepo" url="..." authref="distauth"/>
```

Multiple repositories can be used as a group in every place that is legal for a
remote repository:

```xml
<remoterepos id="all">
    <remoterepo refid="ossrh"/>
    <remoterepo refid="rao"/>
    <remoterepo refid="distrepo"/>
</remoterepos>
```

*Note:* Currently, only file:, http: and https: protocols are supported for remote repositories.

### Mirrors

```xml
<mirror id="" url="" mirrorOf=""/>
```

### Offline Mode

To suppress network activity and use only cached artifacts and metadata, set this boolean property:

```xml
<property name="resolver.offline" value="true"/>
```

## Project

Project settings use locally available information about the build.

### POM

The POM is the data type that determines the target for the install and deploy tasks.
If you define a POM without an id based on a full `pom.xml` file, that POM is the default.
The tasks use the default POM for install and deploy.

```xml
<pom file="pom.xml" id="pom"/>
<pom groupId="g" artifactId="a" version="v"/>
<pom coords="g:a:v"/>
```

#### Properties

If you set a POM via a file parameter, its effective model is available as properties to the Ant project.
The properties use the ref id of the `<pom>` element as the prefix.
For the POM above, the prefix is `${pom.version}`.
Project properties defined in the POM also use the prefix `pom.properties.`.
If you assign no id, the properties use the prefix `pom.` by default.

### Output Artifacts

The `<artifact>` elements define the artifacts that this build produces for install or deploy.

```xml
<artifact file="file-src.jar" type="jar" classifier="sources" id="src"/>

<artifacts id="producedArtifacts">
    <artifact refid="src"/>
    <artifact file="file-src.jar"/>
</artifacts>
```

### Dependencies

Dependencies create classpaths or filesets.
The `<resolve>` task collects the dependency artifacts transitively.

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
of the `<dependency>` element, i.e.
<groupId>:<artifactId>:<version>[[:<type>[:<classifier>]]:<scope>]
Everything after the first hash (#) character on a line is considered a comment.
-->
```

## Tasks

### Install

Set a POM that references a file for the install task to work.

```xml
<install artifactsref="producedArtifacts"/>
```

### Deploy

Set a POM that references a file for the deploy task to work.
The deploy task deploys that POM file to the repository.

```xml
<deploy artifactsref="producedArtifacts">
    <remoterepo refid="distrepo"/>
    <snapshotrepo refid="snaprepo">
</deploy>
```

### Resolve

The `<resolve>` task collects and resolves dependencies from remote servers.
If no repositories are set for the task, the task uses the `resolver.repositories` reference.
This reference contains only central by default.
You can override it with another repository definition that uses this id.


The task can assemble the collected dependencies in three ways:

* Classpath: The `<path>` element defines a classpath with all resolved dependencies.
* Files: The `<files>` element assembles a resource collection with all resolved dependencies.
  The element can also copy the files to a directory.
* Properties: The `<properties>` element sets properties with the given prefix.
  The name of a property is the prefix plus the coordinates.
  The value of a property is the path to the resolved file.

These targets can be mentioned more than once for the same resolve task.
Only one `<dependencies>` element is allowed.

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

You can set scope filters on every target.
The filters list the included and excluded scope names.
An exclusion is denoted by a `-` or `!` prefix on the scope name.
The value `provided,!system` is an example.

The `classpath` attribute is a shortcut for the scope filters.
For example, `classpath="compile"` equals `scope="provided,system,compile"`.
Valid values are `compile`, `runtime`, and `test`.

```xml
<resolve>
    <dependencies pomRef="pom"/>
    <remoterepos refid="all"/>
    <path refid="cp" classpath="compile"/>
    <path refid="tp" classpath="test"/>
</resolve>
```

The layout attribute of the `<files>` element is only allowed when the `dir` attribute is also given.
The attribute recognizes the placeholders below.
The placeholders refer to the coordinates of the processed artifact:

* `{groupId}`, for example "org.apache.maven.resolver"
* `{groupIdDirs}`, for example "org/apache/maven/resolver"
* `{artifactId}`, for example "maven-resolver-api"
* `{version}`, for example "1.0.0-20140518.181353-123"
* `{baseVersion}`, for example "1.0.0-SNAPSHOT"
* `{extension}`, for example "jar"
* `{classifier}`, for example "sources"

# More information
See [usage.md](https://github.com/apache/maven-resolver-ant-tasks/blob/master/usage.md) for more information.

The [examples](https://github.com/apache/maven-resolver-ant-tasks/tree/master/examples) directory contains 7 complete examples of how to use Maven Resolver Ant Tasks.
