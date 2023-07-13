# QuickLabPIV-ng v0.8.3 - Initial release
A Particle Imaging Velocimetry (PIV) and Dense PIV Software Laboratory with GPGPU processing

- Pre-requisites:
  Java development kit higher than 8, maven, aparapi-3.0.3-LM.jar, QuickLabPIV-libs

- (Temporary workaround - before official release of new Aparapi version in maven central) -  
Package generation instructions:

Step 1 - Install aparapi-3.0.1-LM.jar in a local maven repository under current folder / mavenLM

```mvn install:install-file -Dfile=<PATH_TO_JAR>/aparapi-3.0.1-LM.jar -DlocalRepositoryPath=<BASE_DIR>/mavenLM -DgroupId=com.aparapi -DartifactId=aparapi -Dversion=3.0.1-LM -Dpackaging=jar```

Step 2 - Compile QuickLab-libs

```mvn package```

Step 3 - Install QuickLab-libs

```mvn install:install-file -Dfile=<PATH_TO_JAR>/QuickLabPIV-libs.jar -DlocalRepositoryPath=<BASE_DIR>/mavenLM -DgroupId=pt.quickLabPIV.libs -DartifactId=quickLabPIV-libs -Dversion=0.1.0-LM -Dpackaging=jar```

Step 4 - Adjust mavenLM local repository folder in pom.xml
```
   <repository>
                <id>mavenLM</id>
                    <url>file:///mavenLM</url>
   </repository>
```

Step 5 - Adjust mavenLM version for aparapi if required
```
    <!-- https://mvnrepository.com/artifact/com.aparapi/aparapi -->
		<dependency>
		    <groupId>com.aparapi</groupId>
		    <artifactId>aparapi</artifactId>
		    <version>3.0.1_SNAP</version>
		</dependency>
```

Step 6 - Compile QuickLab

```mvn package```

optionally skip tests:

```mvn package -DskipTests=true```

# Example projects
- Liu-Shen combined with dense Lucas-Kanade hybrid PIV:
  ```examples/LiuShenWithLucasKanade_HybridPIV.xml```
