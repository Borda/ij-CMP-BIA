# ImageJ plugins by CMP-BIA group #

These plugins have their home on the [Fiji Wiki](http://fiji.sc/CMP-BIA_tools).

## CMP-BIA tools ##

We extend the ImageJ implementation by our java codes. This project mainly 
contains implementation of our methods as  plugins in ImageJ (or Fiji) 
and some other useful API for image segmentation and registration.


## General project structure ##

* `src/main/java/` - all our source codes and some external single java classes.
** plugins - only the runnable IJ plugins are here.
** classification - clustering and classification functions.
** optiomisation - optimisation algorithms such as L-BFGS.
** registration - set of registration functions mainly the ASSAR.
** segmentation - contains segmentation structures with own visualisation and some simple supplementary methods; there are also superpixel clustering methods (e.g. SLIC) and other segmentation methods.
** tools - some useful tools for type conversion, number generators, matrix tools, logging, etc.
** transform - transformation function for images such as wavelets.
* `src/test/java/` - validation tests using JUnit templates for code testing per parts; the internal structure is similar to the `main/` folder; they can be also seen as samples how to use individual functions.
* `docs/` - folder for some related documents.
* `target/` - folder into which Maven will write the compiled artifacts.

## ImageJ plugins ##

* jSLIC superpixel segmentation (only 2D images)

## Reference ##

[1] Borovec, J., & Kybic, J. (2014). [jSLIC : superpixels in ImageJ](https://cmp.felk.cvut.cz/cvww2014/papers/32/32.pdf). In Z. Kunbelova & J. Heller (Eds.), Computer Vision Winter Workshop (pp. 14–18). Praha: Czech Society for Cybernetics and Informatics.
