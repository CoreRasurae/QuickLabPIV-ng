## If you like this project, or if it is useful to you in any way, please give this project a GitHub star, it costs nothing.

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

# Software based on research work
Mendes, L.P.N.; Ricardo, A.M.C.; Bernardino, A.J.M.; Ferreira, R.M.L. A Hybrid PIV/Optical Flow Method for Incompressible Turbulent Flows. Water 2024, 16, 1021. https://doi.org/10.3390/w16071021 

# Compilation
- Pre-requisites:
  Java development kit at least version 8, maven, QuickLabPIV-libs, Aparapi
  
  https://github.com/CoreRasurae/QuickLabPIV-libs/releases/tag/QuickLabPIV-libs_v0.1.0

  https://git.cleverthis.com/cleverthis/aparapi/aparapi

Package generation instructions:

Step 1 - Compile QuickLab-libs

```mvn package```

Step 2 - Install QuickLab-libs

```mvn install:install-file -Dfile=<PATH_TO_JAR>/QuickLabPIV-libs.jar -DlocalRepositoryPath=<BASE_DIR>/mavenLM -DgroupId=pt.quickLabPIV.libs -DartifactId=quickLabPIV-libs -Dversion=0.1.0-LM -Dpackaging=jar```

Step 3 - Adjust mavenLM local repository folder in pom.xml, by updating the url to the correct path
```
   <repository>
                <id>mavenLM</id>
                <url>file:///mavenLM</url>
   </repository>
```

Step 4 - Adjust version for Aparapi if required
```
    <!-- https://mvnrepository.com/artifact/com.aparapi/aparapi -->
		<dependency>
		    <groupId>com.aparapi</groupId>
		    <artifactId>aparapi</artifactId>
		    <version>3.0.2</version>
		</dependency>
```

Step 5 - Compile QuickLab

```mvn package```

optionally skip tests:

```mvn package -DskipTests=true```

# Example projects
- Liu-Shen combined with dense Lucas-Kanade hybrid PIV:
  ```examples/LiuShenWithLucasKanade_HybridPIV.xml```
