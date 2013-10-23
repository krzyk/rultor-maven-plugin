# Rultor.com Supplementary Maven Plugin

Add it to your `pom.xml`:

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

Then, in the [Rultor rule](http://doc.rultor.com/index.html#rule)
start your Maven build with this command line argument:

```
$ mvn clean install -Drultor.skip=false
```
