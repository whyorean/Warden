package com.aurora.warden;

interface IPrivilegedCallback {
    void handleResult(in String packageName, in int returnCode);
}