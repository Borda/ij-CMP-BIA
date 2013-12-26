# Compilation using Maven #

It is the actual way how to compile this project, we are using building tool Maven.
See [brief info](http://en.wikipedia.org/wiki/Apache_Maven) about [Maven](http://maven.apache.org/).
There are basically three ways how to run then Maven - compile, test, install

* compile - using command "mvn compile" compiles the source code
* test - using command "mvn test" runs all internal tests and show the results
* install - using command "mvn install" combines the compile + tests and also install the plugin into the ImageJ/Fiji instance. Check the location of you ImageJ/Fiji in the pom.xml file (variable imagej.app.directory).

Note: Download the [ImageJ](http://imagej.net/) or [Fiji](http://fiji.sc/Fiji) as appropriate for your operation system (e.g Linux 64bit).
Note2: the installation into plugin folder is done only it does not already exist so manual removing from plugin folder is needed

## Description of the `pom.xml` file ##

some important/interesting facts:
* the "artifactId" has to contain an underscore to be installed among plugins, otherwise it is installed into the `jars/` folder
* "SNAPSHOT" in version marks that the actual version is "in process"
* the "imagej.app.directory" property specifies the location of ImageJ/Fiji instance for an installation
* "dependencies" specifies the list of used libraries and their versions
* "parent" defines the project parent - org.scijava - where some other stuff is declared, e.g. how to install the plugin, etc.

## Local Maven Repos ##

We follow [this article](https://devcenter.heroku.com/articles/local-maven-dependencies) to provide dependencies from a project-local repository that are not available from any official Maven repositories.
In particular, we provide a current version of the [Java Library for Machine Learning](https://sourceforge.net/projects/jlml/). It was "deployed" using this command-line:

```bash
mvn deploy:deploy-file -Durl=file:$(pwd)/repo/ -Dfile=JML-2.8-JRE-1.6.jar -DgroupId=net.sourceforge -DartifactId=jml -Dpackaging=jar -Dversion=2.8-SNAPSHOT
```

The same command-line needs to be used to update the locally-deployed version to a new one.
