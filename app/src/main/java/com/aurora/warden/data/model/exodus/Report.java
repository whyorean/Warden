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

package com.aurora.warden.data.model.exodus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lombok.Data;

@Data
public class Report {
    @SerializedName("downloads")
    @Expose
    private String downloads;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("creation_date")
    @Expose
    private String creationDate;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("version_code")
    @Expose
    private String versionCode;
    @SerializedName("trackers")
    @Expose
    private List<Integer> trackers = null;

    public Long getFormattedCreationDate() {
        try {
            DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            return simpleDateFormat.parse(creationDate).getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }
}
