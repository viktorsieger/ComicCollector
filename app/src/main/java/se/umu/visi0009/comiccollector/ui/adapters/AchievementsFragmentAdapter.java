package se.umu.visi0009.comiccollector.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.db.entities.Achievement;
import se.umu.visi0009.comiccollector.ui.click_listener_interfaces.OnAchievementClickListener;
import se.umu.visi0009.comiccollector.other.enums.SortTypes;

/**
 * Class that handles the list in AchievementsFragment.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class AchievementsFragmentAdapter extends RecyclerView.Adapter<AchievementsFragmentAdapter.ViewHolder> {

    private final OnAchievementClickListener mOnAchievementClickListener;

    private SortTypes mSortType = SortTypes.ASCENDING;
    private List<Achievement> mAchievements;
    private List<Achievement> mAchievementsSorted;

    /**
     * Class that handles the information in each list item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mTextViewName;
        private final TextView mTextViewStatus;

        /**
         * Constructor for the class. Initializes view attributes.
         *
         * @param v     The list item's view.
         */
        public ViewHolder(View v) {
            super(v);

            mTextViewName = v.findViewById(R.id.achievements_recycler_item_name);
            mTextViewStatus = v.findViewById(R.id.achievements_recycler_item_status);
        }

        /**
         * Binds data to the view and sets an onclicklistener.
         *
         * @param achievement                       Achievement containing the
         *                                          data to be displayed.
         * @param onAchievementClickListener        Clicklistener that is used
         *                                          when the user clicks the
         *                                          item.
         */
        public void bind(final Achievement achievement, final OnAchievementClickListener onAchievementClickListener) {
            mTextViewName.setText(achievement.getName());

            if(achievement.getDate_completed().equals(Achievement.DATE_INCOMPLETE)) {
                mTextViewStatus.setText(R.string.achievement_status_incomplete_string);
            }
            else {
                mTextViewStatus.setText(R.string.achievement_status_complete_string);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onAchievementClickListener.onAchievementClick(achievement);
                }
            });
        }
    }

    /**
     * Constructor for the class.
     *
     * @param onAchievementClickListener        Clicklistener that is used when
     *                                          an item is clicked.
     */
    public AchievementsFragmentAdapter(OnAchievementClickListener onAchievementClickListener) {
        mOnAchievementClickListener = onAchievementClickListener;
    }

    /**
     * Creates new views by inflating a layout from XML.
     *
     * @param viewGroup     The ViewGroup into which the new View will be added
     *                      after it is bound to an adapter position.
     * @param i             The view type of the new View.
     * @return              A new ViewHolder that holds a View of the given view
     *                      type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_achievements, viewGroup, false);
        return new ViewHolder(v);
    }

    /**
     * Populates data into an item.
     *
     * @param viewHolder        The ViewHolder which should be updated to
     *                          represent the contents of the item at the given
     *                          position in the data set.
     * @param i                 The position of the item within the adapter's
     *                          data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if(mAchievementsSorted != null) {
            viewHolder.bind(mAchievementsSorted.get(i), mOnAchievementClickListener);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return      The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mAchievementsSorted == null ? 0 : mAchievementsSorted.size();
    }

    /**
     * Changes the sorting order of the list items.
     */
    public void toogleSortType() {
        if(mSortType == SortTypes.ASCENDING) {
            mSortType = SortTypes.DESCENDING;
        }
        else {
            mSortType = SortTypes.ASCENDING;
        }
    }

    /**
     * Sets the adapter's data set.
     *
     * @param achievements      The data set to be used.
     */
    public void setAchievements(List<Achievement> achievements) {
        mAchievements = achievements;
    }

    /**
     * Sorts the data set by the current sort type.
     */
    public void updateSortedAchievements() {
        List<Achievement> tempList;

        tempList = mAchievements;

        //Sort
        switch(mSortType) {
            case ASCENDING:
                Collections.sort(tempList, new Comparator<Achievement>() {
                    @Override
                    public int compare(Achievement achievement1, Achievement achievement2) {

                        String nameAchievement1;
                        String nameAchievement2;

                        nameAchievement1 = achievement1.getName().toLowerCase();
                        nameAchievement2 = achievement2.getName().toLowerCase();

                        return nameAchievement1.compareTo(nameAchievement2);
                    }
                });

                break;
            case DESCENDING:
                Collections.sort(tempList, new Comparator<Achievement>() {
                    @Override
                    public int compare(Achievement achievement1, Achievement achievement2) {

                        String nameAchievement1;
                        String nameAchievement2;

                        nameAchievement1 = achievement1.getName().toLowerCase();
                        nameAchievement2 = achievement2.getName().toLowerCase();

                        return nameAchievement2.compareTo(nameAchievement1);
                    }
                });

                break;
        }

        mAchievementsSorted = tempList;
    }
}
