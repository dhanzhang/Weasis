/*******************************************************************************
 * Copyright (C) 2009-2018 Weasis Team and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.launcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.felix.framework.util.Util;

public class FileUtil {

    private static final Logger LOGGER = Logger.getLogger(FileUtil.class.getName());

    public static final int FILE_BUFFER = 4096;

    private FileUtil() {
    }

    public static void safeClose(final AutoCloseable object) {
        if (object != null) {
            try {
                object.close();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Cannot close AutoCloseable", e); //$NON-NLS-1$
            }
        }
    }

    public static void recursiveDelete(File rootDir, boolean deleteRoot) {
        if ((rootDir == null) || !rootDir.isDirectory()) {
            return;
        }
        File[] childDirs = rootDir.listFiles();
        if (childDirs != null) {
            for (File f : childDirs) {
                if (f.isDirectory()) {
                    // deleteRoot used only for the first level, directory is deleted in next line
                    recursiveDelete(f, false);
                    deleteFile(f);
                } else {
                    deleteFile(f);
                }
            }
        }
        if (deleteRoot) {
            deleteFile(rootDir);
        }
    }

    public static final void deleteDirectoryContents(final File dir, int deleteDirLevel, int level) {
        if ((dir == null) || !dir.isDirectory()) {
            return;
        }
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isDirectory()) {
                    deleteDirectoryContents(f, deleteDirLevel, level + 1);
                } else {
                    deleteFile(f);
                }
            }
        }
        if (level >= deleteDirLevel) {
            deleteFile(dir);
        }
    }

    private static boolean deleteFile(File fileOrDirectory) {
        try {
            Files.delete(fileOrDirectory.toPath());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot delete", e); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    public static boolean delete(File fileOrDirectory) {
        if (fileOrDirectory == null || !fileOrDirectory.exists()) {
            return false;
        }

        if (fileOrDirectory.isDirectory()) {
            final File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    delete(child);
                }
            }
        }
        return deleteFile(fileOrDirectory);
    }

    public static void writeStream(InputStream inputStream, OutputStream out) {
        if (inputStream == null || out == null) {
            return;
        }
        try {
            byte[] buf = new byte[FILE_BUFFER];
            int offset;
            while ((offset = inputStream.read(buf)) > 0) {
                out.write(buf, 0, offset);
            }
            out.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error when writing stream", e); //$NON-NLS-1$
        } finally {
            FileUtil.safeClose(inputStream);
            FileUtil.safeClose(out);
        }
    }

    public static File getApplicationTempDir() {
        String tempDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
        File tdir;
        if (tempDir == null || tempDir.length() == 1) {
            String dir = System.getProperty("user.home", ""); //$NON-NLS-1$ //$NON-NLS-2$
            tdir = new File(dir);
        } else {
            tdir = new File(tempDir);
        }
        return new File(tdir, "weasis-" + System.getProperty("user.name", "tmp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public static boolean readProperties(File propsFile, Properties props) {
        if (propsFile.canRead()) {
            try (FileInputStream fis = new FileInputStream(propsFile)) {
                props.load(fis);
                return true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e, () -> String.format("Loading %s", propsFile.getPath())); //$NON-NLS-1$
            }
        } else {
            File appFoler = new File(System.getProperty(WeasisLauncher.P_WEASIS_PATH, "")); //$NON-NLS-1$
            appFoler.mkdirs();
        }
        return false;
    }

    public static void storeProperties(File propsFile, Properties props, String comments) {
        if (props != null && propsFile != null) {
            try (FileOutputStream fout = new FileOutputStream(propsFile)) {
                props.store(fout, comments);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error when writing properties", e); //$NON-NLS-1$
            }
        }
    }

    public static String writeResources(String srcPath, File cacheDir, String date) throws IOException {
        String fileDate = null;

        URLConnection urlConnection = FileUtil.getAdaptedConnection(new URL(srcPath), false);
        long last = urlConnection.getLastModified();
        if (last != 0) {
            fileDate = Long.toString(last);
        }
        // Rebuild a cache for resources based on the last modified date
        if (!cacheDir.canRead() || date == null || !date.equals(fileDate)) {
            recursiveDelete(cacheDir, false);
            unzip(urlConnection.getInputStream(), cacheDir);
        }
        return fileDate;
    }

    public static URLConnection getAdaptedConnection(URL url, boolean useCaches) throws IOException {
        URLConnection connection = url.openConnection();
        // Prevent caching of Java WebStart.
        connection.setUseCaches(useCaches);
        // Support for http proxy authentication.
        String p = url.getProtocol();
        String pauth = System.getProperty("http.proxyAuth", null); //$NON-NLS-1$
        if (Utils.hasText(pauth) && ("http".equals(p) || "https".equals(p))) { //$NON-NLS-1$ //$NON-NLS-2$
            String base64 = Util.base64Encode(pauth);
            connection.setRequestProperty("Proxy-Authorization", "Basic " + base64); //$NON-NLS-1$ //$NON-NLS-2$
        }

        String auth = System.getProperty("http.authorization", null); //$NON-NLS-1$
        if (Utils.hasText(auth) && ("http".equals(p) || "https".equals(p))) { //$NON-NLS-1$ //$NON-NLS-2$
            connection.setRequestProperty("Authorization", auth); //$NON-NLS-1$ 
        }

        return connection;
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        if (in == null || out == null) {
            return;
        }
        byte[] buf = new byte[FILE_BUFFER];
        int offset;
        while ((offset = in.read(buf)) > 0) {
            out.write(buf, 0, offset);
        }
        out.flush();
    }

    private static void copyZip(InputStream in, File file) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            copy(in, out);
        }
    }

    public static void unzip(InputStream inputStream, File directory) throws IOException {
        if (inputStream == null || directory == null) {
            return;
        }

        try (BufferedInputStream bufInStream = new BufferedInputStream(inputStream);
                        ZipInputStream zis = new ZipInputStream(bufInStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(directory, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    copyZip(zis, file);
                }
            }
        } finally {
            FileUtil.safeClose(inputStream);
        }
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B"; //$NON-NLS-1$
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre); //$NON-NLS-1$
    }
}
