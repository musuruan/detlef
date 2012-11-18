package at.ac.tuwien.detlef.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import at.ac.tuwien.detlef.R;
import at.ac.tuwien.detlef.adapters.EpisodeListAdapter;
import at.ac.tuwien.detlef.db.EpisodeDAOImpl;
import at.ac.tuwien.detlef.domain.Episode;
import at.ac.tuwien.detlef.domain.Podcast;
import at.ac.tuwien.detlef.models.EpisodeListModel;

public class EpisodeListFragment extends ListFragment
implements EpisodeDAOImpl.OnEpisodeChangeListener {

    private EpisodeListModel model;
    private EpisodeListAdapter adapter;
    private Podcast filteredByPodcast = null;
    private OnEpisodeSelectedListener listener;

    /**
     * The parent activity must implement this interface in order to interact
     * with this fragment. The listener is called whenever an episode is
     * clicked.
     */
    public interface OnEpisodeSelectedListener {
        void onEpisodeSelected(Episode episode);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnEpisodeSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(String.format("%s must implement %s",
                    activity.toString(),
                    OnEpisodeSelectedListener.class.getName()));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EpisodeDAOImpl dao = EpisodeDAOImpl.i(getActivity());
        dao.addEpisodeChangedListener(this);

        List<Episode> eplist = dao.getAllEpisodes();
        model = new EpisodeListModel(eplist);

        adapter = new EpisodeListAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                new ArrayList<Episode>(eplist));
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.episode_fragment_layout, container,
                false);
    }

    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        registerForContextMenu(getListView());
    }

    @Override
    public void onDestroy() {
        EpisodeDAOImpl dao = EpisodeDAOImpl.i(this.getActivity());
        dao.removeEpisodeChangedListener(this);

        super.onDestroy();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Episode episode = (Episode) v.getTag();
        listener.onEpisodeSelected(episode);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.episode_context, menu);
    }

    /**
     * Called whenever a podcast is clicked in the PodListFragment. Filters the
     * episode list to display only episodes belonging to the specified podcast.
     * If podcast is null, all episodes are shown.
     */
    public void setPodcast(Podcast podcast) {
        filteredByPodcast = podcast;
        filterByPodcast();
    }

    private void filterByPodcast() {
        adapter.clear();
        if (filteredByPodcast == null) {
            adapter.addAll(model.getAll());
        } else {
            adapter.addAll(model.getByPodcast(filteredByPodcast));
        }
    }

    @Override
    public void onEpisodeChanged(Episode episode) {
        updateEpisodeList();
    }

    @Override
    public void onEpisodeAdded(Episode episode) {
        model.addEpisode(episode);
        filterByPodcast();
    }

    @Override
    public void onEpisodeDeleted(Episode episode) {
        model.removeEpisode(episode);
        filterByPodcast();
    }

    /**
     * Updates the displayed list based on the current model contents.
     * Ensures that UI methods are called on the UI thread.
     */
    private void updateEpisodeList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }
}
