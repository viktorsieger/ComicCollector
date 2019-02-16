package se.umu.visi0009.comiccollector.ui.fragments;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.db.entities.Achievement;
import se.umu.visi0009.comiccollector.ui.activities.MainActivity;
import se.umu.visi0009.comiccollector.viewmodel.AchievementsViewModel;
import se.umu.visi0009.comiccollector.ui.adapters.AchievementsFragmentAdapter;
import se.umu.visi0009.comiccollector.ui.click_listener_interfaces.OnAchievementClickListener;

/**
 * Fragment that displays a list of achievements.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class AchievementsFragment extends Fragment {

    private Toolbar mToolbar;
    protected AchievementsFragmentAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;

    /**
     * Onclicklistener that is used when an achievement in the list is selected.
     * Calls a method that displays detailed information about the selected
     * achievement.
     */
    private final OnAchievementClickListener mOnAchievementClickListener = new OnAchievementClickListener() {
        @Override
        public void onAchievementClick(Achievement achievement) {
            if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                ((MainActivity)getActivity()).show(achievement);
            }
        }
    };

    /**
     * Called to have the fragment instantiate its user interface view. Inflates
     * the XML layout. Also sets upp the list.
     *
     * @param inflater              LayoutInflater object that can be used to
     *                              inflate any views in the fragment.
     * @param container             If non-null, this is the parent view that
     *                              the fragment's UI should be attached to. The
     *                              fragment should not add the view itself, but
     *                              this can be used to generate the
     *                              LayoutParams of the view.
     * @param savedInstanceState    If non-null, this fragment is being
     *                              re-constructed from a previous saved state
     *                              as given here.
     * @return                      Return the View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_achievements, container, false);

        mRecyclerView = rootView.findViewById(R.id.recycler_view2);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new AchievementsFragmentAdapter(mOnAchievementClickListener);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    /**
     * Called when the fragment's activity has been created and this fragment's
     * view hierarchy is instantiated. Finds a reference to the app bar. Also
     * creates the view model and fills the layout with live data from the view
     * model.
     *
     * @param savedInstanceState    If the fragment is being re-created from a
     *                              previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        AchievementsViewModel achievementsViewModel;

        super.onActivityCreated(savedInstanceState);

        mToolbar = getActivity().findViewById(R.id.toolbar);

        achievementsViewModel = ViewModelProviders.of(this).get(AchievementsViewModel.class);

        achievementsViewModel.getAchievements().observe(this, new Observer<List<Achievement>>() {
            @Override
            public void onChanged(@Nullable List<Achievement> achievements) {
                mAdapter.setAchievements(achievements);
                mAdapter.updateSortedAchievements();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * Sets the app bar's title and inflates the app bar's menu.
     */
    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setTitle(R.string.toolbar_title_achievements);
        mToolbar.inflateMenu(R.menu.toolbar_achievementsfragment);
    }

    /**
     * Called when the Fragment is no longer resumed. Clears the app bar's menu.
     */
    @Override
    public void onPause() {
        super.onPause();
        mToolbar.getMenu().clear();
    }

    /**
     * Handles interaction with the app bar's menu. If the sort item was
     * selected the list's sorting order is changed.
     *
     * @param menuItem      The menu item that was selected.
     * @return              Return false to allow normal menu processing to
     *                      proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if(menuItem.getItemId() == R.id.toolbar_achievements_sort) {
            mAdapter.toogleSortType();
            mAdapter.updateSortedAchievements();
            mAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(menuItem);
    }
}
