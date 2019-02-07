/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.build.gradle.internal.dsl;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

/**
 * DSL object for per-variant NDK settings, such as the ABI filter.
 */
public class NdkOptions implements CoreNdkOptions, Serializable {
    private static final long serialVersionUID = 1L;

    private String moduleName;
    private String cFlags;
    private List<String> ldLibs;
    private Set<String> abiFilters;
    private String stl;
    private Integer jobs;

    public NdkOptions() {
    }

    public void _initWith(@NonNull CoreNdkOptions ndkConfig) {
        moduleName = ndkConfig.getModuleName();
        cFlags = ndkConfig.getcFlags();
        setLdLibs(ndkConfig.getLdLibs());
        setAbiFilters(ndkConfig.getAbiFilters());
    }

    @Override
    @Input @Optional
    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    @Input @Optional
    public String getcFlags() {
        return cFlags;
    }

    public void setcFlags(String cFlags) {
        this.cFlags = cFlags;
    }

    @Override
    @Input @Optional
    public List<String> getLdLibs() {
        return ldLibs;
    }

    @NonNull
    public NdkOptions ldLibs(String lib) {
        if (ldLibs == null) {
