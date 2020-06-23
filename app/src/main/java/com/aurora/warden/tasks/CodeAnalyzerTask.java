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

import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.aurora.warden.manager.StaticDataProvider;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodeAnalyzerTask {

    private Context context;
    private Set<String> classNameList;
    private StaticDataProvider staticDataProvider;

    public CodeAnalyzerTask(Context context, Set<String> classNameList) {
        this.context = context;
        this.classNameList = classNameList;
        this.staticDataProvider = StaticDataProvider.getInstance(context);
    }

    public Map<String, Tracker> getTrackers() {
        final List<Tracker> trackerList = new ArrayList<>(staticDataProvider.getKnownTrackersList().values());
        final Map<String, Tracker> trackerSignatureMap = new HashMap<>();
        final Map<String, Tracker> matchedSignatureMap = new HashMap<>();

        for (Tracker tracker : trackerList) {
            String signature = tracker.getCodeSignature();
            if (signature.length() > 1 && !signature.equals("NC")) {
                if (signature.contains("|")) {
                    final String[] signatureArr = StringUtils.split(signature, "|");
                    for (String splitSignature : signatureArr) {
                        tracker.setCodeSignature(splitSignature);
                        trackerSignatureMap.put(splitSignature, tracker);
                    }
                } else {
                    trackerSignatureMap.put(tracker.getCodeSignature(), tracker);
                }
            }
        }

        for (String className : classNameList) {
            final String signature = isPotentialShit(className, trackerSignatureMap.keySet());
            if (StringUtils.isNotEmpty(signature)) {
                matchedSignatureMap.put(signature, trackerSignatureMap.get(signature));
            }
        }

        return matchedSignatureMap;
    }

    public Map<String, Logger> getLoggers() {
        final List<Logger> loggerList = new ArrayList<>(staticDataProvider.getKnownLoggerList().values());
        final Map<String, Logger> loggerSignatureMap = new HashMap<>();
        final Map<String, Logger> matchedSignatureMap = new HashMap<>();

        for (Logger logger : loggerList) {
            String signature = logger.getCodeSignature();

            if (signature.length() > 1) {
                if (signature.contains("|")) {
                    final String[] signatureArr = StringUtils.split(signature, "|");
                    for (String splitSignature : signatureArr) {
                        logger.setCodeSignature(splitSignature);
                        loggerSignatureMap.put(splitSignature, logger);
                    }
                } else {
                    loggerSignatureMap.put(logger.getCodeSignature(), logger);
                }
            }
        }

        for (String className : classNameList) {
            final String signature = isPotentialShit(className, loggerSignatureMap.keySet());
            if (StringUtils.isNotEmpty(signature)) {
                matchedSignatureMap.put(signature, loggerSignatureMap.get(signature));
            }
        }

        return matchedSignatureMap;
    }

    private String isPotentialShit(String className, Set<String> trackerSignature) {
        for (String signature : trackerSignature) {
            if (className.contains(signature) || className.startsWith(signature)) {
                return signature;
            }
        }
        return null;
    }
}
