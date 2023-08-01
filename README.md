# QuickLabPIV-ng
A Particle Imaging Velocimetry (PIV) and Dense PIV Software Laboratory with GPGPU processing

- PIV and Hybrid PIV high performance computing application with GP-GPU OpenCL support (by Aparapi)
- Friendly Graphical User Interface (GUI)
- Supports both dense and sparse Liu-Shen combined with Lucas-Kanade and Lucas-Kanade only Optical Flow methods
- Support classic PIV and PIV with warping modes
- MATLAB file format data export including multi-volume support
- Adaptive PIV support with configurable start and end Interrogation Area window sizes
- Multiple sub-pixel methods including Polynomial Gaussian 1D-1D and Hongwei Guo's Gaussian 1D-1D Robust Linear regression, among others
- Vector validation and substitution including secondary peak substitution
- Single XML configuration file
- Support images sequences and image pairs
- Selectable GPU per CPU threads distribution

# Compilation
- Pre-requisites:
  Java development kit higher than 8, maven, aparapi-3.0.1_LM2.jar, QuickLabPIV-libs

  https://github.com/CoreRasurae/aparapi/releases/tag/Aparapi_v3.0.1LM2
  
  https://github.com/CoreRasurae/QuickLabPIV-libs/releases/tag/QuickLabPIV-libs_v0.1.0

- (Temporary workaround - before official release of new Aparapi version in maven central) -  
Package generation instructions:

Step 1 - Install aparapi-3.0.1_LM2.jar in a local maven repository under current folder / mavenLM

```mvn install:install-file -Dfile=<PATH_TO_JAR>/aparapi-3.0.1_LM2.jar -DlocalRepositoryPath=<BASE_DIR>/mavenLM -DgroupId=com.aparapi -DartifactId=aparapi -Dversion=3.0.1-LM -Dpackaging=jar```

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
