package se.umu.visi0009.comiccollector.fragments;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.activities.MainActivity;
import se.umu.visi0009.comiccollector.db.entities.Character;
import se.umu.visi0009.comiccollector.other.OnCharacterClickListener;
import se.umu.visi0009.comiccollector.other.CollectionViewModel;
import se.umu.visi0009.comiccollector.other.CustomAdapter;

public class CollectionFragment extends Fragment {

    private CollectionViewModel mCollectionViewModel;
    private Toolbar mToolbar;
    protected CustomAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;

    private final OnCharacterClickListener mOnCharacterClickListener = new OnCharacterClickListener() {
        @Override
        public void onCharacterClick(Character character) {
            if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
                ((MainActivity)getActivity()).show(character);
            }
        }
    };

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_collection, container, false);

        mRecyclerView = rootView.findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new CustomAdapter(mOnCharacterClickListener);
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mToolbar = getActivity().findViewById(R.id.toolbar);

        mToolbar.setTitle("Collection");

        mCollectionViewModel = ViewModelProviders.of(this).get(CollectionViewModel.class);

        mCollectionViewModel.getCharacters().observe(this, new Observer<List<Character>>() {
            @Override
            public void onChanged(@Nullable List<Character> characters) {
                mAdapter.setCharacters(characters);
                mAdapter.updateSortedFilteredCharacters();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mToolbar.inflateMenu(R.menu.toolbar_collectionfragment);
        setupActionItemSearch();
    }

    @Override
    public void onPause() {
        super.onPause();
        mToolbar.getMenu().clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        if(menuItem.getItemId() == R.id.toolbar_sort) {
            mAdapter.toogleSortType();
            mAdapter.updateSortedFilteredCharacters();
            mAdapter.notifyDataSetChanged();
        }

        return super.onOptionsItemSelected(menuItem);
    }

    private void setupActionItemSearch() {
        SearchManager searchManager;
        SearchView searchView;

        searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView)mToolbar.getMenu().findItem(R.id.toolbar_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnQueryTextListener(mOnQueryTextListener);
    }
}
