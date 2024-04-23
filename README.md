JMTE: Java Minimal Template Language
====

[![Build Status](https://travis-ci.org/DJCordhose/jmte.svg?branch=master)](https://travis-ci.org/DJCordhose/jmte)

The Java project Minimal Template Engine is meant to fill the gap between simple string formatting with basic Java classes like String.format and complex template solutions like Velocity or StringTemplate.

It is complete but minimal in a sense that you can express everything you need in a template language including 'if' and 'foreach', but nothing else. Because of this it is small, easy to learn and clearly focused. It does not try to solve what Java can do better anyway.

It supports separation of model and view, runs without external dependencies, can be extended and configured in many ways and runs in almost all environments including Google App Engine.

**Caution**: JMTE is is in low maintenance mode: critical bugs will be fixed, but do not expect new features


Include using Maven
-------------------

https://repo1.maven.org/maven2/com/floreysoft/jmte/

```
   <dependency>
      <groupId>com.floreysoft</groupId>
      <artifactId>jmte</artifactId>
      <version>7.0.3</version>
      <scope>build</scope>
    </dependency>
```    

Documentation
-------------

This project has been moved from https://code.google.com/p/jmte/, and there is some documentation still in their wiki pages: https://code.google.com/archive/p/jmte/wikis. On github there just is basic documentation [here](https://cdn.rawgit.com/DJCordhose/jmte/master/doc/index.html). For more details see the tests that cover all variations of the scripting language and extensions to the engine: https://github.com/DJCordhose/jmte/tree/master/test/com/floreysoft/jmte

Where is JMTE used
------------------

- Document and JavaScript Templating in Ultradox: https://help.ultradox.com/en/guides/templates/overview.html
- Email Notifications in Graylog: http://docs.graylog.org/en/3.1/pages/streams/alerts.html#email-alert-notification



Releasing
---------
- https://andresalmiray.com/publishing-to-maven-central-using-apache-maven/
  * `mvn -B --file pom.xml release:prepare release:perform`
- JDK 17 has issues: https://issues.sonatype.org/browse/OSSRH-66257
  * `export MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"`
- GPG might have problems with your terminal: https://github.com/keybase/keybase-issues/issues/2798
