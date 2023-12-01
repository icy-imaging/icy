Icy 2.4.4.0 - 01/12/2023
------------------------

**Application**
* VTK
 - updated to VTK 9.2.6
 - fixed picking and panel resizing when GUI zooming (OS) is used
 - allow transparent geometry to correctly intermix with volume (still a bit buggy)
 - improved library loading process (might be slower though)
* ROI
  - optimized boolean operations for shape type rois.
* added support for MacOS arm64 architecture (M1/M2)
* small fix for MacOS 'Quit' command
* load of small bugfixes
  
**Developper**
* ROI
  - added fast AreaX implementation (allow fast shape boolean operation)
  - Added ROI2DPath.addFast(), .intersectFast() and .exclusiveAddFast() methods
  - added getBounds() methods to ROIDescriptor (useful to 'bound' descriptor result)
* cleaned up / removed some old useless classes.


Icy 2.4.3.0 - 16/12/2022
------------------------

**Application**
* Saver
  - fixed FPS setting on AVI file saving
* Loader
  - fixed small bug on multiple files loading
  - fixed resolution level parameter which wasn't taken in account
* Canvas
  - added `+` / `-` shortcuts to zoom IN / OUT in 2D canvas view
* ROI
  - fixed ROI2DPoint / ROI3DPoint focused state
* fixed some issues with old internal HTTP address
* fixed invalid warning report on load
* fixed several issues with MacOS app integration
* updated Bio-Formats to version 6.11.0
* updated ImageJ to version 1.53r
* updated MacOS app launcher
* switched to Markdown for CHANGELOG file
  
**Developper**
* added new PluginScriptFactory interface for plugin providing ScriptEngineFactory
* cleaned up / removed some old useless classes.
  

Icy 2.4.2.0 - 09/05/2022
------------------------

**Application**
* Loader
  - fixed unresponsive UI when loading large group of file
  - improved performance on file group loading
  - faster preview refresh
  - fixed others smalls issues with file loading in general

**Developer**
* fixed a small bug where embedded sub class plugin couldn't be found in PluginLoader plugin list
* AbstractImageProvider: added InterruptedException + small fix on process interruption (we were removing interruption state)
* SequencePrefetcher: fixed incorrect InterruptedException handling
* SequenceFileGroupImporter: added 'ordering' parameter (when directly opening a list of file)


Icy 2.4.1.0  - 28/04/2022
------------------------

**Application**
* fixed a regression on signed byte image creation
* fixed possible memory leak on colormodel listener release
* updated Bio-Formats to 6.9.0 + added custom patch to fix ND2 file loading
* general better performance when dealing with many ROIs
* Loader
  - fixed a bug making a new loaded image irresponsive in some situation
  - better colormap restoration on group loading
  - improved image paths parsing for image grouping operation 
* ROI
  - added shift modifier to link new created ROI to current T and Z position
  - added Shift+T and Shift+Z shortcuts to attach ROI to current T and Z pos
- others smalls bug fixes and tweaks.

**Developer**
* added SequenceUtil.setDefaultColormaps(..) method


Icy 2.4.0.0  - 02/02/2022
------------------------

**Application**
* Viewer
  - added current image name in viewer title
* ImageCache
  - image cache can now be initialized and shutdown at runtime (better performance and better memory usage)
* Updater
  - OSX: removed automatic native library signature
* System
  - fixed and improved automatic bug report plugin detection
  - re-introduced library path patching for Java version supporting it (still marked as deprecated but better to keep it when supported)  
  - added general 'interruptible' background process actions
  - improved interrupted process handling
  - small fixes for headless mode
  - fixed exe launcher to accept Java 12 or above
- several bug fixes and tweaks.

**Developer**
* Image
  - implemented proper ColorSpace sharing on IcyColorModel (not anymore using illegal reflection)
* ImageCache
  - reworked image cache so it does not anymore require usage of illegal reflection
  - better handling of volatile image on loading process
  - removed Cache.isEnabled() method (higher level now)
  - added Cache.clean() and Cache.isEmpty() methods
  - improved Cache.end() implementation
* ROI
  - fixed a small bug with ROIUtil.computeWatershedSeparation(..)
* System / advanced
  - added ReflectionUtil.setFieldValue(..) methods
  - added ClassPatcher.getField(..) and ClassPatcher.setFieldPublic(..) methods
  - removed IcySecurityManager (deprecated with Java 14 or >)
  - improved 'InterruptedException' throw / propagation and handling


Icy 2.3.0.0  - 05/11/2021
------------------------

**Application**
* 3D / VTK
  - added new volume slicer tool
  - tweaked default opacity 
* Loader
  - fixed loading non image files from Open command with 'All files' filter selected
* GUI
  - fixed a minor viewer toolbar refresh bug when switching rendering mode
  - fixed histogram log/linear display state restoration
  - improved histogram info display to remain visible even when moving bounds
* ROI
  - small optimization on 'Erosion' / 'Watershed' process time
* switch dataset to 'virtual' state asynchronously (as it can takes age to do so)
* fixed ImageJ / Icy image conversion switch
* improved Java 17 compatibility (replacing illegal reflective access where possible)
  Still need some work to allow the image cache engine to work with Java 17  
* small change to 'Updater' tool

**Developer**
* 3D / VTK
  - added new VtkArrow/Cylinder/PlaneObject classes for easy drawing of these primitives.
* deprecated all illegal reflective access methods
* bit of refactoring / cleanup in project structure (moved all package.html file to 'javadoc' folder)


Icy 2.2.1.0  - 09/09/2021
------------------------

Fixed possible crash when interacting with ROIs with 3D visualization (VTK).


Icy 2.2.0.0  - 02/08/2021
------------------------

**Application**
* Loader
  - fixed images grouping (could group image of different type or dimension)
* ROI
  - added new 3D ROIs Rectangle3D, Cylinder3D and FlatPolygon3D which are Z extended version of ROI2DRectangle, ROI2DEllipse and ROI2DPolygon
* VTK
  - upgraded VTK / 3D visualization library to version 8.0.1
  - upgraded JOGAMP / JOGL library to version 2.4.0-rc (fix OSX crash with VTK)
* general bug fixes
* fixed high resolution support on OSX
* fixed possible death locks on starting (happen only with certain OpenJDK and on Linux)
* allow illegal-access with Java 16 or above
* major rewrite of README file
* added CODE-OF-CONDUCT and CONTRIBUTING
* fixed links to LICENSE, CHANGELOG and README + tweaked About to redirect to the README.

**Developer**
* ROI
  - added new 3D ROIs ROI3DRectangle, ROI3DCylinder and ROI3DFlatPolygon ROIs classes
  - ROI3DStackEllipse, ROI3DStackRectangle and ROI3DStackPolygon are now deprecated
  - added ROI2DShape getMinZ()/getMaxZ() methods for easier display evolution
  - added base ROI3DZShape / ROI3DBoxShape abstract classes for easier 3D ROI implementation
  - small fix to Polygon2D / Polyline2D and added getPoints() / setPoints(..) methods
  - avoid throwing change event when not changed (bug touching ROIPolygon2D and ROIPolyline2D)
  - replaced ROIUtil.convertToStack(roi) by ROIUtil.convertTo3D(roi) and updated ROIUtil.convertTo2D(..)
  - fixed persistence (disabled Overlay persistence as ROI handle it by itself)
* VTK
  - lot of changes / fixes to make it work with new VTK 8.0 and also fix issues on recent OSX
* Overlay
  - added base ZShape3D and BoxShape3D abstract classes.
  - added new ZBoxAnchor3D, LineAnchor3D and RectAnchor3D classes to handle interaction with 3D objects / ROIs
* cleanup


Icy 2.1.4.0  - 15/04/2021
------------------------

**Application**
* Loader:
  - fixed 'Drag and Drop' loading of multi series or folder dataset.
  - ignore advanced metadata parsing for image format not supporting it (faster loading)
* Saver: 
  - fixed overwriting an opened image (image was locked by the Image cache so it couldn't be properly saved)
* ROI:
  - added 3D ROI support to 'Separate component' ROI separation tool.
  - added new Magic Wand tool and associated preferences.
  - fixed global key action (unselect, delete and read-only mode).
* VTK / 3D visualization:
  - added support for view synchronization. 
* Preferences:
  - added a section to set advanced settings for the new Magic Wand tool.
  - reset cache / result path if they are not anymore valid.
* general bug fixes

**Developer**
* Cursor:
  - fixed VolumetricCursor getter
* BooleanMask3D:
  - added getComponent() method to retrieve connected component from 3D boolean mask
* LUT:
  - added LUTViewer.getActiveChannelIndex() method to know which channel tab has focus
* Loader:
  - added hasMetadata(path) method to know if dataset designed by given path supports metadata
* cleanup
 
 
Icy 2.1.3.0  - 17/03/2021
------------------------

**Application**
* ROI:
  - added global selection and alphabetical sorting function on descriptors selector
  - changed 'positive infinity' to 'ALL' value representation for size Z/T/C ROI descriptors (ROI table)
* Loader: 
  - small fix in 'base path' determination during stiched loading from a set of image files
* MetaData:
  - better metadata preservation in general
  - added 'User Name' information
  - added 'Date' (dataset creation / acquisition date)
* updated Forum help URL (Image.sc)
* minors bug fixes

**Developer**
* Loader:
  - added new openWith(path, flags, readerClass) method to select which reader to use (Bio-Formats importer)
  - fixed a small issue preventing stitched loading using loadSequences(..)
* MetaData:
  - added T,Z,C plane position fix in MetaDataUtil.keepPlanes(..) method
  - added plane metadata preservation in buildMetaData() from group importer
  - added MetaDataUtil.get/setUserName(s) methods to get/set experimenter(s) user name
  - added new OMEUtil.createOMEXMLMetadata(meta, setUserName) method to define if we set the user name in the meta data
* Sequence:
  - added 'user name' property (represent who created / generated the dataset)
* Added new PETA / TERA prefixes to UnitPrefix class
 
 
 Icy 2.1.2.0  - 09/02/2021
-------------------------

**Application**
* fixed Windows launcher to not require JAVA_HOME variable to be defined.
* Sequence:
  - fixed sequence filename when loading a dataset from a folder (regression)
* GUI:
  - fixed support of custom ROI
  - fixed a bug in ROI descriptor computation to avoid error on process interruption
* ROI:
  - small optimization for .contains(..) and .intersects(..) methods
* Sequence:
  - fixed timestamp metadata calculation (could generate problem when copy/pasting ROI)
* Updated Javassist library to version 3.27 to fix issues with recent Java.

**Developer**
* ROI:
  - fixed id preservation on copyFrom(ROI) method
* Sequence:
  - added getProperty(..) / setProperty(..) methods (as in ROI class)


Icy 2.1.0.0  - 31/10/2020
------------------------

**Application**
* Java 8 is now the minimum required version of Java.
* GUI:
  - better headless support in general
* Metadata:
  - better 'time interval' metadata retrieval
* Sequence:
  - fixed fast crop operation (wasn't correctly considering ROI channel information) 
* Cache:
  - added option do disable cache (through command line)
* ROI:
  - ROI2DArea now show its bounding box only when hovered or selected (better for visualization). 
  - added ROI dilatation / erosion operations
  - added ROI distance map operation
  - added ROI watershed separation operation
  - added ROI unstack operation (convert 3D ROI to multiple 2D ROIS, one for each slice) 
  - added new 'Intersected' and 'Contained' ROI descriptors (number of intersected and contained ROI)
  - added  'Internal id' ROI descriptor to retrieve internal ROI id (can be useful) 
  - fixed ROI table focusing on double click
  - better handling of ROI descriptors process interruption (when switching or closing an image for ex)
  - fixed BooleanMask for tiny ROI2DShape (single pixel) 
  - fixed area multi selection for ROI with channel position different from ALL
* Viewer:
  - we can now loop in a timelaps using left/right keys.
  - fixed pixel size of sequence rendering views (snapshots) 
* Network:
  - added option to disable network (through command line)
  - ignore possible error on audit process
* Updater:
  - fixed weird lockup on process execution error 
* better recovery of parent / main plugin for 'Bundled Plugin'
* many others changes, tweaks or fixes

**Developer**
* project migrated to maven
* repository migrated to GitLab (Pasteur)
* cleanup up unused dependencies / libraries (they are now distributed as plugin)
* IcyBufferedImage:
  - added IcyBufferedImageCursor class for easier / faster image data manipulation
  - minor tweak on cache data management 
* Sequence:
  - added VolumetricImageCursor class for easier / faster 3D image data manipulation
  - added SequenceCursor class for easier / faster sequence data manipulation
* Plugin:
  - added static methods to extract native library files
  - added getMainPlugin() method to retrieve Main Plugin descriptor (Bundled Plugin) 


Icy 2.0.3.0  - 31/10/2019
------------------------

**Application**
* Canvas:
  - faster and smaller memory footprint for screenshot for simple 2D image
* VTK:
  - fixed colormap alpha restoration for 3D VTK canvas. 
* Loader / Bio-Formats:
  - fixed tile image loading.
  - fixed duplicated data when opening several sequences at once.
  - minor tweak to avoid using cache when not needed (better performance).
  - minor change on data pre-fetch strategy.
* replaced micro character by 'u' to avoid incorrect display on non UTF-16 softwares
* updated online bug report to make it compliant with new web report.
* small fix in ROI table sorter.

**Developer**
- fixed MANIFEST.MF file containing an invalid character which was making Eclipse to crash.
- small tweak in XMLUtil.loadDocument(..) methods.


Icy 2.0.2.0  - 30/08/2019
------------------------

**Application**
* Loader:
  - empty pre-fetch tasks on Sequence close.
* VTK:
  - correct colormap alpha channel loading for IcyCanvas3D
* Search:
  - general improvements on search engine (support +/- operator on keyword).
  - switched online search to the new web interface

**Developer**
* fixed ROIUtil.convertToSequence(..) method to correctly generate labeled image.


Icy 2.0.1.0  - 12/08/2019
------------------------

**Application**
* Loader:
  - fixed empty (black) image when opening multi-series Sequence (shared importer was closed)
  - limited number of importer (reduce memory usage when the group contains many files)
* ImageCache:
  - removed temporal images from the cache to not waste memory
  - tweaked EHCache data expiration settings
  - minor fix in GUI refresh
* Sequence:
  - added simple data prefetch for faster initial data loading
  - added getFilename(t, z, c) to get file path for image at given position when Sequence was loaded from multiple files
* added global layer enable/disable action (shift + L)
* fixed native library patching for Java 12 or above
* VTK:
  - better error reporting
  - minor tweak on VTK native library loading


Icy 2.0.0.0  - 12/04/2019
------------------------

**Application**
* new image cache engine.
* new packaging for easier installation on OSX system.
* GUI:
  - added new Icy 'virtual mode' button to force the image cache engine to be used for all opened / created images.
  - memory monitor now display image cache load level.
  - Icy shows 'virtual mode' in title when image cache engine is forced ON.
  - improved 'General' and 'Canvas' preferences presentation.
  - added image cache settings in 'General preferences' section.
  - added informations on image cache at startup.
  - fixed position of Series selection dialog
* Loader:
  - added lazy data loading for faster Sequence loading operation.
  - improved preview display refresh.
* ROI:
  - added 'ROI to Square' conversion.
  - added 'ROI to Circle' conversion.
  - small changes / improvements on ROI tasks GUI
* Viewer:
  - timelaps are now in 'repeat' playback mode by default
  - added 'SPACE' key shortcut to play/pause timelaps playback 
* better background processing of some basic sequence operations ('Duplicate', 'Convert RGB', 'Convert Gray', 'Crop'...)
* minor speed improvement on application start
* added new splash screens
* many other small changes, tweaks, fixes and refactoring

**Developer**
* IcyBufferedImage:
  - many changes to add image cache support.
  - added lazy data loading support (for faster Sequence loading operation)
  - added isVolatile() / setVolatile(..) method to enable/disable image caching.
  - added isDataInitialized() / isDataLoaded().. methods.
  - many others changes.
* Sequence:
  - added isVolatile() / setVolatile(..) method to enable/disable image caching.
  - added setDataXY(..) method (need to use it for image caching)
  - changed the way channel bounds are updated (as data can be loaded on demand).
* SequenceUtil:
  - fixed getSubSequence(...) method to work with volatile images.
  - extractSlice(..) and extractFrame(..) now correctly duplicate original images data (should have been the case).
  - minor fix on colormap update in utilities methods.
* Loader:
  - added 'forceVolatile' parameter on some methods to force image caching.
  - fixed possible Importer leak on error. 
* Bio-Formats importer:
  - fixed possible bugs during downscaled image/data loading
* ROIUtil:
  - added convertToPoint(..), convertToEllipse(..) and convertToRectangle(..) methods
* DataIterator:
  - updated to be compatible with volatile images.
* VtkUtil:
  - minor change in getBinaryImageData(vtkPolyData, ...) method
* added FileUtil.getDrive(path) method
* added IcyFrame.getBorder() method 
* fixed last ROIs objects retention when removing ROIS from an image while keeping the image open.
* added EHCache library.


Icy 1.9.10.0  - 19/11/2018
-------------------------

**Application**
* ROI:
  - fixed 3D VTK display for ROI2DPoint and ROI3DPoint
  - tweaks for faster retrieve of default property value
* Saver:
  - FPS is automatically determined from metadata (AVI format)
  - added time interval field for easier FPS calculation (AVI format)
* Plugin: minor fix about plugin installation notification
* Network: audit methods cannot lock anymore Icy launch process.
* Minor fix on possible exception with the Clipboard.
* Other minors changes or fixes.

**Developer**
* IcyBufferedImage:
  - fixed possible out of bounds operation with getDataInterpolated(..)
  - minors changes to prepare volatile raster
* Sequence:
  - fixed possible out of bounds operation with getDataInterpolated(..)
  - use metadata to retrieve dimension information when we can't get them from data (just for X, Y and C dimension).
* Loader: prepared async loading method (not yet complete)
* VtkUtil: renamed getArray(..) to getJavaArray(..)


Icy 1.9.9.1  - 28/09/2018
------------------------

**Application**
* fixed java version checking for HTTPS compatibility


Icy 1.9.9.0  - 27/09/2018
------------------------

**Application**
* Loader:
  - added series grouping when 'grouping' in enabled in Loader dialog
  - added groupSeries(..) method to group similar series in an image
* Sequence: fixed series index reset after saving an image
* ROI:
  - fixed 3D VTK display (correctly handling specific Z and T position of the ROI)
  - fixed ROI Mass Center computation for single point ROI
* VtkCanvas:
  - fixed layers refresh on position change
  - fixed/better implementation of layers visibility
* Network:
  - added warning message for HTTPS compatibility with the future web site
  - fixed HTTPS support for future web site (when java version ok)
  - replaced automatic redirection by manual (and safer) redirection
  - better error handling in general

**Developer**
* Sequence: added getDataInterpolated(t, z, c, y, x) method
* IcyBufferedImage: added getDataInterpolate(x,y,c) method
* Loader: added groupSeries(..) method to group similar series in an image
* Line2DIterator / Line3DIterator: added forceLastPoint parameter.
* VtkUtil:
  - added getJavaArray(vtkDataArray) method
  - added getPolyDataFromDataSet(vtkDataSet) method
  - added getBooleanMaskFromBinaryImage(vtkImageData..) method


Icy 1.9.8.1  - 01/08/2018
------------------------

**Application**
* Loader:
  - better colormap preservation on grouped image opening.
  - fixed tile image loading on grouped image opening.
* Removed IRC chat (not really used and to avoid bot spam)
* Fixed minor issue with preferences saving (asking for restart everytime)


Icy 1.9.8.0  - 20/07/2018
------------------------

**Application**
* Loader:
  - fixed time position (timestamp) in metadata when loading sub part of sequence
  - fixed a small bug in setting panel (couldn't change 'loading type' in some situation)
  - fixed minor bug with possible invalid metadata when stitching was used.
  - stitching disabled when 'loading type' is set to 'separate' (better implementation) 
* Minor fix to update plugin processing (incorrect message report) 

**Developer**
* Sequence:
  - better metadata initialization
  - added getPosition() method for fast XYZ position retrieving
  - added getPositionT() / getTimeStamp() methods
  - added getPositionTOffset(..) method
  - added setPositionT() / setTimeStamp(..) methods
  - added setPositionTOffset(..) method
* MetaDataUtil:
  - added getPositionT(..) / getTimeStamp(..) methods
  - changed getTimePosition(..) by getPositionTOffset(..) methods
  - added setPositionT(..) / setTimeStamp(..) methods
  - added setPositionTOffset(..) methods
* SequenceUtil:
  - added convertPoint(..) methods for easy coordinate conversion between 2 Sequence objects
  - added convertRectangle(..) methods for easy region conversion between 2 Sequence objects
* ROIUtil:
  - rewrote adjustToSequence(..) method (better implementation).
* VtkUtil:
  - added getROIFromBinaryImage(..) method to convert VTK binary image to ROI
  - renamed method polyDataToImageDate(..) to getBinaryImageData(..)
  - added getTransform(..) method
  - renamed method objToMesh(..) to getSurfaceFromOBJ(..)


Icy 1.9.7.0  - 29/05/2018
------------------------

**Application**
* Several fixes and improvements on multi file loading
* Fixed updater to keep it compatible with Java 6


Icy 1.9.6.2  - 23/05/2018
------------------------

**Application**
* Fixed launcher settings to be compatible with Java 10


Icy 1.9.6.1  - 23/05/2018
------------------------

**Application**
* Fixed / improved image preview refresh in Loader dialog.
* Minor change in frame / component visibility on opening.
* Other minor fixes.


Icy 1.9.6.0  - 18/05/2018
------------------------

**Application**
* Many changes and improvements with multi-file image loading:
  - better meta data handling
  - preview supported
  - automatic Stitching
  - compatible with Importer interface
* We can now see plugin JAR file location and online help directly from the 'Plugin details' panel.
* Fixed issues with Java 10 on OSX (ImageJ error) and Windows (Launcher error).
* Output log can now be saved in file
* Minor change on memory information display
* Some other minor fixed and changes 
* Documentation fixes

**Developer**
* Loader:
  - reworked getSequenceFileImporters(..) methods to be more generic
  - moved file grouping in separate class.
  - added new 'load(...)' methods for partial load and display of Sequence from a group of file.
  - added new 'loadSequence(..)' method for partial Sequence load from a group of file.
  - added 'internalLoadGroup(..)' methods for partial Sequence load from a file group
  - Sequence now keep a reference on used Importer for loading, the Imported is closed when Sequence is unloaded from memory.
* LociImporterPlugin:
  - added automatic Stitching
  - added Metadata level selection using 'flags' in open(..) method
  - reverted 'groupFiles' property back to true by default
  - implemented getPixelsByTile(..) method with region loading support
  - added region loading support to getImageByTile(..) method
  - added sub resolution support to getImage(IFormatReader, ...) method
  - added sub resolution support to getPixelsInternal(IFormatReader, ...) methods
  - added sub resolution support to getImageInternal(IFormatReader, ...) methods 
* AbstractImageProvider:
  - replaced getImageByTile(..) by getPixelsByTile(..) and the method now support region loading.
* SequenceFileGroupImporter:
  - added closeInternalsImporters() method
  - minor fix on getImage(..) (sizeX and sizeY could be wrong)
  - minor fix on getThumbnail(..)
* BooleanMask2D:
  - minor fix in getContourPointsxxx(..) methods
  - changed the way we detect incompatible  dimension on Union and Intersection operation
* ROI2DShape:
  - minor fix on computeBounds2D()
  - minor fix on getBooleanMask(..)
* VtkUtil:
  - added getPolyDataFromPoints(..) method
  - added polyDataToImageData(..) method
  - added transformPolyData(..) method
  - added objToMesh(..) method
  - added objToImageData(..) method
* Array1DUtil:
  - added copyRect(...) method for easier 'rectangular' copy
* ClassUtil:
  - added getJarPath(Class) method
* Sequence:
  - tweaked getOverlays(Class) method
  - added getSelectedROISet(Class) method
  - added getSelectedROIs(Class roiClass, sorted, wantReadOnly) method
  - added imageProvider field (this is the importer, will be used later)
* LUT/IcyColorMap:
  - minor fix on copyFrom(..) method
  - minor fix on setAlphaToOpaque(..) method
* ImageUtil: added getTileList(Rectangle region, ...) method
* added GeomUtil class (utilities for 'Geom' classes)
* SeriesSelectionDialog: now use SequenceIdImporter instead of SequenceFileImporter importer.
* SystemUtil:
  - getJavaVersionAsNumber() now returns JAVA version as major numbers (6.0xx, 7.0xx, 8.0xx, 9.0xx, ...)
* added caching preferences (not yet used)
* OMEUtil:
  - added getNonNegativeInteger(int) method
* PluginDescriptor:
  - better information recovering for bundled plugin.
  - added getPluginJarPath() method
* prepared new Web interface


Icy 1.9.5.1  - 27/11/2017
------------------------

**Application**
* fixed ImageJ issue on last OSX preventing Icy from starting.
* Search: 'Actionable' bundled plugins are not visible in search results.

**Developer**
* added added Sequence.getROIs(Class roiClass, boolean sorted) method
* minor tweak/fix in MetaDataUtil.keepSingleSerie(..)


Icy 1.9.5.0  - 03/11/2017
------------------------

**Application**
* Fixed some java 9 issues.
* ROI: copy/paste operation now takes care of sequence sub resolution / crop.
* fixed minor issue with Substance library with Java 9.
* better sequence naming on partial loading (sub region).
* fixed small bug in task submission resulting in better responsiveness of some task (as the search result).
* others minors changes and tweaks.

**Developer**
* ROIUtil:
  - fixed scale(..) operation on ROI2DRectShape type.
  - added adjustToSequence(..) method to adjust a ROI depending the Sequence resolution and position.
* BooleanMask2D: 2D downscale() method now use 2 pixels for true by default.
* BooleanMask3D: 3D downscale() method now use 4 pixels for true by default.
* ImageProvider: added isResolutionAvailable(..) method
* AbstractImageProvider: added basic isResolutionAvailable(..) implementation
* Canvas2D:
  - re-introduced old CanvasView.getImage() for backward compatibility
  - CanvasView.getImage() renamed to CanvasView.getImageAsTiles()
* Search API: some changes to prepare for new Web search interface
* added WebInterface class to prepare future Web interface communication
* StringUtil: added split() method


Icy 1.9.4.1  - 30/08/2017
------------------------

**Application**
* Histogram: added export to XLS (or CSV)
* Loader: fixed small issue in Sequence naming.
* Bio-Formats importer: better preservation of original filename
* others minors changes and tweaks.

**Developer**
* Histogram: added getCSVFormattedData() method
* XLSUtil: added setFromCSV(..) method
* SaveDialog: added chooseFileForResult(..) methods


Icy 1.9.4.0  - 11/08/2017
------------------------

**Application**
- minors fixes and tweaks.

**Developer**
* ROI2DArea / BooleanMask2D:
  - added upscale() method for 2x up scaling.
  - added downscale() method for 2x down scaling.
* ROI3DArea / BooleanMask3D: 
  - added upscale() method for 2x up scaling.
  - added upscale2D() method for 2x up scaling (2D only).
  - added downscale() method for 2x down scaling.
  - added downscale2D() method for 2x down scaling (2D only).
* ROI:
  - getProperty(..) / setProperty(..) property name is not anymore case sensitive.
* ROIUtil:
  - added scale(ROI, ...) method (only work for ROI2DShape and ROI3DShape)
  - added getUpscaled(..) method for 2x up scaled ROI.
  - added getDownscaled(..) method for 2x down scaled ROI. 
* Loader: fixed a minor bug on region image loading.
* Saver: fixed "origin" informations on saving operation
* Sequence: added resetOriginInformation() method


Icy 1.9.3.2  - 12/07/2017
------------------------

**Application**
* GUI: removed 'show all' button from search result panel (useless).
* Loader:
  - fixed multi series opening (available only with defaults load options)
  - fixed an issue with tile loading in Bio-Formats importer
  - better report of incorrect java version for importer
* Canvas2D: fixed issue on refreshing empty sequence (regression)

**Developer**
* ROI: added getProperty(..) and setProperty(..) method to get/set arbitrary properties (String format).
* ROI3D/4D/5DStack: safer for multi-threaded operations.
* Plugin: added prepareLibrary(..) method to prepare native library loading
* Better color map preservation for multi channel image creation.


Icy 1.9.3.0  - 30/06/2017
------------------------

* modified image rendering in Canvas2D to improve update and rendering speed with large image. 
* fixed error detection on download for 0 byte files.
* new informative message for plugin requiring a new java version.
* fixed 'multiple files' image saving which could failed in some case.
* minor change to memory panel.
* others minors fixes.


Icy 1.9.2.1  - 19/06/2017
------------------------

* Bio-formats importer:
  - fixed sub resolution image loading when region is specified
* ROI2DPoint / ROI3DPoint:
  - fixed getBoundxx() so it returns a non empty bounds (which is problematic for some calculation)
  - fixed getBooleanMask(..) so it does return a mask when inclusive is true
* fixed regression on SequenceUtil.concatC(..) method.
* others minors changes or fixes.


Icy 1.9.2.0  - 06/06/2017
------------------------

**Application**
* GUI: reworked Sequence and ROI menu tasks.
* Loader:
  - we can now select image series directly from Loader dialog
  - some bug fixes in preview refresh
  - fixed Bio-Formats sub resolution loading
  - fixed some bugs in Bio-Formats importer
  - preserve series information when using 'open sequence region'
* fixed colormap opacity (regression)
* better colormap preservation of some Sequence operations.
* now support HTTPS connection for plugin repository.
* OutputPanel: we can now set a maximum line limit
* several bug fixes

**Developer**
* fixed IcyBufferedImage.toBufferedImage(..) method (regression)
* switched to OMEXMLMetadata class from 'ome' package when possible (refactoring)
* MetadataUtil: lot of refactoring (due to OMEXMLMetadata class change mainly)
* OMEUtil: some refactoring (due to OMEXMLMetadata class change mainly)
* added ImageProvider.getOMEXMLMetadata() method to use the good OMEXMLMetadata class ('ome' package)
* VTKUtil: added getImageData(..) method
* MainInterface: added getROIRibbonTask() method
* IcyCanvas: protected listener operations (add/remove/fireEvent)
* minor fix on ROI2DLine.setLine(..) method
* ROIActions: added fillInterior and fillExterior actions
* Sequence: some refactoring
* minor change in function ClassUtil.getFile(String className)
* minor fix in FileUtil.getFileExtension(..) method


Icy 1.9.1.0  - 10/04/2017
------------------------

**Application**
* ROI2DArea / ROI3DArea : fixed ROI double click shortcut.
* ROI2DPoint / ROI3DPoint: fixed auto select on point focus.
* Canvas2D: fixed image cache update and auto resizing on image size change.
* Loader: minor improvement to image preview display
* Updater: better local path handling.

**Developer**
* Anchor2D: added COLOR, SELECTEDCOLOR and SELECTED property change event.
* added FileUtil.APPLICATION_DIRECTORY constant
* Metadata:
  - fixed some retro compatibility issues with OMEXMLMetadata object.
  - refactoring (switch from loci to ome OMEXMLMetadata structure when possible)
* fixed 'micro' character representation


Icy 1.9.0.1  - 03/04/2017
------------------------

**Application**
* added new 3D ROI:
  - 3D point type ROI.
  - 3D line type ROI.
  - 3D polyline type ROI.
  - Ellipse 3D stack type ROI (available from stacking operation).
  - Polygon 3D stack type ROI (available from stacking operation).
* ROI:
  - more interaction with 3D VTK display (can move and modify the ROI)
  - better and safer VTK rendering in general
  - added 'to 3D stack' operation to convert 2D ROI to 3D Stack ROI.
  - added 'to Mask' operation to convert shape ROI to Area ROI.
  - added 'to Shape' operation to convert area ROI to Polygon ROI.
  - added 'Separate' operation to separate unconnected object from an Area ROI.
  - added 'ROI Cutter' operation to manually cut/separate ROI.
* ROI Panel:
  - can now modify position / size of multiple ROI at same time.
  - we can now quickly move view to a specific ROI location by double clicking on it (work in both way: from ROI panel or from the image). 
  - cleaned GUI:
    - removed some ROI operations we usually do from keyboard (copy/paste, delete)
    - moved some ROI operations to ROI band (load/save, boolean operator).
    - XLS export button moved at the top of the ROI table.
* Image Loader (loader dialog):
  - added Z and T range selection
  - added channel selection
  - added resolution level selection
  - added 2D region (XY) selection
  - improved preview
  - better filename auto ordering
* Image Saver (saver dialog):
  - removed Z and T range selection (better to do adjust range before)
  - use big tiff when dataset is above 2GB
  - setting are now preserved in XML preferences.
  - image conversion is now automatically done when you use a save format which isn't compatible with the current image format.
   A warning is displayed to inform the user about possible data loss.
* GUI:
  - added a new tooltip to explain where is the main menu.
  - added java 6 warning for OSX users.
  - improved memory usage report in memory monitor.
  - reorganized 'File & ROI tools' menu:
    - added 'open region...' command to load a specific XY region of an image.
    - re-organized the ROI creation actions.
    - added many ROI operation actions (see the above 'ROI' and 'ROI Panel' sections).
* Canvas2D:
  - reversed panning direction from keypad keys.
  - image cache now use volatile accelerated image (image redraw should be much faster on some systems) 
* VtkCanvas: improved overlay handling (should be a bit faster and safer)
* better metadata preservation on Sequence operation.   
* sequence merging operations is now possible with Sequence of different type.  
* ToolTipFrame: we can now clink on URL to open external browser.
* Updater: fixed local directory saving (it was saving in working folder instead of application folder)
* fixed wait for plugin update in headless mode.
* many bug fixes.
* several tweaks and optimizations.
* some code cleanup.
 
**Developer**
* ROI:
  - added 'group' field (will be use in future for 'group' ROI operation)
  - added getLength(Sequence) method to compute length/perimeter with Sequence pixel size information.
  - added getEffectiveDimension(..) dimension method.
  - added unselectAllPoints() method.
  - moved some methods to ROIUtil.
* ROI3D:
  - added ROI3DPainter class for easier display and interaction for 3D ROI.
  - added isOverEdge(..) method.
  - added add(..), exclusiveAdd(..), intersects(..) and subtract(..) methods.
  - added ROI3DShape: abstract class for 3D Shape ROI.
  - added ROI3DPoint: 3D point type ROI.
  - added ROI3DLine: 3D line type ROI.
  - added ROI3DPolyline: 3D polyline type ROI.
  - added ROI3DStackShape: abstract class for 3D Stack Shape ROI.
  - added ROI3DStackEllipse: Ellipse 3D stack type ROI (available from stacking operation).
  - added ROI3DStackPolygon: Polygon 3D stack type ROI (available from stacking operation).
* ROI3DStack:
  - fixed setName(..) methods so children get modified as well.
  - deprecated getUseChildColor()/setUseChildColor(..) methods.
* ROI3DArea:
  - added ROI3DArea(BooleanMask2D, zMin, zMax) constructor.
  - added isOverEdge(..) method.
* ROI5DArea: fixed a bug in optimizeBounds()
* ROI Overlay: drawROI(..) and drawName(..) are now public.
* ROIUtil:
  - moved methods from ROI: getUnion(..), getIntersection(..), ...
  - added convertToStack(..) method to convert 2D ROI to 3D Stack ROI.
  - added convertToMask(..) method to convert Shape ROI to Area ROI.
  - added convertToShape(..) method to convert Area ROI to Polygon ROI.
  - added getConnectedComponents(..) to separate and retrieve connected components from an input ROI.
  - added copyROIProperties(..) to copy properties from an input ROI.
  - added split(ROI, Line2D) to cut/split a ROI from a given Line2D.
  - added convertToSequence(..) method to convert a list of ROI to a labeled sequence.
* ROIDescriptor: added getDescriptor(descriptors, descriptorId) method   
* ROIPerimeterDescriptor: modified to use the new ROI.getLength(..) method.
* BooleanMask2D:
  - added getConnectedContourPoints(..) method to retrieve contour in 'connected' order.
  - minor change to make it safer.
* added new 'geom' classes: Polygon2D, Polyline2D, Shape3D (base class for 3D shape), Line3D, Polyline3D
* Rectangle3D: added intersectsLine(..) method.
* Point3D: 
  - added many methods to compute distance, total distance, angle, normalize, length, dot product, cross product...
  - added translate(..) method
  - better hashCode() implementation.
* Anchor2D:
  - added integer Z position information (for 3D position of 2D ROI)
  - added VTK display and interaction support (allow ROI2D manipulation with VTK).
* PathAnchor2D: fixed visibility for closing point.
* added Rectangle2DUtil, Line2DUtil and Point2DUtil classes (utilities for the java classes)
* Sequence:
  - added position X, Y, Z information (metadata)
  - added 'origin' resolution, XY region, Z range, T range, channel information (when loading sub part of an image)
  - added getOutputBaseName() / getOutputExtension() and getOutputFilename() to obtain wanted output filename given 'origin' informations
  - trying to load too large array of data throw a TooLargeArrayException exception
* SequencePersistent: added isValidXMLPersitence(..) methods
* SequenceUtil:
  - added getOriginPoint(..) / convertPoint(..) for point position conversion operation
  - added getOriginRectangle(..) / convertRectangle(..) for rectangle position conversion operation
* IcyBufferedImage:
  - trying to load too large array of data throw a TooLargeArrayException exception
  - added copyData(array, ..) method to copy data from an array
* IcyBufferedImageUtil:
  - added fast downscaleBy2(..) methods to quickly downscale image by a factor of 2 
  - changed toBufferedImage(..) method so it returns accelerated volatile image when possible.
* new 'undoable' operations:
  - ROIReplaceSequenceEdit (replaced single ROI in a sequence)
  - ROIReplacesSequenceEdit (replaced multiple ROI in a sequence)
  - PositionROIEdit (modified single ROI position)
  - PositionROIsEdit (modified multiple ROI position)
  - BoundsROIEdit (modified single ROI bounds)
  - BoundsROIsEdit (modified multiple ROI bounds)
  - PropertyROIEdit (modified single ROI property)
  - PropertyROIsEdit (modified multiple ROI property)
  - Point3DAddedROIEdit (added a new point in a 3D ROI)
  - Point3DMovedROIEdit (modified a point in a 3D ROI)
  - Point3DRemovedROIEdit (removed a point in a 3D ROI)
* DefaultSequenceEdit: default undo operation on Sequence also preserve ROI and Overlays now (reference only)
* IcyCanvas:
  - added canvasToImageLogDeltaZ(..) method, this is the Z equivalent to canvasToImageLogDeltaY(..) / canvasToImageLogDeltaX(..) methods.
  - added centerOn(region) method for centering view on a specified image location. 
* IcyCanvas2D: improved setMousePos(..) method to be safer.
* IcyCanvas3D:
  - added methods to handle mouse (image) position.
  - added imageToCanvas(..) and canvasToImage(..) methods (with default implementation to avoid breaking compatibility)
* Canvas2D:
  - added getFitToCanvas() / setFirToCanvas() methods
  - getARGBImage(..) now returns volatile accelerated image for faster image drawing operation (may consume more memory to prepare the image)
* VtkCanvas:
  - added getWorldZ(Point) method to retrieve Z depth for a given 2D canvas position.
  - added worldToDisplay(Point3D) method to convert a 3D world point to canvas 3D position.
  - added displayToWorld(Point) method to convert a 2D canvas position to a world 3D point.
  - implemented imageToCanvas(..) and canvasToImage(..) from previous added methods.
  - implemented getScaleX/Y() methods.
  - implemented setMouseImagePosX/Y/Z(..) methods.
  - mouse event now provide a correct mouse position.
  - removed the enable picking on Mouse Move switch.
* VtkUtil: simplified getSurfaceFromImage(..) method.
* IcyVtkPanel: added proper event consuming.
* Loader:
  - added checkOpening(..) method to check we have enough resource to open an image
  - added methods to load only sub part (Z, T and C) and sub resolution of the image.
  - fixed issues while using Importer on non image files.
  - many changes in general, new methods, refactoring...
* Bio-Format importer:
  - added multi thread support.
  - added tile loading for large image and sub resolution loading.
  - better thumbnail loading.
* AbstractImageProvider / PluginSequenceFileImporter / PluginSequenceIdImporter:
  - improved default getThumbnail(..) implementation
  - added getImageByTile(..) method to load an image by tile (useful for large image)
* Saver:
  - added many new methods to check compatibility image writing compatibility.
  - reverted channel combination for 3 channels image saving in TIFF format (better for RGB image preview)
  - methods doing Z/T range saving operation are now deprecated (better to do it before)
  - added method getCompatibleSequenceForWriter(..) to automatically convert a Sequence so it can be save by a given writer (possible data loss)
  - added SaverOptionPanel for easier setting definition
* SequenceDataIterator: fixed typo making some ROI calculations wrong in some situation.
* MetaDataUtil: replaced OMEXMLMetadataImpl class by OMEXMLMetadata in some methods (more generic, check if that bring issue)
* FileUtil: added getFiles(..) method to easily retrieve file with a specific extension without using a FileFilter.
* XMLUtil: added filtering to avoid incorrect character in name/attribute fields.
* SystemUtil:
  - added getJavaVersionAsNumber() method
  - getJavaFreeMemory() return the total amount of free memory available to the JVM 
  - getJavaMaxMemory() return the maximum amount of memory the JVM will attempt to use 
  - getJavaAllocatedMemory() return the memory currently *allocated* by the JVM
  - getJavaUsedMemory() return an estimation of the memory currently *used* by the JVM
  - getJavaTotalMemory() is deprecated (replaced by getJavaAllocatedMemory())
* ImageComponent: now use a cache for faster image drawing
* NumberTextField: added setInteger(..) method to force integer value instead of double value. 
* ImageJUtil: added convertToIcyBufferedImage(..) method to convert a single image from an ImagePlus to an IcyBufferedImage
* IcyExceptionHandler: added anti spam protection for report(..) method.
* IcyCommandButton: added setPopup(..) method to enable popup action.
* RecentList: thread safe accesses, fixed possible NPE.
 

Icy 1.8.6.0  - 26/07/2016
------------------------

**Application**
* fixed an issue when we tried to reload an image which has been saved after a Z slice remove operation.
* removed POI library from Icy-Kernel package (fix the infamous workbook error in protocol).
* other internals tweaks or changes.


Icy 1.8.5.1  - 22/07/2016
------------------------

**Application**
* fixed a minor bug in ROI table values refresh.
* fixed a bug in Plugin Importer retrieval.


Icy 1.8.5.0  - 20/07/2016
------------------------

**Application**
* GUI: fixed infinite histogram refresh in very rare case (keep high CPU usage).
* General: fixed Plugin changelog display on plugin update (regression).
* Loader: minor fix on colormap retrieve for Bio-Formats importer.

**Developer**
* added ColorUtil.getColorFromWavelength(double) method.
* ClassPatcher: add new methods.
* JarClassLoader: fixed issue with externally loaded classes.


Icy 1.8.4.0  - 11/07/2016
------------------------

**Application**
* GUI:
  - better handling of Ctrl+Tab shortcut to switch between opened image.
  - double clicking on desktop background now collapse inspector instead of detaching it.
  - modified desktop background image drawing.
* General:
  - added an option to ask for saving new generated sequence before closing them.
* Metadata:
  - removed issue which forced having a time interval > 0 on metadata edition.
  - fixed possible NPE with metadata access.
* Loader: better colormap recovery from Bio-Formats
* PluginInstaller: added detection of corrupted JAR / XML file during installation (should get rid of these infamous false error reports)
* ImageJ: fixed wrapper so plugin playing with ImageJ Toolbar (as NeuronJ) work as expected.

**Developer**
* ExternalizablePanel use its internal frame only only when required.
* added ConfirmDialog.confirmEx(..) method to distinguish 'Cancel' operation.
* ROI2D: removed perimeter calculation cache as it is dependent from Sequence metadata as well.
* ROI3D: removed surface area calculation cache as it is dependent from Sequence metadata as well.
* ZipUtil: added isValid(..) method


Icy 1.8.3.2  - 17/06/2016
------------------------

**Application**
* fixed severals issues with metadata during image load/store operations.


Icy 1.8.3.0  - 13/06/2016
------------------------

**Application**
* fixed small issue on single image saving operation (script / protocol).

**Developer**
* Plugin: fixed issue with native library loading on OSX with java 8.


Icy 1.8.2.0  - 08/06/2016
------------------------

**Application**
* fixed memory leak when removing image from Sequence.
* VTK: fixed NPE when drawing a ROI on an image which have a 3D duplicated view.
* always separate channel when saving in OME TIFF format (bug fixed in Bio-Formats library).
* better metadata preservation on image saving operation.

**Developer**
* ROIXDArea: fixed a small issue in setBoooleanMask(..) method
* MetaDataUtil: fixed minors issues and added many methods to MetadataUtil.
* some internal refactoring.


Icy 1.8.1.0  - 12/05/2016
------------------------

**Application**
* ROI Panel:
  - relaxed mutex for better performance.
  - refresh ROI descriptors when plugin list changed.
* ROI: fixed minors issues on contains(ROI) and intersects(ROI) methods.
* Viewer: display a message when changing canvas.
* fixed SequenceImporter and Importer plugin launch.
* plugin instance are registered on instance creation and not only on 'run' operation.
* minors changes on Plugin update process.
* others small fixes, tweaks and changes.

**Developer**
* fixed a small issue with GuiUtil.createIcyFrameFromWindow(..) method.
* minors changes to LookAndFeelUtil.
* MetaDataUtil: fixed minors issues.


Icy 1.8.0.0  - 26/04/2016
------------------------

**Application**
* VTK:
  - upgraded to VTK 6.3
  - switched to the new VTK OpenGL2 rendering backend
    --> simplified mapper (GPU or CPU)
    --> multi channel support for GPU raycasting
    --> GPU raycasting should work on OSX as long the graphics card driver are up to date.
  - switched to OpenGL components
    --> OSX 10.6.8 and older *do not support anymore* VTK, do not upgrade to this version if you need to use VTK on these OS !
    --> newer OSX versions can now use Java 7 or Java 8 without any troubles for VTK.
    --> no more display refresh / overlapping on OSX.
* VtkCanvas:
  - some changes related to the new VTK implementation.
  - better handling of automatic rendering level.
  - support object/ROI picking.
  - improved translation operation.
  - fixed global layers visibility for outline objects. 
* Canvas2D: added background color support.
* ROI2DPoint: better visualization in VTK canvas.
* ROI2DShape: added selection box in VTK canvas.
* ROI2DArea: added visualization in VTK canvas.
* ROI3DArea: added visualization in VTK canvas.
* Viewer: better handling of error during canvas type change.
* Added support for inspector collapsing.
* Added support for Proxy authentication.
* ROI panel:
  - added ROI color field.
  - fixed ROI panel filtering / sorting.
  - fixed refresh issue on property change for multiple ROI (as opacity change).
  - fixed 'no more field update' bug after a not handled exception happened during descriptor calculation.
  - split ROI Descriptor calculation on separate thread (3 threads) so basic information (as name, position...) are always immediately computed
   and displayed even if a complex descriptor takes a very long time to compute.
  - fixed multi selection when column sorting was enabled.
* Loader:
  - better handling of error during Importer creation
  - fixed XML cleaning for non image file (allow to open XML file from other importers)
* Double click on a Plugin in the Preferences plugin list open the plugin detail panel.
* Fixed issue with saving image file containing ':' character in their file path.
* Fixed Icy to ImageJ image conversion (was failing with double data type)
* Added warning tooltip when using 32 bits java on 64 bits window system.
* Fixed DownloadFrame so it can display download speed again.
* Re-introduced plugin download progress so we can cancel it.
* Better handling of windows positioning on different OS.
* Improved application launch speed (still very dependent of the number of plugin installed).
* Several memory usage optimization.
* Many minors improvements, tweaks and cleanup.

**Developer**
* VtkCanvas:
  - added pickProp(x,y) method.
  - added getPickedObject() to retrieve picked object on last mouse press/move event.
  - added 'PickOnMouseMove' property to allow object picking on mouse move event.
  - moved volume shading switch to the setting panel.
  - getMouseImagePos() method always returns null (not supported) now.
  - alpha LUT factor is now modified in this class and not anymore in IcyCanvas3D.
* VtkImageVolume: simplified volume mapper definition (GPU or CPU)
* added VtkBinaryVolume class (derived from VtkImageVolume).
* VtkUtil:
  - added getBinaryImageData(..) method to retrieve a binary vtkImageData object.
  - added getSurfaceFromImage(..) method to transform a vtkImageData object to vtkPolyData (iso surface).
  - added getBinaryColorMap() and getBinaryOpacityMap() methods.
  - added setPolyDataColor(..), getOutline(..) and setOutlineBounds(..) methods.
  - added getLayerProps(..) / added getLayersProps(..) methods.
  - added getProps(..), hasProp(..) / addProps(..) methods.
  - faster hasProp(..) method.
  - safer canvas locking on method using it. 
* ROI:
  - moved basic updateFocus(), updateSelect() and updateDrag() handling in ROI class (was in ROI2D)
  - moved basic mouseMove() and mousePressed() handling in ROI class (was in ROI2D)
  - optimized contains(ROI) and getIntersection(ROI) methods. 
  - added isEmpty() method.
  - added property info to ROI_CHANGED event (distinguish position or content change).
* ROI2D: added computePerimeter(Sequence) and getPerimeter(Sequence) methods to provide accurate perimeter information.
* ROI3D: added computeSurfaceArea(Sequence) and getSurfaceArea(Sequence) methods to provide accurate surface area information.
* ROI2DShape:
  - added support for focus/selected state with VTK.
  - added getTotalDistance(..) method with factors parameters to allow accurate calculation of perimeter.
* ROI2DArea:
  - optimized intersect(ROI) method.
  - fixed possible issue on XML persistence.
  - more robust for multi thread environment.
* ROI3DArea: added support for focus/selected state with VTK.
* ROI3DStack: fixed minors issues.
* ROI Descriptor:
  - perimeter & area calculation only available 2D ROI.
  - surface area & volume calculation only available 3D ROI.
* BooleanMask2D:
  - tweaks and fixes on boolean operation.
  - reworked getContourLength() for more accurate calculation.
* BooleanMask3D:
  - fixed and reworked getContourLength() for more accurate calculation.
* Overlay: added XML persistence support.
* Sequence:
  - overlays can now be saved in XML persistence if the Overlay 'persistence' property is set (false by default).
  - fixed setColormap(..) method so persistence is not lost in some case.
  - contained IcyBufferedImage share a common IcyColorSpace object to reduce memory usage.
* IcyBufferedImage: by default IcyBufferedImage are now created with 'autoUpdateChannelBounds' set to false.
  This avoid useless computation, flag is automatically updated when the image is added to a Sequence object.
* modified SequenceUtil/IcybufferedImage type conversion methods.  
* Performance optimization:
  - reworked the UpdateEventHandler to dramatically increase performance for large number of pending updates.
  - replaced EventHierarchicalChecker with CollapsibleEvent  --> ChangeListener interface modified (check for possible impact)
  - replaced EventListenerList by List<Listener> for faster events dispatching in critical sections (ROI, Overlay...)
  - optimized JAR resource loading for PluginClassLoader
  - optimized XML load operation
* ImageUtil: added loadAsync(..) methods.
* SytemUtil:
  - added isWindows64() method.
  - added getScreenDevices() method.
* XMLUtil: optimized memory usage of zip/unzip operation.
* ZipUtil: added new pack(..)/unpack(..) methods.
* Polygon2D: fixed possible NPE.
* PluginDescriptor: added lazy load of changelog, icon and image (reduce memory usage).
* PluginRepositoryLoader: simplified loading (see PluginDescriptor changes)
* Array1DUtil:
  - fixed ArrayToSafeArray methods when using same input and output type.
  - fixed getInterleavedData(..) and deInterleave(..) methods.
* FileUtil: added cleanPath(String) method to remove some invalid character in file path before save operation.
* MetaDataUtil: added getTimeIntervalFromTimePositions(..) method
* DateUtil: added getTimeAsString(double valueInMs) method.
* Colormap: better detection of linear colormap.
* ComponentUtil: added fixPosition(Component) to fix position of a component entirely offscreen (useful for Window).
* UnitUtil:
  - fixed a bug in displayTimeAsStringWithUnits(..) method.
  - fixed UnitUtil.displayTimeAsStringWithComma(..) method.
* SystemUtil:
  - added getScreenDevices(..) and getScreenDevice(..) to retrieve screen device for a given position / region.
  - added getScreenBounds(..) to retrieve the area of a specific screen device.
* some refactoring.


Icy 1.7.3.0  - 12/11/2015
------------------------

**Application**
  - just some bug fixes here and there.
 

Icy 1.7.2.0  - 04/11/2015
------------------------

**Application**
  - fixed NPE on Metadata read preventing some image load operation (regression).
 

Icy 1.7.1.0  - 03/11/2015
------------------------

* ImageDataIterator:
  - fixed a bug (regression).
  - optimized a bit the implementation.
 

Icy 1.7.0.0  - 28/10/2015
------------------------

**Application**
* ROI panel:
  - full implementation of the new ROIDescriptor feature.
  - extensible descriptor list by plugin.
  - now display the number of selected / total ROI in the table.
  - better display configuration.
  - separate columns selection for view and export.
  - added default drawing properties.
  - improved table computation time.
  - better value rounding policies.
  - fixed 'NOT' boolean operation on 2D ROI with fixed Z, T or C position.
  - faster ROI merge operation (up to 10x faster).
* fixed a bug in the Sequence dimension adjustment operation.
* Undo:
  - fixed a bug with the undo operation of ROI remove point.
  - some changes to the history panel.
* Histogram panel:
  - bounds are now correctly restored from persistence.
  - minors changes and fixes to the bounds range setting dialog. 
* some fixes with 3D rendering (VtkCanvas)
* conversion from IJ to Icy do not anymore display RoiManager.
* better handling of error when changing the rendering mode (Canvas type change).
* new splash screens (which are now dynamically fetched from res/image/splash location).
* 'time interval' metadata is automatically computed when possible.
* custom colormap (in 'colormap' folder) are now visible in the colormap selector.
* double click on the 'Sequence filename' will open the folder where the file is actually located.
* we can now remove the warning message when starting a second instance of Icy.
* fixed a minor issue in search result with bold character.
* improved Z/T dimension swap operation performance.
* some improvements in support of plugin execution from command line.
* many others changes and tweaks.

**Developer**
* Sequence:
  - added addROIs(..) and removeROIs(..) methods.
  - do not cumulate ROI and Overlay events anymore (an event is sent for each add, modification or remove object operation).
  - fixed getPixelSizeScaling(..) and calculateSize(..) methods.
* IcyBufferendImage: fixed get/setRawData(..) methods
* SequenceDataIterator: fixed a (very) nasty copy-paste bug that prevents all calculations in 3D.
* ImageDataIterator: optimizations.
* added Position2DIterator and Position5DIterator interfaces.
* added BooleanMask2DIterator class to iterate over all contained points in a boolean mask.
* added ROIIterator class to iterate over all contained points in a ROI.
* added ROIDescriptor abstract class to define new ROI descriptor (any calculation done from a ROI)
* added PluginROIDescriptor interface for easy implementation of new ROIDescriptor from Plugin.
* BooleanMaskXD:
  - added getNumberOfPoints() method.
  - added getContourLength() method (return perimeter / surface area)
* BooleanMask2D:
  - added methods for merge operation with another BooleanMask.
   These methods allow faster merge operation of many BooleanMask by reducing the number of BooleanMask creation.
* ROI:
  - added getSubROI(..) method to retrieve a sub part of a ROI.
  - added methods for merge operation with another ROI.
   These methods allow faster merge operation of many ROI by reducing the number of ROI creation.
  - fixed default computeNumberOfPoints() implementation.
  - better error detection on ROI copy operation.
  - added methods to retrieve and set the defaults settings of drawing properties.
* ROI2DArea:
  - added add/exclusiveAdd/remove(ROI2DArea) methods
  - added add/exclusiveAdd/remove(BooleanMask2D) methods
  - safer manipulation in multi threaded context
  - fixed a minor refresh issue.
  - some tweaks here and there.
* ROI2DShape: faster getBooleanMask2D(..) implementation (help a lot for descriptor calculation).
* ROI2DPath:
  - fixed contains(..) and intersects(..) implementation.
  - better merge(ROI) implementation.
* ROI2DPolyLine: use the new Polyline2D shape for a better and more accurate implementation.
* ROI2DPolygon: use the new Polygon2D shape for a better and more accurate implementation.
* ROIUtil:
  - merge(List<ROI>, Op) method should be faster now in most situation now.
  - many deprecated methods because of the new ROIDescriptor structure.
* added Polygon2D and Polyline2D Shape objects.
* ShapeUtil:
  - added isClosed(Shape), getOpenPath(Shape), getClosedPath(Shape) methods.
  - merge(List<Shape>, Op) should work better and faster.
  - union, exclusiveUnion, intersection and subtraction operation should work better.
* Overlay: add getAttachedCanvas() method to retrieve all canvas where the overlay is displayed.
* IcyCanvas.create(..) now throws exception.
* IcyCanvas2D: set the alpha colormap value to full opaque by default.
* IcyCanvas3D: set the alpha colormap value to linear by default.
* Canvas2D: fixed minor issue with filtering parameter.
* LUT: added setAlphaToLinear() and setAlphaToOpaque() methods.
* IcyColormap: added methods to retrieve default and custom colormap
* MainInterface:
  - tweaked the events generation.
  - 'ROI/Overlay add' events happen now *after* 'Sequence opened' event.
* Loader:
  - fixed Importer retrieval (it was launching the plugin instead of just creating it).
  - getXXXImporters(..) methods automatically report errors.
* PluginLoader: fixed getPlugins(Class<?> ..) method which wasn't retrieving BlundedPlugin.
* added AbstractRoisPanel class to allow an easier incorporation of the ROI table in external component / frame.
* MetaDataUtil: added getPositionX/Y/Z() and getTimePosition() methods.
* SystemUtil: added openFolder(..) method.
* Array1DUtil: added getInterleavedData(..) and deInterleave(..) methods.
* ByteArrayConvert: major rewrite (fixed several bugs).
* FileUtil: added isDirectory(String) method.
* PluginLauncher: put create(..) method in public as we may need it.
* ColorUtil: added mix(Color, Color, ratio) method.
* ZipUtil: reworked a bit
* XMLUtil: better handling of random exception using xerces library.
* GuiUtil: added createIcyFrameFromWindow(..) method to convert external window to IcyFrame.
* minor fix in the way we handle Layer removal operation.
* many others fixes, tweaks and cleanup.


Icy 1.6.1.1  - 20/02/2015
------------------------

**Application**
* optimized default opacity for 3D rendering.
* fixed a possible NPE on colormap update. 
 
 
 Icy 1.6.1.0  - 18/02/2015
------------------------

**Application**
* ROI:
  - can set position and size of selected ROI with CTRL + arrow keys.
  - fixed the SHIFT modifier for Line and Rectangular ROIs.
  - fixed minor issue with contains(x, y, z, t, c) method.
  - 3D rendering (in VtkCanvas) improved.
  - fixed some issues with channel specific information display in ROI panel.
  - better approximation of the number of points with the generic computeNumberOfPoints() implementation.
* Canvas2D:
  - minors changes on shortcuts action.
* VtkCanvas:
  - added support to layer visibility and opacity.
  - now support arrow keys to navigate through frame and keypad keys to translate view. 
  - default background color is now black
  - display information message when no enough memory to display image
  - fixed VTK 3D rendering with more than 4 channels.
* ImageJ:
  - added support for drop file operation.
  - better ROI conversion between Icy and ImageJ (uses RoiManager if needed).
* fixed windows repositioning on multiple virtual desktop system.
* added custom colormap support (colormaps inside the 'colormap' folder are now visible in the colormap combobox).
* added range movement and range settings to Histogram panel.
* added information on maximum memory with 64bit JVM in Preferences.
* better assignment of default image colormaps.
* some fixes and minors changes.

**Developer**
* Layer: replaced 'alpha' by 'opacity'
* fixed a bug with SequenceDataIterator object (should fix some issues with some ROIUtil methods).
* added IcyCanvas.isVisible(Layer) method.
* modified all the ArrayUtil.arrayToSafeArray(..) methods for better type conversion.
* fixed a minor issue with the IcyBufferedImageUtil.convertToType() method.
* removed the Plugin constructor using PluginDescriptor parameter (can cause issue)
* several improvements to IcyVtkPanel class.
* added ComponentUtil.centerOn(..) methods
* minor changes to the SequenceIdImporter interface (now implements Closeable)
* refactoring, cleanup.


Icy 1.6.0.1  - 26/11/2014
------------------------

**Application**
* fixed a bug with the auto update process.
* fixed a minor issue in metadata parsing.


Icy 1.6.0.0  - 20/11/2014
------------------------

**Application**
* Undo operation support:
  - customizable length history
  - support undo and redo depending the operation
  - undo / redo supported for almost all ROI operation
  - undo supported for almost all sequence operation
  - global undo / redo operation (when supported) by pressing Ctrl+Z / Ctrl+Y
* Added plugin usage statistics report (beta).
* Canvas2D:
  - added area selection (Ctrl key + mouse drag)
  - added message when there is not enough memory to display the image
* Canvas2D and VtkCanvas:
  - reset view with 'r' shortcut
  - reset view and palette with 'R' shortcut
* ROI:
  - fixed minor issue on subtraction operation.
  - fixed NOT boolean operation for 3D, 4D and 5D ROI.
* ROIPanel : fixed sorting on position and size. 
* Loader:
  - better image auto ordering from filename when loading a group of file
  - better progress information when loading a group of file
  - improved image preview display (support multiple importer)
* Loci importer: better retrieving of channel color from metadata
* VTK library loading fixed for Unix system (still some issues with VTK 6.1 support on unix system) 
* Colormap are preserved on Sequence extract channel / frame / slice operation.
* Histogram:
  - 'auto bounds' parameter is now preserved.
  - fixed minor refresh issue.
* Minor fix with internet monitor.
* Many bug fixes and tweaks.

**Developer**
* New undo framework:
  - added many classes to provide 'Undoable' operation (IcyUndoManager, IcyUndoableEdit...)
  - added MainInterface.getUndoManager() method to retrieve the active Sequence UndoManager.
  - added MainInterface.undo() and MainInterface.redo() methods for fast undo and redo on the active Sequence.
  - look at ROI undoable operation example to learn how to use the framework.
* FileUtil.getApplicationDirectory() tweaked for better support on OSX
* PluginDescriptor:
  - added isBlundled() method to know if this is a bundled plugin (several plugins inside the same package)
  - added isKernelPlugin() to know if this plugin is bundled in the application.
* PluginLauncher: some changes with plugin launch operation.
* Plugin: added getName(), isBundled() and getOwnerClassName() methods.
* ROI:
  - added copyFrom(ROI) method to obtain a ROI copy.
  - added getPropertyValue(..) / setPropertyValue(..) for generic property get and set operations.
* MetaDataUtil: added getSizeX/Y/Z/T/C() and setSizeX/Y/Z/T/C() methods
* Sequence:
  - added copyFrom(..), copyDataFrom(..) and copyMetadataFrom(..) methods.
  - added createUndoPoint(..), createUndoDataPoint(..), createUndoMetadataPoint(..) and AddUndoableEdit(..) methods to create complete, partial or custom restore point for undo operation.
  - added undo() and redo() and clearUndoManager() methods
  - added getSelectedROIs(Class roiClass..) to retrieve selected ROI of a specific type only.
  - added getDefautColormap(..) and setDefautColormap(..) methods.
  - added getColormap(..) and setColormap(..) methods (user LUT).
* Added SequenceUtil.getCopy(Sequence, copyROI, copyOverlay, suffix) to get a copy of the specified sequence with specific options.
* Added the ugly PluginNoEDTConstructor interface to avoid plugin creation in the swing graphics thread (needed for retro compatibility).
  Be careful as some Plugin abstract classes now use this interface.
* IcyBufferedImageUtil.convertToType(..) now uses safe type conversion as we always prefer it (even if it's bit slower).
* Added ImageUtil.isGray(image) method to know if the specified RGB image only contains gray pixels.
* Added MetaDataUtil.getChannelColor(..) method.
* ROI2DShape: added addNewPoint(..) and removePoint(..) method. 
* RectanglexD: fixed some typos in contains() and intersects() methods
* ROIxDStack: fixed rounding error in contains(..) and intersects() methods
* Added 3D, 4D and 5D rectangle ROI (available through code only)
* Added new methods in LUT and LUTChannel to copy colormap and scaler objects.
* Added MainInterface.getActiveLUT() for fast retrieving of current viewer's LUT.
* Added new methods to DateUtil.


Icy 1.5.4.2  - 04/09/2014
------------------------

**Application**
* added an option to display the ROI name (per ROI).
* fixed continuous histogram refresh on empty sequence (regression).
* some improvements on headless mode.
* check for update only once per day.
* fixed network detection when proxy is used.
* fixed faulty plugins verification.
* other minors fixes.


Icy 1.5.4.0  - 29/08/2014
------------------------

**Application**
* Layers panel:
  - opacity setting has moved at bottom.
  - support custom setting panel per layer (Overlay.getOptionsPanel())
* SearchBar: faster result display / refresh of search bar
* ROI:
  - added ROI contour size field (only for drawing).
  - fixed ROI2DEllipse area calculation.
  - fixed vertical and horizontal ROI2DLine selection.
  - modified ROI2DPoint rendering.
  - Rewrote ROI2D and ROI3D perimeter calculation with a more precise approximation.
* Fixed shared LUT on viewer duplication (regression).
* Icy let you select appropriate importer when multiple choice are available.
* Fixed histogram refresh.
* Loader dialog show image preview even for "all files" filter.
* Minors changes on VTK library on Unix system..
* Fixed crash on exit on Unix system (temporary workaround for JVM bug).
* Better internet connection monitor (lighter on network traffic).
* Tweaks / fixes on GUI refresh.
* Faster background repaint (cached), more responsive when moving windows.
* Others minors improvements, bug fixes and cleanup.


**Developer**
* Loader: many changes to provide better support on FileImporter and SequenceFileImporter classes.
* Overlay: support custom setting panel with getOptionsPanel()
* FileImporter.acceptFile(..) method now use path parameter.
* SequenceUtil:
  - C/Z/T merge operation now preserve metadata.
  - added getSubSequence(Sequence, ROI, double) method.
* Sequence:
  - added hasOverlay(Class) method.
  - added getROI/2D/3Ds(boolean sorted) methods.
* User email information stored in preferences.
* Fixed ROIUtil.getMassCenter() method.
* Added GeneralPreferences.get/setLoaderFolder(..) to retrieve the current "Open" folder.
* Do not clear anymore sequence objects (ROI and overlay) when sequence is closed.
* Fixed XLSUtil.loadWorkbook(..) which was clearing file.
* Fixed InstanceProcessors.submit(..) method.
* Minor change in the way we close opened windows on application exit.
* Some refactoring:
  - ImageLoaderDialog  --> LoaderDialog
  - ImageSaverDialog  --> SaverDialog
  - LoadDialog  --> OpenDialog
* Some cleanup, refactoring and javadoc updates.


Icy 1.5.3.1  - 09/07/2014
------------------------

Fixed Rectangle3D/4D/5D.contains(...) method (can be annoying for developer).


Icy 1.5.3.0  - 08/07/2014
------------------------

**Application**
* Last used colormap is now saved in the sequence persistent data.
* Added multi selection support in Plugin browser.
* Some improvements on the colormap combo box representation.
* Partial rewrite of the Plugin class loader sub system, this hopefully fix the random load class errors !
* Clean metadata on image loading.
* Avoid useless Z and T numbering when saving as "multiple files".
* Added missing plugins checking on start.
* Minor changes/fixes on inspector layout.
* Others minors changes.

**Developer**
* Thread safe implementation of XMLPreferences.
* Added PluginLauncher.startSafe(..) method which allow to handle exceptions.
* Reworked the ThreadUtil.invokeNow(Callable) method.
* Packed LociImporter class in LociImporterPlugin class.
* Some cleanup, refactoring and javadoc updates.


Icy 1.5.2.0  - 13/06/2014
------------------------

**Application**
* Modified way of retrieving the application folder (old method could fail on new OSX).
* Saver: removed the temporary hack to fix BioFormats save.
* Importer: fixed a minor issue with file path on Bio-Format importer.
* ROI:
  - fixed a issue with ROI subtraction operation.
  - fixed a issue with ROI2DShape intersects() method.
  - fixed issues with empty ROI2DArea.
* Rectangle5D: fixed infinite size C report.
* XMLUtil: added filterString(..) method to filter incorrect character.
* VTK:
  - fixed some VTK crashes due to multi thread access (even if protected).
  - fixed volume shading.
* minor change in plugin class loader for better handling on thread context class loader.
* others minors changes.

**Developer**
* ROI: fixed a issue with ROI2DShape intersects() method.
* Rectangle5D: fixed infinite size C report.
* XMLUtil: added filterString(..) method to filter incorrect character.
* VTK:
  - added lock() / unlock() method to require access to VTK rendering.
  - added access to VTK properties and actors (activeCam, axes, bounding box, ruler, image volume...)
  - added pick(..) method for 3D picking.
  - added setVolumeVisible(..) method to display/hide image volume object.
* some javadoc updates.
* some cleanup.


Icy 1.5.1.0  - 23/05/2014
------------------------

**Application**
* Fixed ROIUtil.getMassCenter() method.
* Fixed pixel size calculation on image resize operation.
* Fixed bug on ROI opacity change.
* Fixed LOD for ROI2DPoint and ROI2DLine.
* Fixed possible NPE in ROI panel.
* Fixed possible NPE in LUTViewer.
* Faulty plugins (corrupted jar file) are now automatically reinstalled.
* Others minors fixes and improvements.


Icy 1.5.0.0  - 19/05/2014
------------------------

**Application**
* Improved 3D visualization.
* Main menu:
  - Added 'Import' command for plugin capable of importing resources.  
  - Added 'Save Metadata' command to force save of the image metadata.
* ROI panel:
   - Subtraction command now produces the 2 possible results.
    You have to remove the incorrect one afterward.
   - Channel informations are now separated in several column (better for excel analysis).
   - Excel export now always export all ROI (as this is generally what we want).
   - Performance improvement when dealing with many ROIs
* Improved headless support.
* Fixed Java 8 compatibility issue.
* Added support for local repository.
* Fixed a bug making 1 pixel height or 1 pixel width ROI to become invisible.
* Simpler image resize operation.
* Swimming pool viewer improved.
* Many bug fixes and others improvements.

**Developer**
* Added Importer (and derived) interface to easily implement new resource / image importer from plugin.
* Added ImageProvider interface for flexible image access.
  This interface can be used in importer to provide low level image access.
* ThreadUtil: added runSingle(..) method. 
* Many changes in Loader interface.
* VTK:
  - New VTKCanvas class to replace Canvas3D.
  - Added VtkImageVolume class for easier volume rendering from an image stack.
  - New methods in VTKUtil.
* ROI:
   - Added contains(ROI) and intersects(ROI) methods.
   - Added ROIUtil.getMassCenter() method.
* SequenceUtil:
  - Added rotate() method.
  - Added toGray() and toRGB() methods.
* IcyBufferedImageUtil: added rotate() method.
* Added ActionDialog.isCanceled() method.
* MainInterface:
  - Added isHeadLess() method.
  - Added getCommandLineArgs() and getCommandLinePluginArgs() method (for headless mode mainly).
* FileUtil: added toFiles(paths) and toPath(files) methods.
* Some fix on ROI merge operations (again).
* Better error handling of incorrect ROI merge operation.
* Report frame now available also on internal application error.
* Updated Bio-Formats library to version 5.0.1
* Updated VTK to version 6.1 (some plugins using the old VTK API might not work anymore).
* Many others changes, deprecation, tweaks...

Icy 1.4.3.5  - 21/11/2013
------------------------

**Application**
* Some fix on ROI merge operations (again).
* Better error handling of incorrect ROI merge operation.
* Report frame now available also on internal application error.


Icy 1.4.3.0  - 21/11/2013
------------------------

**Application**
* Fixed some issues with ROI boolean merge operation.
* Modified the VTK Canvas3D screen shot rendering to RGB so it should always looks correct now. 
* Replaced the default 'Save' ribbon menu command by 'Save as' command instead.   
* Filter incorrect named classes in plugins folder to avoid useless warning messages.
* Minors changes in the About frame.
* Some others minors tweaks and changes.

**Developer**
* Severals fixes with BooleanMask and ROI bounds calculations.
* Rectangle3D/4D/5D: added setInfinity..() method.
* Some Javadoc changes.


Icy 1.4.2.0  - 13/11/2013
------------------------

**Application**
* Bug Report frame close automatically after report (regression fix).
* ChangeLog is now accessible directly through the application (close to the 'About' section).
* Some others minors tweaks and changes.

**Developer**
* Fixed native library loading from Plugin on unix like system.
* Javadoc changes.


Icy 1.4.1.0  - 12/11/2013
------------------------

**Application**
* ROIPanel: fixed area and volume unit representation.
* Removed image auto ordering when loading from the history.

**Developer**
* Fixed issue in getSubSequence(Sequence, ROI) method.
* Improved backward compatibility.
  Lot of compatibilities issues appeared in the last version because of some majors changes in ROI and Overlay in general.
  We are really sorry for the troubles and some should be fixed now but still it is preferable for developers to update their plugins which heavily rely on ROI and Overlay.
* Javadoc changes.


Icy 1.4.0.2  - 30/10/2013
------------------------

**Application**
* GUI:
   - now preserve last file filter used on load image dialog.
   - fixed multiple plugin launch from Ribbon plugin button.
   - fixed global shortcut action being executed while taping text in text field.
   - change log is displayed when a new version has been installed.
 * ROI / ROI Panel:
   - complete new panel with more ROI informations and XLS export support.
   - removed the 'selected color' property (selected ROI keep their normal color now).
   - added 'opacity' property to change the content opacity representation.
   - fixed point removing on polygon.
   - improved drawing performance (use Graphics clip information).
* Layer:
   - can now edit the layer's name directly through the table (as the ROI). 
   - accept delete command through the table.
   - some others minors tweaks.
* Colormap:
   - fixed color calculation with alpha blending.
   - active color line order modified (alpha is behind now).
   - tabs are now colored to reflect the channel colormap.
* Fill sequence operation now use the ROI properties to get the region to fill.
* Fixed issue in saving image in multiple file (temporary hack until issue is fixed in Bio-Formats).
* Minors changes and fixes in Sequence save operation (better name logic).
* F1 key now open a browser to the support forum.
* Fixed minor issue in plugin installation.
* Windows re-organization only affect viewer.
* ImageJ wrapper:
   - TRACED_ROI (ImageJ) are now converted to ROI2DArea.
   - fixed a minor issue with the IJ <---> Icy image conversion.
* Fixed a rendering issue with the screenshot feature on Canvas2D.
* Fixed the infamous margin error in text field.
* Fixed a bug with XML file containing '#' character in their filename.
* Fixed ROI painter event handling with Canvas3D.
* Fixed an issue with headless support (work in progress).
* Fixed a regression which made the Canvas3D visible even if VTK library was not loaded.
* More internals commands accessible through the search bar.
* Better error handling in general.
* Better backup of invalid XML metadata file.
* Removed the useless ROI tooltips.
* Added signature for the OSX launcher.
* Lot of others fixes, changes and tweaks.

**Developer**
* New developer online documentation based on javadoc (still need some work).
* Plugin:
   - added loadLibrary(..) method for easy native libraries loading from the plugin jar file.
* Sequence:
   - setSelectedROI(..) method rewrote.
* BooleanMask:
   - added BooleanMask3D, BooleanMask4D and BooleanMask5D classes.
   - added contains(BooleanMask) and intersects(BooleanMask) methods.
   - added getPointsAsIntArray(..) method (better for GC work)
   - added getContourPointsAsIntArray(..) method (better for GC work)
* ROI (WARNING. major changes which may need code modification):
   - general better 3D, 4D and 5D ROI support.
   - added default 5D point constructor for all ROI.
   - added hasSelectedPoint() method (used for cursor update in canvas)
   - added getBooleanMask2D(..) method
   - added boolean and subtraction operations support.
   - added canSetPosition() and setPosition5D() methods.
   - added canSetBounds() and setBounds5D(..) methods.
   - added contains(ROI) and intersects(ROI) methods.
   - added getNumberOfContourPoints() and getNumberOfPoints() methods (used for perimeter, area.. calculation).  
   - added icon property (used by the ROI panel).
   - internal overlay mouse position is now 5D.
   - better BooleanMask integration in ROI.
   - added ROI3DArea, ROI4DArea and ROI5DArea.  
   - ROI are now implemented as plugin.
   - many others changes.
* ROIUtil:
   - added boolean and subtraction operations for ROI list.
   - added getStandardDeviation(..) methods.
   - added getContourSize(..) and getInteriorSize(..) methods (return result in pixel).
   - added getPerimeter(..) and getArea(..) methods (return the result in the input sequence pixel unit).
   - added getSurfaceArea(..) and getVolume(..) methods (return the result in the input sequence pixel unit).
* PluginROI interface modified, plugins using it have to be updated !
* Painter interface deprecation completed:
   - OverlayWrapper class used for wrapping.
   - Layer, IcyCanvas, Sequence, ROI... now uses the Overlay only internally.
   - removed as much Painter reference as possible.
* Overlay:
   - events now support 5D coordinates.
   - added receiveKeyEventOnHidden and receiveMouseEventOnHidden properties.
   - added isAttached(Sequence) method.
   - do not anymore extends AbstractPainter.
* MainListener interface is deprecated, use one of these instead:
  GlobalViewerListener, GlobalSequenceListener, GlobalROIListener, GlobalOverlayListener, GlobalPluginListener.
* Replaced 'focused sequence/viewer' by 'active sequence/viewer' logic:
  FocusedSequenceListener becomes ActiveSequenceListener and FocusedViewerListener becomes ActiveViewerListener.
* IcyCanvas:
   - fixed default canvas size implementation.
   - provide helpers for mouse event dispatch to overlays.
   - minors changes and refactoring.
 * Canvas2D:
   - better support for future 3D/4D/5D ROI drawing.
* SequenceDataIterator and ImageDataIterator implements the new 5D support from ROI.
* Point3D / Point4D / Point5D classes improvements.
* Rectangle3D / Rectangle4D / Rectangle5D classes improvements.
* Added Dimension3D / Dimension4D / Dimension5D classes.
* ByteArrayConvert: added readXXX() / writeXXX() methods for easy serialization.
* Major rewrite of Processor and derived objects:
  - replaced addTask(..) method by submit(..)
  - support Callable interface and return Future object.
* ThreadUtil: added support for Callable interface and Future object.
* DynamicArray: added support for variable block size (granularity parameter).
* Added ActionManager class which allow to access command action in a centralized way (will be improved in future).
* Replaced XLSManager by XLSUtil so use the later one now.
* GeneralPreferences: added getResultFolder() and setResultFolder() methods to get/set the default application 'results' folder.
* IcyColorMap: added getRedMap(), getGreenMap()... methods.
* TypeUtil: added toPoint(int[]) method.
* FileUtil: added backup(..) method.
* ArrayMath: added sum(..) method for all native array type.
* ColorUtil: added many color conversion methods.
* ImageUtil: added toRGB(..) and toARGB(..) methods.
* Added ErrorReportFrame for easier generic report.
* Added IcyTable component (better column resizing policy than JTable).
* Added SpecialValueSpinner component.
* ImageSaverDialog can now return selected options.
* Viewer: can now start/stop play video sequence and change the desired frame rate from the code.
* Fixed FileUtil.setExtension(..) method.
* Rewrote SequenceChooser class (cleaner and updated to new method).
* Prelemenary Bio-Formats automatic error report.
* Many refactoring.
* Removed some old deprecated units.


Icy 1.3.6.0  - 22/05/2013
------------------------

**Application**
* Fixed copy/paste operation for image/ROI (IcyCanvas).
* Fixed deprecated method in Loader which was causing stack overflow.
* Fixed some image loading crash after an image has been saved.
* Improved Canvas3D screenshot performance.

**Developer**
* Clipboard: added methods for system clipboard handling.
* A bit of refactoring.


Icy 1.3.5.0  - 17/05/2013
------------------------

**Application**
* Added an option to disable the automatic image ordering when loading a group of image.
  It was always enabled before which may cause some issues.
* Improved online search (less intensive on the website  --> faster replies).
* Results from the search bar are now sorted depending their relevance.
* Canvas3D: channel can now be enabled/disabled the same way than with Canvas2D.
* Added image copy/paste operation from/to external application.
* Better Roi copy/paste operation.
* Added Bio-Formats metakit part of library (needed for some image format).
* Better default colormap for image with more than 3 channels.
* Fixed a bug in chat panel which prevented private chat to work.
* Fixed an issue with Viewer losing focus in some situation.
* Added predefined colomaps: "alpha" and "glow upper under". 
* Improved network error handling.
* Many fixes and improvements... 

**Developer**
* Added new Overlay class to replace the Painter interface:
  - add support for overlay priority.
  - automatic change event dispatch to sequence object.
  - many classes has been modified internally to use the new Overlay class, backward compatibility should be provided.
  - many others improvements, don't use the Painter anymore.
* Reworked Layer class:
  - handle new Overlay class.
  - dispatch event only for Layer properties change.
  - added fixed (cannot be removed) and readOnly (cannot change name) properties.
* Canvas2D:
  - All events are now sent to painter first then canvas.
   If a painter consume an event, canvas won't receive it.
  - Better support of getRenderedImage(...) image.
* Canvas3D: event are now correctly forwarded to painter.
* Loader: new methods, lot of rewrite...
* added onClosed() method to ActionDialog.
* Separated file choose and loading operation from ImageLoaderDialog class.
* Separated file choose and saving operation from ImageSaverDialog class.
* Added Plugin.getResource(..) and Plugin.getResources(..) methods.
* Added SystemUtil.addToJavaLibraryPath(..) method to modify library path at runtime.
* Added SystemUtil.loadLibrary(..) methods for easier native library loading.
* IcyBufferedImage: added method to load only a sub part of an image from Reader.
* New SequenceBuilder class to help in building a sequence with multi-threaded algorithms.
* Plugin bug report now give informations about dependencies. 
* PluginClassLoader:
  - removed Osgi loader (not needed there).
  - changed loader search order (system loader first  --> local loader last).
  - some fixes.
* Added Sequence.calculateSize(..) method to help in calculating a distance or surface from a specified number of pixel.
* Main interface do not anymore combine events for ROI/Painter add/remove operations.
   --> more practical for developer but can make application slower when dealing with many ROI/Painter.


Icy 1.3.2.0  - 18/02/2013
------------------------

**Application**
* Fixed an issue in local plugin search (search could not be done in online documentation of the plugin). 
* Removed 'empty' plugin from the research result list.
* Reverted to 'F3' shortcut for external / internal window mode.
* Fixed minors issues with viewer initialization.
* Replaced some Swing timer by Util timer (Swing timer tends to slowdown when you use too many of them).
* Automatically switch to headless mode for headless system (that make sense).
* Fixed regression in network loss detection (some latencies were added in Icy 1.3.0.0)
* Several minors improvements, tweaks and fixes. 

**Developer**
* Major rewrite of the plugin class loader, hope that will help with perm gen space error:
   - now support getResource(..) and getResources(..) methods (return valid URL).
   - far less memory usage (load resource on demand only).
   - can release loaded resources so if loader is retained by a class memory usage is maintained low.
* Fixed FileUtil.getCurrentDirectory() so it actually returns the application root directory
  and not the launch directory which can be different.
* General shortcut handled ad ActionMap / InputMap so a plugin can override them.


Icy 1.3.1.0  - 17/01/2013
------------------------

**Application**
* Added ROI boolean NOT operation (inverse ROI).
* Fixed GUI resize issue with the Image Resize frame.

**Developer**
* Fixed issue on save sequence operation when parent directory was missing.
* Fixed IcyButton tooltip when an action is attached to the button.
* Can now disable/enable histogram auto refresh and auto bounds from code.
* Fixed BooleanMask2D.setBounds(..) which was accidently hidden in Icy 1.3.0.0
* NetworkUtil: fixed openURL(..) backward compatibility.
* FileUtil: tried to fix the obscure bug on delete() and rename() methods (Windows system only).


Icy 1.3.0.0  - 10/01/2013
------------------------

**Application**
* Added a general search bar:
   - Search in plugins, protocols and kernel commands.
   - You can add you own results to search engine with a plugin.
* New ROI panel:
  - new Load / Save / Copy / Paste operations on ROI.
  - can change size and position depending the ROI type.
  - added subtraction boolean operation.
  - added surface / volume information.
  - added pixel intensity information. 
* New shortcuts:
   - 2/4/6/8 (keypad): image displacement (Canvas2D).
   - F2: duplicate view
   - F3: global search
   - F4: internalize/externalize view
   - Ctrl+F4: close view
   - Ctrl+O: open an image file
   - 0: disable view synchronization group
   - 1: enable/disable view synchronization group 1
   - 2: enable/disable view synchronization group 2
   - 3: enable/disable view synchronization group 3
   - 4: enable/disable view synchronization group 4
   - Shift+1: global view synchronization group 1
   - Shift+2: global view synchronization group 2
   - Shift+3: global view synchronization group 3
   - Shift+4: global view synchronization group 4
   - Shift+0: cancel global view synchronization
   - Shift+G: grid tile viewers in grid
   - Shift+H: horizontal tile viewers arrangement
   - Shift+V: vertical tile viewers arrangement
* Reworked ROI control in Canvas2D.
* Fixed proxy setting which should work now on Linux system.
* Improved plugin installation / udpate process.
* Fixed IcyBufferedImage bicubic filtered scaling.
* Fixed the infamous ComboBox selection bug in Dialog on MAC OSX system.
* Improved native librairie loading.
* Better handling of incorrect save format.
* Screenshot action now generate RGB image (ARGB is not supported by JPG nor AVI).
* Fixed Canvas3D screenshot fearure.
* Added support to launch a plugin at startup from the command line.
* Added support to open an image from command line.  
* Ctrl key is now used as mouse modifier on MAC OSX as Cmd key is
misinterpreted by JVM as right mouse button modifier.
* Better filename preservation in Sequence name.
* Improved histogram display.
* Added 'help' button in plugin manager to redirect on online plugin help.
* Fixed a bug in metadata pixelSize fields.
* Added new splash screens:)
* A lot of others minors fixes and improvements.

**Developer**
* Added new methods in specific ROI classes:
   - ROI2DEllipse.setEllipse(..)
   - ROI2DRectangle.setRectangle(..)
* Added Point3D, Point4D and Point5D classes (same implementation as java.awt.geom.Point2D).
* Added Rectangle3D, Rectangle4D and Rectangle5D classes (same implementation as java.awt.geom.Rectangle2D).
* ROI:
  - added getVolume() method (return volume in number of pixel).
  - added getPerimeter() method (return perimeter in number of pixel).
  - added getDimension() method (return number of dimension).
  - introduced contains(..) method (method was initially located in ROI2D, ROI3D... classes).
  - introduced intersect(..) method (method was initially located in ROI2D, ROI3D... classes).
* BooleanMask2D:
  - added getPoints() method (return points composing the mask).
  - added getComponents() method (return independent object mask).
  - added getEdgePoints() method (return mask contour points).
  - added getComponentsPoints() method (return points from each independent object).
* Centralized all kernels menu commands to actions:
  - FileActions: all file commands.
  - PreferencesActions: launch preferences panel commands.
  - SequenceOperationActions: sequence manipulation commands.
  - WindowActions: window arrangement / opening commands.
  - GeneralActions: all others commands.
  - RoiActions: ROI panel actions.
* Canvas2D:
  - added void translate(...) method.
* Added Clipboard class for easy copy/paste operations in Icy.
* IcyAbstractAction:
  - now supports background action execution (with progressbar)
  - added GetRichToolTip() method to retrieve the action RichToolTip.
* IcyCommandxxxButton now support IcyAbstractAction.
* added AbstractSequenceModel class for easy SequenceModel implementation.
* added PluginSearchProvider interface: a plugin implementing this interface provides results for the global search bar.
* Removed NetworkConnectionListener to only keep InternetAccessListener.
* IcyFrame:
  - added dispose() method to ensure frame close and release.
  - added getVisibleRect() method.
* Added new methods to the SwimmingPool class so we can now manipulate objects depending their names.
* Added SwimmingObjectChooser component to select specific object type from the swimming pool.
* Changed default pixel size unit to �m and time interval to second (compatible with OME).
* Added convertToType methods can now rescale values on data bounds.
* Added XMLPreferences.getXMLNode() method to get XML node.
* Added WeakWindowFocusListener class.
* Added methods to LookAndFeelUtil.
* Changed IcyTextField event.
* Added PluginRichToolTip class.


Icy 1.2.7.0  - 10/09/2012
------------------------

**Application**
* Entirely reworked the 'Sequence / Image operation' band:
   - new sequence resize feature.
   - new C/Z/T operations.
   - new color conversion feature.
   - general improvements.
* Better plugin error report.
* Better colormap preservation when switching from 2D to 3D canvas.
* Better Icy to ImageJ ROI conversion.
* Modified ROI2DPoint so it now intersects point and do not contains it (more consistent with other ROI).
* Better metadata support for multi serie image.
* Fixed preview in the image loading dialog so it consumes less memory if possible.
* Added chat status messages filtering.
* Added network proxy setting.
* Added link to online website and help.
* Added direct access to online plugins in taskbar.
* A lot of others minors fixes and improvements.

**Developer**
* Loader:
   - added methods so we can now specify serie(s) to load.
   - added 'getMetaData(File file)' method to retrieve the metadata of an image file.
   - added 'loadImage(File file, int z, int t)' method to load a single image from multi image file.
   - added methods to hide the progress bar.
* Added methods to Saver class to hide the progress bar while saving.
* Added IcyBufferedImage.copyData(src, int srcChannel, int dstChannel) to only copy data for a specified channel.
* Added ExcelTable component to display excel type table.
* Added abstract method 'getViewComponent()' to IcyCanvas so we can always retrieve the display component in a Canvas.
* Added many components for all Sequence tools (SequencePreviewPane, SequenceRangePreviewPanel...)
* Added IcyBufferedImageUtil class (general IcyBufferedImage tools).
* Added methods to SequenceUtil class.
* Fixed cursor preservation on Canvas2D.
* Added MainInterface.getSequences(String name) to retrieve sequence by name.
* Fixed a bug in ImageDataIterator.
* Fixed a minor bug in ROI.getBooleanMask() method.
* Fixed a minor bug in ROI2D.contains() and ROI2D.intersects() methods.
* Added BooleanMask2D.getSubtractionMask(..) method.
* Added ROI2D.merge(..) method to apply OR, AND, XOR binary operation on severals ROI2D.
* Added ROI2D.subtract(roi1, roi2) method to do ROI subtraction.
* Added ShapeUtil.merge(..) and ShapeUtil.subtract(..) operations.
* Sequence.metaChanged(String name) method is now public so you can use it to notify custom metadata changes.
* Added a command line option to remove splash-screen.
* Removed some deprecation in IcyCanvas.
* Lot of refactoring and source reorganization (check deprecations).


Icy 1.2.6.1  - 06/07/2012
------------------------

**Application**
* Fixed multiple image loading.
* Fixed multiple plugin error report frame.
* Better multi threads handling.
* Fixed a bug in FileUtil.delete(..) method which could cause lags.
* Improved preview (now support float data type) in the image load dialog.
* A lot of others minors fixed and improvements.

**Developer**
* Some code rewrite & cleanup in Plugin and Workspace management.
* Some others minors changes or refactoring.


Icy 1.2.6.0  - 29/06/2012
------------------------

**Application**
* TIF saving uses LZW compression by default.
* Added series selection dialog for multi series image file.
* Fixed daemons plugins start and stop operations.
* Fixed PermGen space problems.
* Better metadata preservation.
* cleanup and minors fixes...

**Developer**
* PluginUpdater class has been splited in PluginUpdater and PluginUpdateFrame classes.
* Added ThumbnailComponent class to display image thumbnail with basic description.
* Added some methods in Loader class for better sequence / image / metadata loading...
* Added MetaDataUtil class which provide metadata utilities.
* Rewrote DataType class so getEnumContants() is working correctly.
* Simplified DataIterator.


Icy 1.2.5.0  - 18/06/2012
------------------------

**Application**
* Improved GUI for colormap model loading.
* Improved native libraries loading.
* Preliminary headless support.
* Lot of minors fixes and improvements...

**Developer**
* Added new general GUI components:
   - RangeSlider (slider representing a range).
   - ColormapIcon (icon representing a colormap).
* Added new GUI components for basic Sequence operation:
   - SequencePreviewPanel (sequence preview).
   - SequenceChooserPanel.
   - SequenceRangePreviewPanel class.
   - SequenceChooserPreviewPanel class.
   - SequenceChooserRangePreviewPanel class.
* Added PluginBundled interface when you need to bundle severals
  classes extending Plugin in a single JAR file (bundled plugins are
  usually hidden).
* Added HandledException class (permit to avoid report dialog for handled exception).
* Improved RangeComponent (can now display inner RangeSlider component).
* Improved SimpleITK integration.
* Added SwingX library.


Icy 1.2.4.1  - 21/05/2012
------------------------

Minor fix in update processing and OSX launch.


Icy 1.2.4.0  - 21/05/2012
------------------------

**Application**
* Internet connection monitor (the CPU/memory monitor panel now displays an icon when you are not connected to internet).
* Ribbon minimization state is now saved and restored after applciation restart.
* Fixed ImageJ menu disappearing on MAC OSX.
* Plugin detail window rewritten (now correctly display HTML formated description).
* Improved window tile arrangement (take previous window position in account).
* Others tweaks, fixes or cleanup...

**Developer**
* Added IdConfirmDialog class: 
  A ConfirmDialog containing a checkbox to not display confirmation next time.
* Removed IcyFileUtil and IcyNetworkUtil classes.
* Moved Plugin.report(...) to IcyExceptionHandler.report(...)
* Renamed FileDrop.Listener to FileDrop.FileDropListener.


Icy 1.2.3.1  - 26/04/2012
------------------------

Minor bug fix in image saving.


Icy 1.2.3.0  - 26/04/2012
------------------------

**Application**
* Improved ImageJ integration:
   - ImageJ is now patched at runtime so we can use official builds and even update them.
   - no more AWT component, less refresh problems in general.
   - basic methods to convert Icy Sequence to ImageJ ImagePlus (with ROI preservation) and vice versa.
* Added safe background processes termination check when application exit.
* Canvas:
   - fixed a bug in layers display and event dispatch ordering.
* Canvas2D:
   - removed zoom operation on double-click (already on mouse wheel).
   - added shift modifier for aligned translation / rotation / scaling operations.
   - added filtering option.
* Canvas3D:
   - pixel size is taken from sequence metadata information (no more duplicated information).
* Loader:
   - multi series image are now opened in different sequences.
* ROI:
   - implemented alignment assistance with shift drag operation.
   - modified / tweaked ROI manipulation to make it more consistent.
   - improved rendering.
* Preferences:
   - added Canvas preferences panel.
   - added startup (daemon) plugins setup panel.
* Added an option to enable/disable sequence bounds refresh on the Histogram.
* Better metadata preservation on sequence save.
* File drag and drag operation now works also in multi windows mode.
* Added a workaround for the MAC OS JVM 1.6.0_29 update which cause a memory leak.
* Fixed an error on ROI creation when VTK was missing.
* Fixed an error in image saving (regression in 1.2.2.0).
* Lot of others minors fixes, tweaks and improvements.

**Developer**
* Added Parallel Colt library.
* Added Xuggler library.
* Added DataIterator interface:
  This interface provides iteration for both read and write operation on double data.
* Added ImageDataIterator and SequenceDataIterator class for easy IcyBufferedImage and Sequence data manipulation.
* Added DataIteratorMath for basic math operations on DataIterator.
* Renamed CloseTabbedPane  --> CloseableTabbedPane.
* Added ConfirmFrame class (non modal ConfirmDialog).
* Added LineAnchor2D and RectAnchor2D for easy Line / Square alignment on shift drag operation.
* IcyCanvas:
   - added canvasToImageLogDelta() methods.
* IcyCanvas2D:
   - added canvasToImageLogDelta() methods.
   - added getTransform() & getInverseTransform() methods which return AffineTransform object for fast canvas <--> image Graphics context change.
* Canvas2D:
   - give access now to CanvasView and CanvasMap objects.
* Canvas3D:
   - vtkPainter are now automatically added or removed to the renderer.
* ROI:
   - added getPosition / setPosition method.
   - added ROIUtil class which provides utilities for ROI.
   - added ROI.setEditable(..) and ROI.isEditable() methods so now we can have "Read Only" ROI.
   - added setPoints() methods to ROI2DPolygon and ROI2DPolyline.  
* ROIEvent:
   - added FOCUS, SELECTION and PROPERTY change event type.
   - removed POINT_xxx event type.
   - PROPERTY_CHANGE replace the NAME_CHANGE type event.  
* IcyIcon:
   - added supports to normal color image icon (use the 'alpha' proterty).
   - added setColor(Color) method to define the icon colorization when using alpha mask type image.
* Added GraphicsUtil class which provide some Graphics facilities as drawing multi line text, calculating text bounds...
* Sequence metadata object changed to OMEXMLMetadataImpl.
* Worker thread has name so we can recognize them.
* Added PluginThreaded interface so plugin start in a separate thread (replace old PluginStartAsThread which was not fully functional).
* Added PluginDaemon interface so plugin start automatically on application initialization.
  This is useful for enhancer type plugin.
* Some refactoring.


Icy 1.2.2.0  - 14/02/2012
------------------------

**Application**
* Improved / fixed sequence image ordering from filename.
* Many improvements on Chat:
   - faster desktop rendering (fix lag issue on OSX).
   - support style & coloring from IRC.
   - support multi channels chat.
   - preferences panel.
* Some others minors fixes and improvements.

**Developer**
* Added CloseTabbedPane component (basically a JTabbedPane with closable tabs).
* Added IRCUtil class (IRC utilities).
* Minors changes in Pixel/Band/ImagePosition classes.


Icy 1.2.1.1  - 03/02/2012
------------------------

Minor fix in chat nickname change.


Icy 1.2.1.0  - 03/02/2012
------------------------

**Application**
* Added support for "multi windows mode" (� la GIMP or ImageJ).
* Added internal chat so users can discuss together easily:)
* Canvas2D:
  - fixed zoom centering when using '+' / '-' buttons.
  - added invert mouse wheel axis option.
* Cascade and Tile windows organization now work on multi screen.
* Fixed plugin installation from private repository protected by password.
* Some others minors fixes and improvements.


**Developer**
* Improved ExternalizablePanel component, it now uses an IcyFrame for its frame form.
* Added overlay support to IcyDesktopPane (easy drawing on application DesktopPane).
* Added DateUtil (Data utilities) class.
* Added ExtTabbedPane component (JTabbedPane with ExternalizablePanel support).
* More access to Viewer and IcyCanvas object.
* T and Z navigation panel moved to IcyCanvas.
  So now when you override IcyCanvas you have to take care about panels configurations.
* added authentification support for NetworkUtil.download(...) method.
* Added WeakListener abstract class for easy weak listener implementation.
* some refactoring.



Icy 1.2.0.0  - 10/01/2012
------------------------

* ImageJ integration: you can now use ImageJ directly from Icy !
  This is still in beta stage and you may experiences some problems.
  You will see some errors messages coming from the ImageJ GUI construction, this is normal, just ignore them.
  We can remove completly these messages but we want to avoid bad practices with GUI creation and modification.
* Canvas2D: added new synchronization method.
* Canvas3D: severals fixes and improvements.
* Better meta data handling (compatible with OME).
* Fixed double image loading.
* Improved colormap handling for multi channel image.
* Colormap can be disabled.
* Optimization in colormap rendering.
* Can now disable colormap channel.
* Improved GUI update in general (faster).
* Reworked online plugins loader (simpler and faster).
* many internals changes and improvements.
* added JAI ImageIO library for JPEG2000 image.
* updater improvements.


Icy 1.1.3.0  - 14/11/2011
------------------------

* fixed bug with files restoring on update processing.
* minor changes on cropper tool.


Icy 1.1.2.0  - 08/01/2011
------------------------

* fixed a bug with unsigned short data type image.
* improved update processing.
