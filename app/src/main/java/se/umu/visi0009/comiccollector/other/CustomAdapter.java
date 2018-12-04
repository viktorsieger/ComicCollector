package se.umu.visi0009.comiccollector.other;

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
import se.umu.visi0009.comiccollector.enums.CollectionSortTypes;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private final OnCharacterClickListener mOnCharacterClickListener;

    private CollectionSortTypes mSortType = CollectionSortTypes.ASCENDING;
    private List<Character> mCharacters;
    private List<Character> mCharactersFilteredSorted;
    private String mFilterPhrase = "";

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mImageView;
        private final TextView mTextView;

        public ViewHolder(View v) {
            super(v);

            mImageView = v.findViewById(R.id.item_image);
            mTextView = v.findViewById(R.id.item_text);
        }

        public void bind(final Character character, final OnCharacterClickListener listener) {

            mImageView.setImageBitmap(character.getCharacterImage());
            mTextView.setText(character.getName());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onCharacterClick(character);
                }
            });
        }
    }

    public CustomAdapter(OnCharacterClickListener onCharacterClickListener) {
        mOnCharacterClickListener = onCharacterClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if(mCharactersFilteredSorted != null) {
            viewHolder.bind(mCharactersFilteredSorted.get(i), mOnCharacterClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return mCharactersFilteredSorted == null ? 0 : mCharactersFilteredSorted.size();
    }

    public void toogleSortType() {
        if(mSortType == CollectionSortTypes.ASCENDING) {
            mSortType = CollectionSortTypes.DESCENDING;
        }
        else {
            mSortType = CollectionSortTypes.ASCENDING;
        }
    }

    public void setFilterPhrase(String filterPhrase) {
        mFilterPhrase = filterPhrase;
    }

    public void setCharacters(List<Character> characters) {
        mCharacters = characters;
    }

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
