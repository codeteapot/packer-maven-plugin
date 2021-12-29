[![Update](https://github.com/codeteapot/packer-maven-plugin/workflows/Update/badge.svg)](https://github.com/codeteapot/packer-maven-plugin/actions?query=workflow%3AUpdate)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.codeteapot.maven.plugins/packer-maven-plugin?label=Maven%20Central)](https://repo1.maven.org/maven2/com/github/codeteapot/maven/plugins/packer-maven-plugin/)

# Packer Maven Plugin

Maven plug-in to run [Packer](https://www.packer.io/) commands.

```xml
<plugin>
    <groupId>com.github.codeteapot.maven.plugins</groupId>
    <artifactId>packer-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>packer-build</id>
            <goals>
                <goal>build</goal>
            </goals>
            <phase>install</phase>
            <configuration>
                <inputDirectory>${project.build.directory}/packer/input</inputDirectory>
                <template>template.json</template>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Visit [project site](https://codeteapot.github.io/packer-maven-plugin/v1.0.0) to see full
documentation.
