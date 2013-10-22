# Rultor.com Supplementary Maven Plugin

Add it to your `pom.xml`:

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>com.rultor</groupId>
        <artifactId>rultor-maven-plugin</artifactId>
        <version>0.1</version>
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

That's it.
