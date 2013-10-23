# Rultor Supplementary Plugin

This plugin is intended to be used in projects, which are
built by [rultor.com](http://www.rultor.com),
hosted continuous integration environment.

Add it to your `pom.xml`:

```xml
<project>
  <build>
    <pluginManagement>
      <plugins>
        <plugin>
          <groupId>com.rultor</groupId>
          <artifactId>rultor-maven-plugin</artifactId>
          <version>0.3.1</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>
```

Then, in [rultor.com rule](http://doc.rultor.com/index.html#rule)
prepend your Maven build goals with `rultor:steps`:

```
$ mvn rultor:steps clean install
```
