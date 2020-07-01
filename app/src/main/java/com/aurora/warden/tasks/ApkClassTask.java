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

package com.aurora.warden.tasks;

import android.content.Context;
import android.net.Uri;

import com.aurora.warden.utils.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexFile;

public class ApkClassTask {

    private Context context;
    private Uri uri;

    public ApkClassTask(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    public Set<String> getAllClasses() {
        final Set<String> classNameList = new HashSet<>();

        File classFile = null;
        File optimizedFile = null;

        try {
            final InputStream inputStream = context.getContentResolver().openInputStream(uri);

            classFile = File.createTempFile("classes" + Thread.currentThread().getId(),
                    ".dex",
                    context.getCacheDir());

            optimizedFile = File.createTempFile("opt" + Thread.currentThread().getId(),
                    ".dex",
                    context.getCacheDir());

            FileUtils.copyInputStreamToFile(inputStream, classFile);

            final DexFile dexFile = DexFile.loadDex(classFile.getPath(), optimizedFile.getPath(), 0);

            for (Enumeration<String> classNames = dexFile.entries(); classNames.hasMoreElements(); ) {
                final String className = classNames.nextElement();
                if (className.length() >= 8 && className.contains("."))
                    classNameList.add(className);
            }
        } catch (Exception e) {
            Log.e(e.getMessage());
        } finally {
            if (classFile != null && optimizedFile != null) {
                optimizedFile.delete();
                classFile.delete();
            }
        }
        return classNameList;
    }
}
