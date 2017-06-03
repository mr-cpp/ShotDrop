package com.shotdrop.dropbox;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxUploader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Async task to upload a file to a directory
 */
public class UploadFileTask extends AsyncTask<String, Void, SharedLinkMetadata> {

    public final int notificationId;

    private final DbxClientV2 dbxClient;

    private final Callback callback;

    private Exception exception;

    public interface Callback {
        void onUploadComplete(SharedLinkMetadata result);
        void onError(@Nullable Exception e);
    }

    public UploadFileTask(int notificationId, DbxClientV2 dbxClient, Callback callback) {
        this.notificationId = notificationId;
        this.dbxClient = dbxClient;
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(SharedLinkMetadata result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else if (result == null) {
            callback.onError(null);
        } else {
            callback.onUploadComplete(result);
        }
    }

    @Override
    protected SharedLinkMetadata doInBackground(String... values) {
        try (InputStream inputStream = new FileInputStream(new File(values[0] + values[1]))) {
            dbxClient.files()
                    .uploadBuilder(File.separator + values[1])
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
            return dbxClient.sharing()
                    .createSharedLinkWithSettings(File.separator + values[1]);
        } catch (DbxException | IOException e) {
            exception = e;
        }
        return null;
    }
}
