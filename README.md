# Icy, a bioimage analysis software

<!-- badges: start -->
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Twitter](https://img.shields.io/twitter/follow/Icy_BioImaging?style=social)](https://twitter.com/Icy_BioImaging)
[![Image.sc forum](https://img.shields.io/badge/discourse-forum-brightgreen.svg?style=flat)](https://forum.image.sc/tag/icy)
<!-- badges: end -->

This is the **repository for the source code of the Icy kernel**, which was developed and is maintained by the [Biological Image Analysis unit at Institut Pasteur](https://research.pasteur.fr/en/team/bioimage-analysis/). Icy is free and open source (**GPL3 license**). It has been funded both by Institut Pasteur and [France Bioimaging consortium](https://france-bioimaging.org). You can find more info and download Icy on the [Icy website](https://icy.bioimageanalysis.org/). 

## Description

**Icy is a software to do image analysis**,and is mainly oriented toward the analysis of biological images. It was developed as a **toolbox containing state-of-the-art methods**, like the Gaussian filter, **and latest algorithmic developments of the [Biological Image Analysis unit at Institut Pasteur](https://research.pasteur.fr/en/team/bioimage-analysis/)**. It was also developed as a **platform to gather and favor the exchanges** between biologists, computer vision scientists, bioimage analysts and developers. It notably has a righ graphical interface to ease the integration of non programming users, as well as possibility for more advanced users to do scripting (in Javascript), graphical programming using the Protocols tool and development of Java plugins. Last but not least, Icy was developed to promote **quantitative analysis** of biological images and **reproducibility** of analyses. It includes many possibilities to save and reuse parameters and even save and reuse complete workflows.              
In terms of development environment, Icy is coded in **Java** and uses a **Maven project environment**. It is a modular software composed of a kernel and plugins. The kernel contains the main functions related to graphical user interface, image visualization (2D and 3D), regions of interest with statistics and basics image data manipulation tools such as [image cursors](https://icy.bioimageanalysis.org/developer/using-image-cursors-when-developing-plugins/). 


## Installation

As user of the application: [download Icy from the Icy website](https://icy.bioimageanalysis.org/download/) and follow the [installation instructions](https://icy.bioimageanalysis.org/tutorial/installation-instructions-for-icy-software/). You can reach the Icy development team to report a bug or ask for a new feature via the [image.sc forum](https://forum.image.sc/). For bug or feature request related questions, choose the "Usage and Issue" category. For more general questions, use the "Image Analysis" category. In any cases, don't forget the tag "icy".       

As developer willing to contribute to the kernel or to write a plugin for Icy, please get started by reading our blog posts on [how to set your development environment for Icy](https://icy.bioimageanalysis.org/developer/setting-icy-development-environment/), [how to create a new Icy plugin](https://icy.bioimageanalysis.org/developer/create-a-new-icy-plugin/) and [how to migrate your old Icy plugin to Maven](https://icy.bioimageanalysis.org/developer/migrate-your-old-icy-plugin-to-maven/). You can reach the Icy development team via the [image.sc forum](https://forum.image.sc/). For development related questions, choose the "Development" category and don't forget the tag "icy".     

Last but not least, we invite you to read our [Contributing guidelines](https://gitlab.pasteur.fr/bia/icy/-/blob/master/CONTRIBUTING.md) and our [Code of Conduct](https://gitlab.pasteur.fr/bia/icy/-/blob/master/CODE-OF-CONDUCT.md).    

## Documentation

Plugin documentation for users is hosted on the Icy website, in the [Resources category](https://icy.bioimageanalysis.org/plugins/). General documentation is available in the [Training category](https://icy.bioimageanalysis.org/trainings/). You can also have a look at the article on [how to get help on the Icy software](https://icy.bioimageanalysis.org/tutorial/how-to-get-help-on-icy/).         
For developers, there is also an [Icy Javadoc](https://icy.bioimageanalysis.org/javadoc/).        


## Citation information

de Chaumont, F. et al. (2012) Icy: an open bioimage informatics platform for extended reproducible research, [Nature Methods](https://www.nature.com/articles/nmeth.2075), 9, pp. 690-696             
https://icy.bioimageanalysis.org    

Please mention the version of Icy you used (bottom right corner of the GUI or first lines of the Output tab). Don't forget to also cite the plugins you use.           


## Acknowledgements to main kernel and main plugins contributors

We acknowledge every person who contributed one way or another to make Icy what is now, in particular:       
*Kernel architecture and code*: Stephane Dallongeville and Fabrice de Chaumont              
*Head of unit hosting the Icy project*: Jean-Christophe Olivo-Marin                  
*EzPlug and Protocols* (as many others plugins): Alexandre Dufour                
*Scripting*: Thomas Provoost, Timothee Lecomte and Stephane Dallongeville              
*MicroManager for Icy*: Thomas Provoost, Irsath Nguyen and Stephane Dallongeville                
*Beta testers and main contributors*: Nicolas Chenouard, Alexandre Dufour, Nicolas Herve, Vannary Meas-Yedid, Sorin Pop, Thibault Lagache, Jérôme Mutterer and the [Biomedical Imaging Group](http://bigwww.epfl.ch/)       
*Icy website graphic design*: Marcio de Moraes Marim              
*Icy website code*: Fabrice de Chaumont / Marcio de Moraes Marim 


The Icy website hosts a [**detailed list of all contributors**](https://icy.bioimageanalysis.org/contributors/) to the Icy software. Contributions include kernel development, plugin development, protocol creation, blog post writing...       



<!--  M. Geissbuehler and T. Lasser - https://opg.optica.org/oe/fulltext.cfm?uri=oe-21-8-9862&id=252779
"How to display data by color schemes compatible with red-green color perception deficiencies" Opt. Express 21, 9862-9874 (2013) -->


## Main libraries used in Icy               

- [Bio-Formats](https://www.openmicroscopy.org/bio-formats/)                        
- [VTK](https://vtk.org/)
- [Substance / Insubstancial](https://github.com/Insubstantial/insubstantial)      
- [EHCache](https://www.ehcache.org/)        


<!-- ## Development tool for profiling/debugging       

 YourKit is kindly supporting open source projects with its full-featured Java Profiler.             
YourKit, LLC is the creator of innovative and intelligent tools for profiling Java and .NET applications.     
Take a look at YourKit's leading software products: YourKit Java Profiler and YourKit .NET Profiler.   -->      


