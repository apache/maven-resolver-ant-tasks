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
@GrabConfig(systemClassLoader=true)
/**
 * This is a workaround for some classloading issues when executing the script from ant
 * where @GrabConfig(systemClassLoader=true) does not work. Instead the classpath is set up
 * in ant and passed to the java command. When executed from the groovy command however,
 * @GrabConfig(systemClassLoader=true) is required since the maven resolver ant tasks needs
 * to be part of the same classloader as ant.
 */
class Runner {
  static void main(String[] args) {
    new GroovyShell(Runner.class.classLoader)
        .evaluate(new File('antBuilderExample.groovy'))
  }
}