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
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;

import com.aurora.warden.data.model.Logger;
import com.aurora.warden.data.model.Tracker;
import com.aurora.warden.manager.StaticDataProvider;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ComponentAnalyzerTask {

    private Context context;
    private Set<String> classNames;
    private PackageInfo packageInfo;
    private StaticDataProvider staticDataProvider;

    public ComponentAnalyzerTask(Context context, Set<String> classNames, PackageInfo packageInfo) {
        this.context = context;
        this.classNames = classNames;
        this.packageInfo = packageInfo;
        this.staticDataProvider = StaticDataProvider.getInstance(context);
    }

    public Bundle getTrackerComponentBundle() {

        final List<Tracker> trackerList = new ArrayList<>(staticDataProvider.getKnownTrackersList().values());
        final Map<String, Tracker> trackerSignatureMap = new HashMap<>();
        final Map<String, Tracker> matchedSignatureMap = new HashMap<>();
        final Set<ComponentInfo> componentList = new HashSet<>();

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

        for (String className : classNames) {
            final String signature = isPotentialTrackerOrLogger(className, trackerSignatureMap.keySet());
            if (StringUtils.isNotEmpty(signature)) {
                matchedSignatureMap.put(signature, trackerSignatureMap.get(signature));
            }
        }

        for (ComponentInfo componentName : getComponentNames()) {
            final String signature = isPotentialComponent(componentName.name, matchedSignatureMap.keySet());
            if (StringUtils.isNotEmpty(signature)) {
                componentList.add(componentName);
            }
        }

        return new Bundle
                .BundleBuilder()
                .components(componentList)
                .trackerSet(new HashSet<>(matchedSignatureMap.values()))
                .loggerSet(new HashSet<>())
                .build();
    }

    public Bundle getLoggerComponentBundle() {
        final List<Logger> loggerList = new ArrayList<>(staticDataProvider.getKnownLoggerList().values());
        final Map<String, Logger> loggerSignatureMap = new HashMap<>();
        final Map<String, Logger> matchedSignatureMap = new HashMap<>();
        final Set<ComponentInfo> componentList = new HashSet<>();

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

        for (String className : classNames) {
            final String signature = isPotentialTrackerOrLogger(className, loggerSignatureMap.keySet());
            if (StringUtils.isNotEmpty(signature)) {
                matchedSignatureMap.put(signature, loggerSignatureMap.get(signature));
            }
        }


        for (ComponentInfo componentName : getComponentNames()) {
            final String signature = isPotentialComponent(componentName.name, matchedSignatureMap.keySet());
            if (StringUtils.isNotEmpty(signature)) {
                componentList.add(componentName);
            }
        }

        return new Bundle
                .BundleBuilder()
                .components(componentList)
                .trackerSet(new HashSet<>())
                .loggerSet(new HashSet<>(matchedSignatureMap.values()))
                .build();
    }

    private List<ComponentInfo> getComponentNames() {
        final List<ComponentInfo> componentList = new ArrayList<>();
        if (packageInfo.activities != null)
            componentList.addAll(Arrays.asList(packageInfo.activities));
        if (packageInfo.services != null)
            componentList.addAll(Arrays.asList(packageInfo.services));
        if (packageInfo.receivers != null)
            componentList.addAll(Arrays.asList(packageInfo.receivers));
        return componentList;
    }

    private String isPotentialTrackerOrLogger(String className, Set<String> trackerSignature) {
        for (String signature : trackerSignature) {
            if (className.contains(signature) || className.startsWith(signature)) {
                return signature;
            }
        }
        return null;
    }

    private String isPotentialComponent(String componentName, Set<String> trackerSignature) {
        for (String signature : trackerSignature) {
            if (componentName.contains(signature) || componentName.startsWith(signature)) {
                return componentName;
            }
        }
        return null;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Bundle {
        private Set<ComponentInfo> components;
        private Set<Tracker> trackerSet;
        private Set<Logger> loggerSet;
    }
}
