/*
 * Copyright (c) 2010-2026. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.icy.network.update;

import fr.icy.common.Version;
import fr.icy.common.string.StringUtil;
import fr.icy.io.FileUtil;
import fr.icy.io.xml.XMLPersistent;
import fr.icy.io.xml.XMLUtil;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class ElementDescriptor implements XMLPersistent {
    private static final Logger LOGGER = Logger.getLogger(ElementDescriptor.class.getName());

    private static final String ID_NAME = "name";
    private static final String ID_VERSION = "version";
    private static final String ID_FILES = "files";
    private static final String ID_FILE = "file";
    private static final String ID_LINK = "link";
    private static final String ID_EXECUTE = "execute";
    private static final String ID_WRITE = "write";
    private static final String ID_DIRECTORY = "directory";
    private static final String ID_FILE_NUMBER = "fileNumber";
    private static final String ID_DATE_MODIF = "datemodif";
    private static final String ID_LOCAL_PATH = "localpath";
    private static final String ID_ONLINE_PATH = "onlinepath";
    private static final String ID_CHANGES_LOG = "changeslog";

    public static class ElementFile implements XMLPersistent {
        private String localPath;
        private String onlinePath;

        /**
         * symbolic link element, onlinePath define the target of the link file
         */
        private boolean link;

        /**
         * need execute permission
         */
        private boolean executable;

        /**
         * need write permission
         */
        private boolean writable;

        /**
         * directory file.
         */
        private boolean directory;

        /**
         * date of modification
         */
        private long dateModif;

        /**
         * number of files (for directory only, -1 = don't check file number)
         */
        private int fileNumber;

        /**
         *
         */
        public ElementFile(final Node node) {
            super();

            loadFromXML(node);
        }

        /**
         * Create a new element file using specified element information
         */
        @Contract(pure = true)
        public ElementFile(final @NonNull ElementFile elementFile) {
            super();

            localPath = elementFile.localPath;
            onlinePath = elementFile.onlinePath;
            dateModif = elementFile.dateModif;
            link = elementFile.link;
            executable = elementFile.executable;
            writable = elementFile.writable;
            directory = elementFile.directory;
            fileNumber = elementFile.fileNumber;
        }

        @Override
        public boolean loadFromXML(final @Nullable Node node) {
            if (node == null)
                return false;

            localPath = XMLUtil.getElementValue(node, ID_LOCAL_PATH, "");
            onlinePath = XMLUtil.getElementValue(node, ID_ONLINE_PATH, "");
            dateModif = XMLUtil.getElementLongValue(node, ID_DATE_MODIF, 0L);
            link = XMLUtil.getElementBooleanValue(node, ID_LINK, false);
            executable = XMLUtil.getElementBooleanValue(node, ID_EXECUTE, false);
            writable = XMLUtil.getElementBooleanValue(node, ID_WRITE, false);
            directory = XMLUtil.getElementBooleanValue(node, ID_DIRECTORY, false);
            fileNumber = XMLUtil.getElementIntValue(node, ID_FILE_NUMBER, 1);

            return true;
        }

        @Override
        public boolean saveToXML(final @Nullable Node node) {
            return saveToNode(node, true);
        }

        boolean saveToNode(final @Nullable Node node, final boolean onlineSave) {
            if (node == null)
                return false;

            XMLUtil.addElement(node, ID_LOCAL_PATH, localPath);

            if (onlineSave) {
                XMLUtil.addElement(node, ID_ONLINE_PATH, onlinePath);
                XMLUtil.addElement(node, ID_DATE_MODIF, Long.toString(dateModif));
                if (link)
                    XMLUtil.addElement(node, ID_LINK, Boolean.toString(link));
                if (executable)
                    XMLUtil.addElement(node, ID_EXECUTE, Boolean.toString(executable));
                if (writable)
                    XMLUtil.addElement(node, ID_WRITE, Boolean.toString(writable));
                if (directory) {
                    XMLUtil.addElement(node, ID_DIRECTORY, Boolean.toString(directory));
                    XMLUtil.addElement(node, ID_FILE_NUMBER, Integer.toString(fileNumber));
                }
            }

            return true;
        }

        public boolean isEmpty() {
            return StringUtil.isEmpty(localPath) && StringUtil.isEmpty(onlinePath);
        }

        public boolean exists() {
            return FileUtil.exists(localPath);
        }

        /**
         * @return the localPath
         */
        public String getLocalPath() {
            return localPath;
        }

        /**
         * @return the onlinePath
         */
        public String getOnlinePath() {
            return onlinePath;
        }

        /**
         * @return the dateModif
         */
        public long getDateModif() {
            return dateModif;
        }

        /**
         * @return the link
         */
        public boolean isLink() {
            return link;
        }

        /**
         * @return the executable
         */
        public boolean isExecutable() {
            return executable;
        }

        /**
         * @return the writable
         */
        public boolean isWritable() {
            return writable;
        }

        /**
         * @return the directory
         */
        public boolean isDirectory() {
            return directory;
        }

        /**
         * @return the fileNumber
         */
        public int getFileNumber() {
            return fileNumber;
        }

        /**
         * @param dateModif the dateModif to set
         */
        public void setDateModif(final long dateModif) {
            this.dateModif = dateModif;
        }

        /**
         * @param link the link to set
         */
        public void setLink(final boolean link) {
            this.link = link;
        }

        /**
         * @param executable the executable to set
         */
        public void setExecutable(final boolean executable) {
            this.executable = executable;
        }

        /**
         * @param writable the writable to set
         */
        public void setWritable(final boolean writable) {
            this.writable = writable;
        }

        /**
         * @param directory the directory to set
         */
        public void setDirectory(final boolean directory) {
            this.directory = directory;
        }

        /**
         * @param fileNumber the fileNumber to set
         */
        public void setFileNumber(final int fileNumber) {
            this.fileNumber = fileNumber;
        }

        /**
         * Return true if the specified ElementFile is the same as the current one.<br>
         *
         * @param elementFile          the element file to compare
         * @param compareOnlinePath    specify if we compare online path information
         * @param compareValidDateOnly true if we do compare only valid date (!= 0)
         */
        public boolean isSame(final ElementFile elementFile, final boolean compareOnlinePath, final boolean compareValidDateOnly) {
            if (elementFile == null)
                return false;

            if (!StringUtil.equals(elementFile.localPath, localPath))
                return false;
            if (compareOnlinePath && (!StringUtil.equals(elementFile.onlinePath, onlinePath)))
                return false;
            // -1 means we don't check file number
            if ((elementFile.fileNumber != -1) && (fileNumber != -1)) {
                if (elementFile.fileNumber != fileNumber)
                    return false;
            }

            if ((elementFile.dateModif == 0) || (dateModif == 0)) {
                // don't compare dates if one is invalid
                // one of the dates is not valid --> can't compare
                return compareValidDateOnly;
            }

            return (elementFile.dateModif == dateModif);
        }

        @Override
        public String toString() {
            return FileUtil.getFileName(localPath);
        }

    }

    private String name;
    private Version version;
    private final ArrayList<ElementFile> files;
    private String changelog;

    /**
     *
     */
    public ElementDescriptor(final Node node) {
        super();

        files = new ArrayList<>();

        loadFromXML(node);
    }

    /**
     * Create a new element descriptor using specified element information
     */
    public ElementDescriptor(final @NonNull ElementDescriptor element) {
        super();

        name = element.name;
        version = Version.fromString(element.version.toString());
        changelog = element.changelog;

        files = new ArrayList<>();

        for (final ElementFile f : element.files)
            files.add(new ElementFile(f));
    }

    @Override
    public boolean loadFromXML(final @Nullable Node node) {
        if (node == null)
            return false;

        name = XMLUtil.getElementValue(node, ID_NAME, "");
        final String v = XMLUtil.getElementValue(node, ID_VERSION, "");
        LOGGER.config("Element: " + name + " version: " + v);
        version = Version.fromString(v);
        changelog = XMLUtil.getElementValue(node, ID_CHANGES_LOG, "");

        final ArrayList<Node> nodesFile = XMLUtil.getChildren(XMLUtil.getElement(node, ID_FILES), ID_FILE);
        if (nodesFile != null) {
            for (final Node n : nodesFile) {
                final ElementFile elementFile = new ElementFile(n);

                if (!elementFile.isEmpty())
                    files.add(elementFile);
            }
        }

        return true;
    }

    @Override
    public boolean saveToXML(final @Nullable Node node) {
        return saveToNode(node, true);
    }

    public boolean saveToNode(final @Nullable Node node, final boolean onlineSave) {
        if (node == null)
            return false;

        XMLUtil.addElement(node, ID_NAME, name);
        XMLUtil.addElement(node, ID_VERSION, version.toShortString());

        // some information isn't needed for local version
        if (onlineSave)
            XMLUtil.addElement(node, ID_CHANGES_LOG, changelog);

        final Element filesNode = XMLUtil.addElement(node, ID_FILES);
        for (final ElementFile elementFile : files)
            elementFile.saveToNode(XMLUtil.addElement(filesNode, ID_FILE), onlineSave);

        return true;
    }

    /**
     * return ElementFile containing the specified local path
     */
    public @Nullable ElementFile getElementFile(final @NonNull String localPath) {
        for (final ElementFile file : files)
            if (file.getLocalPath().compareToIgnoreCase(localPath) == 0)
                return file;

        return null;
    }

    /**
     * return true if the element contains the specified local path
     */
    public boolean hasLocalPath(final String localPath) {
        return getElementFile(localPath) != null;
    }

    public boolean addElementFile(final ElementFile file) {
        return files.add(file);
    }

    public boolean removeElementFile(final ElementFile file) {
        return files.remove(file);
    }

    public void removeElementFile(final String localPath) {
        removeElementFile(getElementFile(localPath));
    }

    /**
     * Validate the current element descriptor.<br>
     * It actually removes missing files from the element.<br>
     * Return true if all files are valid.
     */
    public boolean validate() {
        boolean result = true;

        for (int i = files.size() - 1; i >= 0; i--) {
            final ElementFile elementFile = files.get(i);
            final File file = new File(elementFile.getLocalPath());

            if (file.exists()) {
                // update modification date
                elementFile.setDateModif(file.lastModified());

                // directory file?
                if (file.isDirectory()) {
                    // update directory information
                    elementFile.setDirectory(true);
                    elementFile.setFileNumber(FileUtil.getFiles(file, null, true, false, false).length);
                }
            }
            else {
                // remove missing file
                files.remove(i);
                result = false;
            }
        }

        return result;
    }

    public boolean isValid() {
        for (final ElementFile file : files)
            if (!file.exists())
                return false;

        return true;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * @return the number of files
     */
    public int getFilesNumber() {
        return files.size();
    }

    /**
     * @return the files
     */
    public ArrayList<ElementFile> getFiles() {
        return files;
    }

    /**
     * @return the specified file
     */
    public ElementFile getFile(final int index) {
        return files.get(index);
    }

    /**
     * @return the changelog
     */
    public String getChangelog() {
        return changelog;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(final Version version) {
        this.version = version;
    }

    /**
     * Return true if the specified ElementDescriptor is the same as the current one.<br>
     *
     * @param element               the element descriptor to compare
     * @param compareFileOnlinePath specify if we compare file online path information
     */
    public boolean isSame(final ElementDescriptor element, final boolean compareFileOnlinePath) {
        if (element == null)
            return false;

        // different name
        if (!name.equals(element.name))
            return false;
        // different version
        if (!version.equals(element.version))
            return false;
        // different number of files
        if (files.size() != element.files.size())
            return false;

        // compare files
        for (final ElementFile file : files) {
            final ElementFile elementFile = element.getElementFile(file.getLocalPath());

            // file missing --> different
            if (elementFile == null)
                return false;

            // file different (compare date only if they are valid) --> different
            if (!elementFile.isSame(file, compareFileOnlinePath, true))
                return false;
        }

        // same element
        return true;
    }

    // TODO Remove this temporary method
    @Contract("_, null, _ -> null")
    @Deprecated(forRemoval = true)
    public static ElementDescriptor getUpdateElement(final @Nullable ElementDescriptor localElement, final @Nullable ElementDescriptor onlineElement, final boolean force) {
        if (onlineElement == null)
            return null;

        // use a copy
        final ElementDescriptor result = new ElementDescriptor(onlineElement);

        if (localElement == null)
            return result;
        // different name
        if (!StringUtil.equals(result.name, localElement.name))
            return result;

        // if same version, compare files on valid date only
        final boolean compareValidDateOnly = result.version.equals(localElement.version);

        // compare files
        for (int i = result.files.size() - 1; i >= 0; i--) {
            final ElementFile onlineFile = result.files.get(i);
            final ElementFile localFile = localElement.getElementFile(onlineFile.getLocalPath());

            // same file? --> remove it (no need to be updated)
            if (!force) {
                if ((localFile != null) && onlineFile.isSame(localFile, false, compareValidDateOnly))
                    result.files.remove(i);
            }
        }

        // no files to update? --> return null
        if (result.files.isEmpty())
            return null;

        return result;
    }

    /**
     * Process and return the update the element which contains differences<br>
     * from the specified local and online elements.<br>
     * If the local element refers to the same item, only missing or different files will remain.<br>
     * If the local element refers to a different element, the online element is returned unchanged.
     *
     * @return the update element (null if local and online elements are the same)
     */
    @Contract("_, null -> null")
    public static ElementDescriptor getUpdateElement(final ElementDescriptor localElement, final ElementDescriptor onlineElement) {
        if (onlineElement == null)
            return null;

        // use a copy
        final ElementDescriptor result = new ElementDescriptor(onlineElement);

        if (localElement == null)
            return result;
        // different name
        if (!StringUtil.equals(result.name, localElement.name))
            return result;

        // if same version, compare files on valid date only
        final boolean compareValidDateOnly = result.version.equals(localElement.version);

        // compare files
        for (int i = result.files.size() - 1; i >= 0; i--) {
            final ElementFile onlineFile = result.files.get(i);
            final ElementFile localFile = localElement.getElementFile(onlineFile.getLocalPath());

            // same file? --> remove it (no need to be updated)
            if ((localFile != null) && onlineFile.isSame(localFile, false, compareValidDateOnly))
                result.files.remove(i);
        }

        // no files to update? --> return null
        if (result.files.isEmpty())
            return null;

        return result;
    }

    /**
     * Update the current element with information from the specified element
     */
    public void update(final @NonNull ElementDescriptor updateElement) {
        // update version info
        version = updateElement.version;

        // updateElement contains only new or modified files (do not contain unmodified ones),
        // so we have to add or update files but not remove old ones.
        for (final ElementFile updateFile : updateElement.files) {
            // get corresponding file
            final ElementFile localFile = getElementFile(updateFile.getLocalPath());

            // is the file missing? --> add it
            if (localFile == null)
                files.add(updateFile);
            else {
                // update file (we don't care about online information)
                localFile.setDateModif(updateFile.getDateModif());
                localFile.setExecutable(updateFile.isExecutable());
                localFile.setLink(updateFile.isLink());
                localFile.setWritable(updateFile.isWritable());
                localFile.setDirectory(updateFile.isDirectory());
            }
        }
    }

    @Override
    public String toString() {
        return name + " " + version.toShortString();
    }
}
