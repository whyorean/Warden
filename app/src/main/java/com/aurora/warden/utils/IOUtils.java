/*
 * Warden
 * Copyright (C) 2020, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.aurora.warden.utils;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static void bytesToFile(byte[] bytes, File result) throws IOException {
        BufferedOutputStream bos =
                new BufferedOutputStream(new FileOutputStream(result));
        bos.write(bytes);
        bos.flush();
        bos.close();
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDirRecursive(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDirRecursive(File directory) {
        if (directory != null && directory.isDirectory()) {
            String[] children = directory.list();
            for (String child : children) {
                boolean success = deleteDirRecursive(new File(directory, child));
                if (!success) {
                    return false;
                }
            }
            return directory.delete();
        } else if (directory != null && directory.isFile()) {
            return directory.delete();
        } else {
            return false;
        }
    }
}
