# NonBooleanUtils

![Build Status](https://jenkins.sse.uni-hildesheim.de/buildStatus/icon?job=KernelHaven_NonBooleanUtils)

A utility plugin for [KernelHaven](https://github.com/KernelHaven/KernelHaven).

Utilities for for replacing non-boolean conditions in source code files.

## Usage

Place [`NonBooleanUtils.jar`](https://jenkins.sse.uni-hildesheim.de/view/KernelHaven/job/KernelHaven_NonBooleanUtils/lastSuccessfulBuild/artifact/build/jar/NonBooleanUtils.jar) in the plugins folder of KernelHaven.

To use this preparation, set `preparation.class.0` to `net.ssehub.kernel_haven.non_boolean.NonBooleanPreperation` in the KernelHaven properties.

## Dependencies

This plugin has no additional dependencies other than KernelHaven.
