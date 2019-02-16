package se.umu.visi0009.comiccollector.ui.fragments;

import android.app.SearchManager;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.ui.activities.MainActivity;
import se.umu.visi0009.comiccollector.db.entities.Character;
import se.umu.visi0009.comiccollector.ui.click_listener_interfaces.OnCharacterClickListener;
import se.umu.visi0009.comiccollector.viewmodel.CollectionViewModel;
import se.umu.visi0009.comiccollector.ui.adapters.CollectionFragmentAdapter;

/**
 * Fragment that displays a list of characters.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class CollectionFragment extends Fragment {

    private Toolbar mToolbar;
    protected CollectionFragmentAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;

    /**
     * Onclicklistener that is used when a character in the list is selected.
     * Calls a method that displays detailed information about the selected
     * character.
     */
    private final OnCharacterClickListener mOnCharacterClickListener = new OnCharacterClickListener() {
        @Override
        public void onCharacterClick(Character character) {
            if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                ((MainActivity)getActivity()).show(character);
            }
        }
    };

    /**
     * Listener that listens for changes in the app bar's search field. The list
     * of characters is filtered by the submitted search phrase.
     */
    private SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            mAdapter.setFilterPhrase(s);
            mAdapter.updateSortedFilteredCharacters();
            mAdapter.notifyDataSetChanged();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            mAdapter.setFilterPhrase(s);
            mAdapter.updateSortedFilteredCharacters();
            mAdapter.notifyDataSetChanged();
            return false;
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
        View rootView = inflater.inflate(R.layout.fragment_collection, container, false);

        mRecyclerView = rootView.findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CollectionFragmentAdapter(mOnCharacterClickListener);
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

        CollectionViewModel collectionViewModel;

        super.onActivityCreated(savedInstanceState);

        mToolbar = getActivity().findViewById(R.id.toolbar);

        collectionViewModel = ViewModelProviders.of(this).get(CollectionViewModel.class);

        collectionViewModel.getCharacters().observe(this, new Observer<List<Character>>() {
            @Override
            public void onChanged(@Nullable List<Character> characters) {
                mAdapter.setCharacters(characters);
                mAdapter.updateSortedFilteredCharacters();
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
        mToolbar.setTitle(R.string.toolbar_title_collection);
        mToolbar.inflateMenu(R.menu.toolbar_collectionfragment);
        setupActionItemSearch();
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

        if(menuItem.getItemId() == R.id.toolbar_collection_sort) {
            mAdapter.toogleSortType();
            mAdapter.updateSortedFilteredCharacters();
            mAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Sets up the search functionality in the app bar.
     */
    private void setupActionItemSearch() {
        SearchManager searchManager;
        SearchView searchView;

        searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView)mToolbar.getMenu().findItem(R.id.toolbar_collection_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(mOnQueryTextListener);
    }
}
