[![Update](https://github.com/codeteapot/packer-maven-plugin/workflows/Update/badge.svg)](https://github.com/codeteapot/packer-maven-plugin/actions?query=workflow%3AUpdate)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.codeteapot.maven.plugins/packer-maven-plugin?label=Maven%20Central)](https://repo1.maven.org/maven2/com/github/codeteapot/maven/plugins/packer-maven-plugin)

# Packer Maven Plugin

Maven plug-in to run [Packer](https://www.packer.io/) commands.

```xml
<plugin>
    <groupId>com.github.codeteapot.maven.plugins</groupId>
    <artifactId>packer-maven-plugin</artifactId>
    <version>0.6.10</version>
    <executions>
        <execution>
            <id>packer-build</id>
            <goals>
                <goal>build</goal>
            </goals>
            <phase>package</phase>
            <configuration>
                <inputDirectory>${project.build.directory}/packer/input</inputDirectory>
                <template>template.json</template>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Visit [project site](https://codeteapot.github.io/packer-maven-plugin/v0.6.10) to see full
documentation.
