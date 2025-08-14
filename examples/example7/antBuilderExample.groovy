/*
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
*/
@Grab('org.apache.maven.resolver:maven-resolver-ant-tasks:1.5.2')
@Grab('org.codehaus.plexus:plexus-xml:4.1.0')
@Grab(group='org.slf4j', module='slf4j-simple', version='2.0.17')
import groovy.ant.AntBuilder
def ant = new AntBuilder()
ant.with {
  typedef(name:"dependency", classname:"org.apache.maven.resolver.internal.ant.types.Dependency")
  typedef(name:"dependencies", classname:"org.apache.maven.resolver.internal.ant.types.Dependencies")
  taskdef(name:"resolve", classname:"org.apache.maven.resolver.internal.ant.tasks.Resolve")

  property(name:'mainBuildDir', value: '${basedir}/target/classes')

  delete dir: '${mainBuildDir}'
  dependencies(id:'compile') {
    dependency groupId: 'org.apache.commons', artifactId: 'commons-lang3', version: '3.18.0'
  }
  resolve(dependenciesref:'compile') {
    path refId: 'compilePath', classpath:'compile'
  }
  mkdir(dir: '${mainBuildDir}')
  javac srcdir:'${basedir}/src/main/java', destdir:'${mainBuildDir}', classpathref:'compilePath', includeantruntime:'false'
}