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



package at.ac.tuwien.detlef.db;

import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Episode.StorageState;
import at.ac.tuwien.detlef.domain.Podcast;

/**
 * tests the episodeDAOImpl
 * 
 * @author Lacky
 */
public class PlaylistDAOImplTest extends AndroidTestCase {

    Episode e0, e1, e2;

    Podcast p1;

    @Override
    protected void setUp() throws Exception {
        p1 = new Podcast();
        p1.setDescription("description");
        p1.setLastUpdate(111);
        p1.setLogoFilePath("logo file path");
        p1.setLogoUrl("logo url");
        p1.setTitle("title");
        p1.setUrl("die url halt");

        e0 = new Episode(p1);
        e0.setAuthor("author");
        e0.setDescription("description");
        e0.setFileSize(0);
        e0.setGuid("guid");
        e0.setLink("link");
        e0.setMimetype("mimetype");
        e0.setReleased(System.currentTimeMillis());
        e0.setTitle("title");
        e0.setUrl("url");
        e0.setStorageState(StorageState.NOT_ON_DEVICE);
        e0.setFilePath("path");

        e1 = new Episode(p1);
        e1.setAuthor("author");
        e1.setDescription("description");
        e1.setFileSize(0);
        e1.setGuid("guid");
        e1.setLink("link");
        e1.setMimetype("mimetype");
        e1.setReleased(System.currentTimeMillis());
        e1.setTitle("title");
        e1.setUrl("url");
        e1.setStorageState(StorageState.NOT_ON_DEVICE);
        e1.setFilePath("path");

        e2 = new Episode(p1);
        e2.setAuthor("author");
        e2.setDescription("description");
        e2.setFileSize(0);
        e2.setGuid("guid");
        e2.setLink("link");
        e2.setMimetype("mimetype");
        e2.setReleased(System.currentTimeMillis());
        e2.setTitle("title");
        e2.setUrl("url");
        e2.setStorageState(StorageState.NOT_ON_DEVICE);
        e2.setFilePath("path");

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void clearDatabase() {
        DatabaseHelper dbHelper = new DatabaseHelper(this.mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_PLAYLIST, null, null);
        db.close();
    }

    public void testAddToBeginning() {
        clearDatabase();

        EpisodeDAOImpl edao = EpisodeDAOImpl.i();
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);
        e0 = edao.insertEpisode(e0);
        e1 = edao.insertEpisode(e1);

        PlaylistDAOImpl ldao = PlaylistDAOImpl.i();
        ldao.addEpisodeToBeginningOfPlaylist(e0);
        assertTrue(ldao.checkNoGaps());
        List<Episode> playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 1);
        assertTrue(playlist.get(0) == e0);
        ldao.addEpisodeToBeginningOfPlaylist(e1);
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 2);
        assertTrue(playlist.get(0) == e1);
        assertTrue(playlist.get(1) == e0);
        ldao.addEpisodeToBeginningOfPlaylist(e1);
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 3);
        assertTrue(playlist.get(0) == e1);
        assertTrue(playlist.get(1) == e1);
        assertTrue(playlist.get(2) == e0);
    }

    public void testAddToEnd() {
        clearDatabase();

        EpisodeDAOImpl edao = EpisodeDAOImpl.i();
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);
        e0 = edao.insertEpisode(e0);
        e1 = edao.insertEpisode(e1);

        PlaylistDAOImpl ldao = PlaylistDAOImpl.i();
        ldao.addEpisodeToEndOfPlaylist(e0);
        assertTrue(ldao.checkNoGaps());
        List<Episode> playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 1);
        assertTrue(playlist.get(0) == e0);
        ldao.addEpisodeToEndOfPlaylist(e1);
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 2);
        assertTrue(playlist.get(0) == e0);
        assertTrue(playlist.get(1) == e1);
        ldao.addEpisodeToEndOfPlaylist(e1);
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 3);
        assertTrue(playlist.get(0) == e0);
        assertTrue(playlist.get(1) == e1);
        assertTrue(playlist.get(2) == e1);
    }

    public void testRemove() {
        clearDatabase();

        EpisodeDAOImpl edao = EpisodeDAOImpl.i();
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);
        e0 = edao.insertEpisode(e0);
        e1 = edao.insertEpisode(e1);
        e2 = edao.insertEpisode(e2);

        PlaylistDAOImpl ldao = PlaylistDAOImpl.i();
        ldao.addEpisodeToEndOfPlaylist(e0);
        ldao.addEpisodeToEndOfPlaylist(e1);
        ldao.addEpisodeToEndOfPlaylist(e2);
        List<Episode> playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 3);
        assertTrue(playlist.get(0) == e0);
        assertTrue(playlist.get(1) == e1);
        assertTrue(playlist.get(2) == e2);
        assertTrue(ldao.removeEpisode(1));
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 2);
        assertTrue(playlist.get(0) == e0);
        assertTrue(playlist.get(1) == e2);
        assertTrue(ldao.removeEpisode(1));
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 1);
        assertTrue(playlist.get(0) == e0);
        assertTrue(ldao.removeEpisode(0));
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 0);
    }

    public void testMove() {
        clearDatabase();

        EpisodeDAOImpl edao = EpisodeDAOImpl.i();
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);
        e0 = edao.insertEpisode(e0);
        e1 = edao.insertEpisode(e1);
        e2 = edao.insertEpisode(e2);

        PlaylistDAOImpl ldao = PlaylistDAOImpl.i();
        ldao.addEpisodeToEndOfPlaylist(e0);
        ldao.addEpisodeToEndOfPlaylist(e1);
        ldao.addEpisodeToEndOfPlaylist(e2);
        List<Episode> playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 3);
        assertTrue(playlist.get(0) == e0);
        assertTrue(playlist.get(1) == e1);
        assertTrue(playlist.get(2) == e2);
        assertTrue(ldao.moveEpisode(0, 2));
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 3);
        assertTrue(playlist.get(0) == e1);
        assertTrue(playlist.get(1) == e2);
        assertTrue(playlist.get(2) == e0);
        assertTrue(ldao.moveEpisode(2, 0));
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 3);
        assertTrue(playlist.get(0) == e0);
        assertTrue(playlist.get(1) == e1);
        assertTrue(playlist.get(2) == e2);
        assertTrue(ldao.moveEpisode(1, 2));
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 3);
        assertTrue(playlist.get(0) == e0);
        assertTrue(playlist.get(1) == e2);
        assertTrue(playlist.get(2) == e1);
    }

    public void testBackgroundDelete() {
        clearDatabase();

        EpisodeDAOImpl edao = EpisodeDAOImpl.i();
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);
        e0 = edao.insertEpisode(e0);
        e1 = edao.insertEpisode(e1);
        e2 = edao.insertEpisode(e2);

        PlaylistDAOImpl ldao = PlaylistDAOImpl.i();
        ldao.addEpisodeToEndOfPlaylist(e0);
        assertTrue(ldao.checkNoGaps());
        ldao.addEpisodeToEndOfPlaylist(e1);
        assertTrue(ldao.checkNoGaps());
        ldao.addEpisodeToEndOfPlaylist(e2);
        assertTrue(ldao.checkNoGaps());
        List<Episode> playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 3);
        assertTrue(playlist.get(0) == e0);
        assertTrue(playlist.get(1) == e1);
        assertTrue(playlist.get(2) == e2);
        edao.deleteEpisode(e1);
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 2);
        assertTrue(playlist.get(0) == e0);
        assertTrue(playlist.get(1) == e2);
        edao.deleteEpisode(e0);
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 1);
        assertTrue(playlist.get(0) == e2);
        edao.deleteEpisode(e2);
        assertTrue(ldao.checkNoGaps());
        playlist = ldao.getNonCachedEpisodes();
        assertTrue(playlist.size() == 0);
    }

    public void testClear() {
        clearDatabase();

        EpisodeDAOImpl edao = EpisodeDAOImpl.i();
        PodcastDAOImpl pdao = PodcastDAOImpl.i();
        p1 = pdao.insertPodcast(p1);
        e0 = edao.insertEpisode(e0);
        e1 = edao.insertEpisode(e1);
        e2 = edao.insertEpisode(e2);

        PlaylistDAOImpl ldao = PlaylistDAOImpl.i();
        ldao.addEpisodeToEndOfPlaylist(e0);
        assertTrue(ldao.checkNoGaps());
        ldao.addEpisodeToEndOfPlaylist(e1);
        assertTrue(ldao.checkNoGaps());
        ldao.addEpisodeToEndOfPlaylist(e2);
        assertTrue(ldao.checkNoGaps());
        assertTrue(ldao.getNonCachedEpisodes().size() == 3);
        ldao.clearPlaylist();
        assertTrue(ldao.checkNoGaps());
        assertTrue(ldao.getNonCachedEpisodes().size() == 0);
    }
}
