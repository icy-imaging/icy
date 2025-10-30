/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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

package org.bioimageanalysis.icy.extension;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.system.UserUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public final class ExtensionInstaller {
    private ExtensionInstaller() {
        //
    }

    public static boolean installExtension(final @NotNull File jar) {
        if (!jar.exists() || !jar.isFile() || !jar.getAbsolutePath().endsWith(".jar")) {
            IcyLogger.error(ExtensionInstaller.class, "File is not a .jar file, cannot install extension");
            return false;
        }

        try {
            final ExtensionDescriptor ed = new ExtensionDescriptor(jar);
            final String fullPath = ed.getGroupId() + "." + ed.getArtifactId();
            final File extensionDir = new File(fullPath.replaceAll("\\.", File.separator));
            if (!extensionDir.exists()) {
                extensionDir.mkdirs();

                final File copiedJar = new File(extensionDir, ed.getArtifactId() + ".jar");
                Files.copy(jar.toPath(), copiedJar.toPath());
            }
        }
        catch (final Throwable t) {
            IcyLogger.error(ExtensionInstaller.class, t, "Cannot install extension: " + jar.getAbsolutePath());
        }

        return true;
    }

    private static boolean appendExtension(final @NotNull ExtensionDescriptor ed) {
        final String fullPath = ed.getGroupId().replaceAll("\\.", File.separator) + File.separator + ed.getArtifactId() + ".jar";
        final File extensionConfigFile = new File(UserUtil.getIcyExtensionsDirectory(), "ext.bin");
        if (!extensionConfigFile.exists()) {
            // Should not happen, because the config file is always created before that, but just in case...
            final List<Map<String, Object>> list = new ArrayList<>();
            list.add(Map.of("path", fullPath));
            final Yaml yaml = new Yaml();
            final String str = yaml.dump(list);
            final byte[] data = Base64.getEncoder().encode(str.getBytes(StandardCharsets.ISO_8859_1));
            try (final OutputStream os = new FileOutputStream(extensionConfigFile)) {
                os.write(data);
            }
            catch (final Throwable t) {
                IcyLogger.error(ExtensionInstaller.class, t, "Unable to write bin file");
                return false;
            }
        }
        else {
            try (final InputStream is = new FileInputStream(extensionConfigFile)) {
                final byte[] readRawData = is.readAllBytes();
                final byte[] readData = Base64.getDecoder().decode(readRawData);
                final StringBuilder sb = new StringBuilder();
                for (final byte readDatum : readData)
                    sb.append((char) readDatum);

                final Yaml yaml = new Yaml();
                final List<Map<String, Object>> list = yaml.load(sb.toString());
                boolean found = false;
                for (final Map<String, Object> map : list) {
                    if (map.get("path").equals(fullPath)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    list.add(Map.of("path", fullPath));

                    dumpData(list, extensionConfigFile);
                }
            }
            catch (final Throwable t) {
                IcyLogger.error(ExtensionInstaller.class, t, "Unable to write bin file");
                return false;
            }
        }

        return true;
    }

    private static void dumpData(final @NotNull List<Map<String, Object>> list, final @NotNull File extensionsBinaryFile) throws Throwable {
        final Yaml yaml = new Yaml();
        final String dump = yaml.dump(list);
        final byte[] data = Base64.getEncoder().encode(dump.getBytes(StandardCharsets.ISO_8859_1));

        try (final OutputStream os = new FileOutputStream(extensionsBinaryFile)) {
            os.write(data);
        }
    }
}
