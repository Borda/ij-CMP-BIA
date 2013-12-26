# How to use #

Brief introduction how to use this API you can find in README in `src/` folder

Eclipse for ImageJ
* http://imagejdocu.tudor.lu/doku.php?id=howto:plugins:the_imagej_eclipse_howto
* http://cmci.embl.de/documents/100825imagej_eclipse_debugging
* http://fiji.sc/wiki/index.php/Developing_Fiji_in_Eclipse
* http://fiji.sc/Maven
* ...

Profiling
* http://www.jvmmonitor.org/doc/

3party libraries
* [Commons Math](http://commons.apache.org/proper/commons-math)
* [Java Library for Machine Learning](http://sourceforge.net/projects/jlml)
* [MAchine Learning for LanguagE Toolkit](http://mallet.cs.umass.edu)

## Previous Compilation using Ant ##

This is a remain after previous project structure where we used a building tool Ant. 
This compilation information are related to the the building file build.xml.OLD (to be compiled this file has to be renamed back to build.xml)
See brief info about Ant - http://en.wikipedia.org/wiki/Apache_Ant ,  http://ant.apache.org/

1) ImageJ from http://rsbweb.nih.gov/ij/download.html and extract it into the same folder as this project is.
* chose related source to you Operation system (e.g Linux 64bit) or the Platform  independent
* actually we are developing for version 1.47o
* alternative option can be downloading the Fiji (http://fiji.sc/Fiji) BUT in this case the path to ij.jar bas to be changed in the ImageJ-CMP-BIA/build.xml
2) Compile and install building plugins - enter the ImageJ-CMP-BIA folder and call 'ant'
* basically the Apache Ant is a software tool for automating software build processes (http://en.wikipedia.org/wiki/Apache_Ant)
* note, the library ij.jar has to be located in the root of downloaded ImageJ
* for any path changes, open the build.xml and edit it
* note we are currently developing under Sun JDK 1.6
3) Launch executable in the ImageJ folder

NOTE: because the project structure have been little changed, there is no guarante that this compilation is runnable 

