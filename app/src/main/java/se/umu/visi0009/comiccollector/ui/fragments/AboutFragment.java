package se.umu.visi0009.comiccollector.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.umu.visi0009.comiccollector.R;

/**
 * Fragment that displays information about the app to the user.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class AboutFragment extends Fragment {

    private Toolbar mToolbar;

    /**
     * Called to have the fragment instantiate its user interface view. Inflates
     * the XML layout.
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
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    /**
     * Called when the fragment's activity has been created and this fragment's
     * view hierarchy is instantiated. Finds a reference to the app bar.
     *
     * @param savedInstanceState    If the fragment is being re-created from a
     *                              previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToolbar = getActivity().findViewById(R.id.toolbar);
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * Sets the app bar's title.
     */
    @Override
    public void onResume() {
        super.onResume();
        mToolbar.setTitle(R.string.toolbar_title_about);
    }
}
