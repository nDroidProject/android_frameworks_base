/*
 * Copyright (C) 2009 The Android Open Source Project
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

package android.backup;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;

public class FileBackupHelper {
    /**
     * Based on oldSnapshot, determine which of the files from the application's data directory
     * need to be backed up, write them to the data stream, and fill in newSnapshot with the
     * state as it exists now.
     */
    public static void performBackup(Context context,
            ParcelFileDescriptor oldSnapshot, ParcelFileDescriptor newSnapshot,
            BackupDataOutput data, String[] files) {
        String basePath = context.getFilesDir().getAbsolutePath();
        performBackup_checked(basePath, oldSnapshot, newSnapshot, data, files);
    }

    /**
     * Check the parameters so the native code doens't have to throw all the exceptions
     * since it's easier to do that from java.
     */
    static void performBackup_checked(String basePath,
            ParcelFileDescriptor oldSnapshot, ParcelFileDescriptor newSnapshot,
            BackupDataOutput data, String[] files) {
        if (newSnapshot == null) {
            throw new NullPointerException("newSnapshot==null");
        }
        if (data == null) {
            throw new NullPointerException("data==null");
        }
        if (data.fd == null) {
            throw new NullPointerException("data.fd==null");
        }
        if (files == null) {
            throw new NullPointerException("files==null");
        }

        int err = performBackup_native(basePath, oldSnapshot.getFileDescriptor(),
                newSnapshot.getFileDescriptor(), data.fd, files);

        if (err != 0) {
            throw new RuntimeException("Backup failed"); // TODO: more here
        }
    }

    native private static int performBackup_native(String basePath,
            FileDescriptor oldSnapshot, FileDescriptor newSnapshot,
            FileDescriptor data, String[] files);
}