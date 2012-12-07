/* *************************************************************************
 *  Copyright 2012 The detlef developers                                   *
 *                                                                         *
 *  This program is free software: you can redistribute it and/or modify   *
 *  it under the terms of the GNU General Public License as published by   *
 *  the Free Software Foundation, either version 2 of the License, or      *
 *  (at your option) any later version.                                    *
 *                                                                         *
 *  This program is distributed in the hope that it will be useful,        *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of         *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          *
 *  GNU General Public License for more details.                           *
 *                                                                         *
 *  You should have received a copy of the GNU General Public License      *
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  *
 ************************************************************************* */



package at.ac.tuwien.detlef.download;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * DetlefDownloadManager keeps track of all active downloads and updates episode
 * information once the downloads have completed. On application shutdown, all running
 * downloads should be cancelled by calling {@link DetlefDownloadManager#cancelAll()}.
 */
public class DetlefDownloadManager {

    private static final String TAG = DetlefDownloadManager.class.getName();

    private final Map<Long, Episode> activeDownloads = new ConcurrentHashMap<Long, Episode>();
    private final Context context;
    private final EpisodeDAOImpl dao;
    private final DownloadManager downloadManager;

    public DetlefDownloadManager(Context context) {
        this.context = context;
        downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        dao = EpisodeDAOImpl.i();
    }

    /**
     * Places an episode in the system's download queue.
     * The episode's state is set to DOWNLOADING, and its path is updated.
     */
    public void enqueue(Episode episode) throws IOException {
        if (!isExternalStorageWritable()) {
            throw new IOException("Cannot write to external storage");
        }

        Podcast podcast = episode.getPodcast();
        Uri uri = Uri.parse(episode.getUrl());

        /* We may need to change our naming policy in case of duplicates. However,
         * let's ignore this for now since it's simplest for us and the user. */

        String path = String.format("%s/%s", podcast.getTitle(),
                new File(uri.toString()).getName());

        /* Ensure the directory already exists. */

        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), path);
        file.getParentFile().mkdirs();

        Request request = new Request(uri);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, path);
        request.allowScanningByMediaScanner();
        request.setTitle(episode.getTitle());
        request.setDescription(
                String.format("Downloading episode from podcast %s", podcast.getTitle()));

        long id = downloadManager.enqueue(request);
        activeDownloads.put(id, episode);

        /* Finally, update the episode's path and state in the database. */

        episode.setFilePath(file.getAbsolutePath());
        episode.setStorageState(StorageState.DOWNLOADING);

        dao.updateFilePath(episode);
        dao.updateStorageState(episode);

        Log.v(TAG, String.format("Enqueued download task %s", path));
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Cancels the download of an episode. If the specified episode is not currently being downloaded,
     * no action is taken.
     */
    public void cancel(Episode episode) {

        for (Entry<Long, Episode> entry : activeDownloads.entrySet()) {
            if (entry.getValue() != episode) {
                continue;
            }

            long id = entry.getKey();

            activeDownloads.remove(id);
            downloadManager.remove(id);

            break;
        }

        episode.setStorageState(StorageState.NOT_ON_DEVICE);
        dao.updateStorageState(episode);
    }

    /**
     * Cancels all active downloads.
     */
    public void cancelAll() {
        for (Entry<Long, Episode> entry : activeDownloads.entrySet()) {

            downloadManager.remove(entry.getKey());

            Episode episode = entry.getValue();
            episode.setStorageState(StorageState.NOT_ON_DEVICE);
            dao.updateStorageState(episode);
        }
        activeDownloads.clear();
    }

    /**
     * Called once a download has completed. Responsible for updating the internal
     * state and pushing episode changes to the database.
     * @param id The download id.
     */
    public void downloadComplete(long id) {
        Episode episode = activeDownloads.remove(id);
        if (episode == null) {
            Log.w(TAG, String.format("No active download found for id %d", id));
            return;
        }

        if (!isDownloadSuccessful(id)) {
            Log.w(TAG, String.format("Download for id %d did not complete successfully (Reason: %d)",
                    id, getDownloadFailureReason(id)));

            episode.setStorageState(StorageState.NOT_ON_DEVICE);
            dao.updateStorageState(episode);

            return;
        }

        Uri uri = downloadManager.getUriForDownloadedFile(id);
        Log.v(TAG, String.format("File %s downloaded successfully", uri.getPath()));

        /* Update the episode's state in the database. */

        episode.setStorageState(StorageState.DOWNLOADED);
        dao.updateStorageState(episode);
    }

    private boolean isDownloadSuccessful(long id) {
        int status = getDownloadQueryAsInt(id, DownloadManager.COLUMN_STATUS);
        return (DownloadManager.STATUS_SUCCESSFUL == status);
    }

    private int getDownloadFailureReason(long id) {
        return getDownloadQueryAsInt(id, DownloadManager.COLUMN_REASON);
    }

    private int getDownloadQueryAsInt(long id, String column) {
        Query query = new Query();
        query.setFilterById(id);

        Cursor c = downloadManager.query(query);
        if (!c.moveToFirst()) {
            return -1;
        }

        int columnIndex = c.getColumnIndex(column);
        return c.getInt(columnIndex);
    }

}
