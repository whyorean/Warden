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
import android.os.Environment;

import com.aurora.warden.data.model.DebloatProfile;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DebloatProfileTask {

    private Context context;
    private Gson gson;

    public DebloatProfileTask(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    public Set<DebloatProfile> getAllProfiles() throws Exception {
        try {
            final Set<DebloatProfile> debloatProfiles = new HashSet<>();
            final File baseDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/Warden/Profiles/");
            if (baseDirectory.exists() && baseDirectory.isDirectory()) {
                for (File file : Objects.requireNonNull(baseDirectory.listFiles())) {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    final byte[] bytes = new byte[fileInputStream.available()];

                    fileInputStream.read(bytes);
                    fileInputStream.close();

                    final String json = new String(bytes, StandardCharsets.UTF_8);
                    final DebloatProfile debloatProfile = gson.fromJson(json, DebloatProfile.class);
                    debloatProfiles.add(debloatProfile);
                }
            }
            return debloatProfiles;
        } catch (Exception e) {
            throw new Exception("No profiles found");
        }
    }
}
