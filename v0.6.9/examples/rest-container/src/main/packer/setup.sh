#!/bin/sh

mkdir $TOMCAT_LIB_DIR
mkdir $APPLICATION_LIB_DIR

cat << EOF >> /init
#!/bin/sh
java \
-classpath $APPLICATION_LIB_DIR \
-Dcom.github.codeteapot.maven.plugins.packer.example.tomcatLibDir=$TOMCAT_LIB_DIR \
-jar \
$APPLICATION_LIB_DIR/${project.artifactId}-${project.version}.jar
EOF

chmod +x /init
