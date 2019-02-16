package se.umu.visi0009.comiccollector.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.db.entities.Character;
import se.umu.visi0009.comiccollector.ui.click_listener_interfaces.OnCharacterClickListener;
import se.umu.visi0009.comiccollector.other.enums.SortTypes;

/**
 * Class that handles the list in CollectionFragment.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class CollectionFragmentAdapter extends RecyclerView.Adapter<CollectionFragmentAdapter.ViewHolder> {

    private final OnCharacterClickListener mOnCharacterClickListener;

    private SortTypes mSortType = SortTypes.ASCENDING;
    private List<Character> mCharacters;
    private List<Character> mCharactersFilteredSorted;
    private String mFilterPhrase = "";

    /**
     * Class that handles the information in each list item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mImageView;
        private final TextView mTextView;

        /**
         * Constructor for the class. Initializes view attributes.
         *
         * @param v     The list item's view.
         */
        public ViewHolder(View v) {
            super(v);

            mImageView = v.findViewById(R.id.collection_recycler_item_image);
            mTextView = v.findViewById(R.id.collection_recycler_item_name);
        }

        /**
         * Binds data to the view and sets an onclicklistener.
         *
         * @param character                     Character containing the data to
         *                                      be displayed.
         * @param onCharacterClickListener      Clicklistener that is used when
         *                                      the user clicks the item.
         */
        public void bind(final Character character, final OnCharacterClickListener onCharacterClickListener) {

            mImageView.setImageBitmap(character.getCharacterImage());
            mTextView.setText(character.getName());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onCharacterClickListener.onCharacterClick(character);
                }
            });
        }
    }

    /**
     * Constructor for the class.
     *
     * @param onCharacterClickListener      Clicklistener that is used when an
     *                                      item is clicked.
     */
    public CollectionFragmentAdapter(OnCharacterClickListener onCharacterClickListener) {
        mOnCharacterClickListener = onCharacterClickListener;
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
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_collection, viewGroup, false);
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
        if(mCharactersFilteredSorted != null) {
            viewHolder.bind(mCharactersFilteredSorted.get(i), mOnCharacterClickListener);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return      The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mCharactersFilteredSorted == null ? 0 : mCharactersFilteredSorted.size();
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
     * Sets the phrase to filter the list items by.
     *
     * @param filterPhrase      The phrase to filter the list items by.
     */
    public void setFilterPhrase(String filterPhrase) {
        mFilterPhrase = filterPhrase;
    }

    /**
     * Sets the adapter's data set.
     *
     * @param characters    The data set to be used.
     */
    public void setCharacters(List<Character> characters) {
        mCharacters = characters;
    }

    /**
     * Filters the data set by the current filter phrase and sorts the remaining
     * data by the current sort type.
     */
    public void updateSortedFilteredCharacters() {

        List<Character> tempList;

        tempList = new ArrayList<>();

        //Filter
        for(Character character : mCharacters) {
            if(character.getName().toLowerCase().contains(mFilterPhrase.toLowerCase())) {
                tempList.add(character);
            }
        }

        //Sort
        switch(mSortType) {
            case ASCENDING:
                Collections.sort(tempList, new Comparator<Character>() {
                    @Override
                    public int compare(Character character1, Character character2) {

                        String nameCharacter1;
                        String nameCharacter2;

                        nameCharacter1 = character1.getName().toLowerCase();
                        nameCharacter2 = character2.getName().toLowerCase();

                        return nameCharacter1.compareTo(nameCharacter2);
                    }
                });

                break;
            case DESCENDING:
                Collections.sort(tempList, new Comparator<Character>() {
                    @Override
                    public int compare(Character character1, Character character2) {

                        String nameCharacter1;
                        String nameCharacter2;

                        nameCharacter1 = character1.getName().toLowerCase();
                        nameCharacter2 = character2.getName().toLowerCase();

                        return nameCharacter2.compareTo(nameCharacter1);
                    }
                });

                break;
        }

        mCharactersFilteredSorted = tempList;
    }
}
