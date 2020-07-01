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

import java.io.File;

public class FileUtil {

    public static String getFileName(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
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
