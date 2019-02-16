package se.umu.visi0009.comiccollector.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.db.entities.Achievement;
import se.umu.visi0009.comiccollector.other.enums.AchievementDifficulty;
import se.umu.visi0009.comiccollector.viewmodel.AchievementDetailsViewModel;

/**
 * Fragment that displays detailed information about an achievement.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class AchievementDetailsFragment extends Fragment {

    private static final String KEY_ACHIEVEMENT_ID = "achievementID";
    private static final String TAG = "AchievementDetails";

    private TextView mTextViewDescription;
    private TextView mTextViewDifficulty;
    private TextView mTextViewStatus;

    /**
     * Called to have the fragment instantiate its user interface view. Inflates
     * the XML layout and finds references to views.
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
        View rootView = inflater.inflate(R.layout.fragment_achievement_details, container, false);

        mTextViewDescription = rootView.findViewById(R.id.achievement_description_text);
        mTextViewDifficulty = rootView.findViewById(R.id.achievement_difficulty_text);
        mTextViewStatus = rootView.findViewById(R.id.achievement_status_text);

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

        final Toolbar toolbar;

        AchievementDetailsViewModel.Factory achievementDetailsViewModelFactory;
        AchievementDetailsViewModel achievementDetailsViewModel;

        super.onActivityCreated(savedInstanceState);

        toolbar = getActivity().findViewById(R.id.toolbar);

        achievementDetailsViewModelFactory = new AchievementDetailsViewModel.Factory(getActivity().getApplication(), getArguments().getInt(KEY_ACHIEVEMENT_ID));

        achievementDetailsViewModel = ViewModelProviders.of(this, achievementDetailsViewModelFactory).get(AchievementDetailsViewModel.class);

        achievementDetailsViewModel.getAchievement().observe(this, new Observer<Achievement>() {
            @Override
            public void onChanged(@Nullable final Achievement achievement) {
                toolbar.setTitle(achievement.getName());
                mTextViewDescription.setText(achievement.getDescription());
                mTextViewDifficulty.setText(difficultyToString(achievement.getDifficulty()));
                mTextViewStatus.setText(statusToString(achievement.getDate_completed()));
            }
        });
    }

    /**
     * Static method that returns a AchievementDetailsFragment with details
     * about an achievement.
     *
     * @param achievementID     ID of achievement to show detailed information
     *                          about.
     * @return                  A AchievementDetailsFragment with details about
     *                          the achievement with the given ID.
     */
    public static AchievementDetailsFragment forItem(int achievementID) {

        AchievementDetailsFragment fragment;
        Bundle args;

        fragment = new AchievementDetailsFragment();
        args = new Bundle();

        args.putInt(KEY_ACHIEVEMENT_ID, achievementID);

        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Returns a string describing a AchievementDifficulty.
     *
     * @param achievementDifficulty     The AchievementDifficulty to describe.
     * @return                          A string describing the
     *                                  AchievementDifficulty.
     */
    private String difficultyToString(AchievementDifficulty achievementDifficulty) {
        switch(achievementDifficulty) {
            case VERY_HARD:
                return "Very hard";
            case HARD:
                return "Hard";
            case MEDIUM:
                return "Medium";
            case EASY:
                return "Easy";
            case VERY_EASY:
                return "Very easy";
            default:
                Log.e(TAG, "Error: Unclassified achievement difficulty");
                return "";
        }
    }

    /**
     * Returns a status string depending on the date parameter.
     *
     * @param date      The date to analyze.
     * @return          A string with a status.
     */
    private String statusToString(Date date) {

        SimpleDateFormat simpleDateFormat;

        if(date.equals(Achievement.DATE_INCOMPLETE)) {
            return "Incomplete";
        }

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

        return "Completed ".concat(simpleDateFormat.format(date));
    }
}
