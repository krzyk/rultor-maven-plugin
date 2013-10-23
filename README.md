# Maven Build Progress Reporter

Rultor.com is a unique continuous integration server that reads build
progress information from its standard output. [Xembly](http://www.xembly.org)
XML data manipulation language is used for reporting. Your build
should throw Xembly instructions into its standard output and Rultor
will pick them, parse, and use for summary statistics building.

`rultor-maven-plugin` automates this operation for Maven builds.
Add it to the list of plugins in your parent module:

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>com.rultor</groupId>
        <artifactId>rultor-maven-plugin</artifactId>
        <version>0.3</version>
        <executions>
          <execution>
            <goals>
              <goal>steps</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

By default the plugin reports successfull completion of every sub-project, if
the project is a multi-module one. And it reports every individual goal if
the project doesn't have any sub-modules.

By default the plugin is turned off. It is supposed to be turned on
only in continuous integration environment, using this command line
argument:

```
$ mvn -Drultor.skip=false install
```

More details: [maven-plugin.rultor.com](http://maven-plugin.rultor.com)
