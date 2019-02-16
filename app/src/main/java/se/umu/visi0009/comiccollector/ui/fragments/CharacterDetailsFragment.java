package se.umu.visi0009.comiccollector.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.db.entities.Card;
import se.umu.visi0009.comiccollector.db.entities.Character;
import se.umu.visi0009.comiccollector.viewmodel.CharacterViewModel;

/**
 * Fragment that displays detailed information about a character.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class CharacterDetailsFragment extends Fragment {

    private static final String KEY_CHARACTER_ID = "characterID";

    private AppCompatImageView mImageView;
    private TextView mTextViewDescription;
    private TextView mTextViewAppearances;
    private TextView mTextViewCardConditions;
    private TextView mTextViewFirstFound;
    private TextView mTextViewMarvelBanner;

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
        View rootView = inflater.inflate(R.layout.fragment_character_details, container, false);

        mImageView = rootView.findViewById(R.id.character_image);
        mTextViewDescription = rootView.findViewById(R.id.character_description_text);
        mTextViewAppearances = rootView.findViewById(R.id.character_appearances_text);
        mTextViewCardConditions = rootView.findViewById(R.id.character_card_conditions_text);
        mTextViewFirstFound = rootView.findViewById(R.id.character_first_found_text);
        mTextViewMarvelBanner = rootView.findViewById(R.id.character_details_marvel_banner);

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

        CharacterViewModel.Factory characterViewModelFactory;
        CharacterViewModel characterViewModel;

        super.onActivityCreated(savedInstanceState);

        toolbar = getActivity().findViewById(R.id.toolbar);

        characterViewModelFactory = new CharacterViewModel.Factory(getActivity().getApplication(), getArguments().getInt(KEY_CHARACTER_ID));

        characterViewModel = ViewModelProviders.of(this, characterViewModelFactory).get(CharacterViewModel.class);

        characterViewModel.getCharacter().observe(this, new Observer<Character>() {
            @Override
            public void onChanged(@Nullable final Character character) {
                toolbar.setTitle(character.getName());
                mImageView.setImageBitmap(character.getCharacterImage());
                mTextViewDescription.setText(character.getDescription());
                mTextViewAppearances.setText(comicsToString(character.getComics()));
                mTextViewMarvelBanner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent;

                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(character.getUrlWiki()));

                        startActivity(intent);
                    }
                });
            }
        });

        characterViewModel.getCards().observe(this, new Observer<List<Card>>() {
            @Override
            public void onChanged(@Nullable List<Card> cards) {
                mTextViewCardConditions.setText(cardsToString(cards));
                mTextViewFirstFound.setText(firstFound(cards));
            }
        });
    }

    /**
     * Static method that returns a CharacterDetailsFragment with details about
     * a character.
     *
     * @param characterID   ID of character to show detailed information about.
     * @return              A CharacterDetailsFragment with details about the
     *                      character with the given ID.
     */
    public static CharacterDetailsFragment forItem(int characterID) {
        Bundle args;
        CharacterDetailsFragment fragment;

        fragment = new CharacterDetailsFragment();
        args = new Bundle();

        args.putInt(KEY_CHARACTER_ID, characterID);

        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Returns a string containing the comics formatted similar to a list.
     *
     * @param comics    The comics to be formatted.
     * @return          A string containing the comics with a linebreak between
     *                  every comic.
     */
    private String comicsToString(ArrayList<String> comics) {

        int i;
        String returnString;

        returnString = "";

        for(i = 0; i < comics.size(); i++) {
            returnString = returnString.concat(comics.get(i));

            if(i != (comics.size() - 1)) {
                returnString = returnString.concat("\n");
            }
        }

        return returnString;
    }

    /**
     * Returns a string containing the cards' conditions formatted similar to a
     * list.
     *
     * @param cards     The cards with the card conditions.
     * @return          A string containing the cards' conditions with a
     *                  linebreak between every condition.
     */
    private String cardsToString(List<Card> cards) {

        int i;
        String returnString, tempConditionString;

        returnString = "";
        tempConditionString = "";

        for(i = 0; i < cards.size(); i++) {
            switch(cards.get(i).getCondition()) {
                case PRISTINE:
                    tempConditionString = "Pristine";
                    break;
                case MINT_CONDITION:
                    tempConditionString = "Mint";
                    break;
                case NEAR_MINT_SLASH_MINT:
                    tempConditionString = "Near mint / Mint";
                    break;
                case NEAR_MINT:
                    tempConditionString = "Near mint";
                    break;
                case EXCELLENT_SLASH_NEAR_MINT:
                    tempConditionString = "Excellent / Near mint";
                    break;
                case EXCELLENT:
                    tempConditionString = "Excellent";
                    break;
                case VERY_GOOD_SLASH_EXCELLENT:
                    tempConditionString = "Very good / Excellent";
                    break;
                case VERY_GOOD:
                    tempConditionString = "Very good";
                    break;
                case GOOD:
                    tempConditionString = "Good";
                    break;
                case POOR:
                    tempConditionString = "Poor";
                    break;
            }

            returnString = returnString.concat(tempConditionString);

            if(i != (cards.size() - 1)) {
                returnString = returnString.concat("\n");
            }
        }

        return returnString;
    }

    /**
     * Returns a string with the date of the first found card.
     *
     * @param cards     The cards to determine which was first found.
     * @return          A string with a date of the first found card.
     */
    private String firstFound(List<Card> cards) {

        Date returnDate;
        SimpleDateFormat simpleDateFormat;

        returnDate = new Date();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

        for(Card card : cards) {
            if(card.getDateFound().before(returnDate)) {
                returnDate = card.getDateFound();
            }
        }

        return simpleDateFormat.format(returnDate);
    }
}
