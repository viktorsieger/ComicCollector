package se.umu.visi0009.comiccollector.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import se.umu.visi0009.comiccollector.ComicCollectorApp;
import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.db.entities.Achievement;
import se.umu.visi0009.comiccollector.db.entities.Card;
import se.umu.visi0009.comiccollector.db.entities.Character;
import se.umu.visi0009.comiccollector.db.entities.Player;
import se.umu.visi0009.comiccollector.other.enums.CardCondition;
import se.umu.visi0009.comiccollector.ui.fragments.AboutFragment;
import se.umu.visi0009.comiccollector.ui.fragments.AchievementDetailsFragment;
import se.umu.visi0009.comiccollector.ui.fragments.AchievementsFragment;
import se.umu.visi0009.comiccollector.ui.fragments.CollectionFragment;
import se.umu.visi0009.comiccollector.ui.fragments.CharacterDetailsFragment;
import se.umu.visi0009.comiccollector.ui.fragments.MapFragment;
import se.umu.visi0009.comiccollector.other.CharacterCountHelper;

/**
 * The main activity in the application. Responsible for handling fragments and
 * handling the navigation drawer including drawer activation and item clicks.
 * When the activity is started it updates outdated characters in the database,
 * in a recursive manner. The activity also receives a broadcast when the user
 * has found a card, whereupon the activity connects to the Marvel API and
 * retrieves and stores a character in the database. Achievements are also
 * monitored by the activity.
 *
 * @author Viktor Sieger
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener {

    public static final String ACTION_GEOFENCE_2 = "geofenceIntentFilter2";

    private static final String FILENAME_DIRECTORY_CHARACTERS_IMAGES = "imagesCharacters";
    private static final String FILENAME_MARVEL_CHARACTER_COUNT = "characterCountMarvel";
    private static final String KEY_FIRST_TIME_SETUP_DONE = "mFirstTimeSetupDone";
    private static final String KEY_IS_DRAWER_ENABLED = "mIsDrawerEnabled";
    private static final String TAG = "MainActivity";

    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private boolean mFirstTimeSetupDone = false;
    private boolean mIsDrawerEnabled = true;
    private Character mCharacter1;
    private Character mCharacter2;
    private DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;
    private int mCharactersAvailable = 1300;
    private List<Character> mOutdatedCharacters;
    private LocalBroadcastManager mLocalBroadcastManager;
    private NavigationView mNavigationView;

    /**
     * BroadcastReceiver that receives broadcasts when the user has found a
     * card. A character offset is generated randomly and a API call for the
     * character is made.
     */
    private BroadcastReceiver mGeofenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int newCharacterRequestOffset;

            newCharacterRequestOffset = (new Random()).nextInt(mCharactersAvailable - 1);

            if(((ComicCollectorApp)getApplication()).isNetworkConnected()) {
                ((ComicCollectorApp)getApplication()).getRequestQueue().addToRequestQueue(getRequestCharacterByOffset(newCharacterRequestOffset));
            }
        }
    };

    /**
     * ResponseListener that is used if errors occur in any of the API calls.
     */
    private Response.ErrorListener mResponseListenerError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getApplicationContext(), R.string.error_api_response, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error: Error in response");
        }
    };

    /**
     * ResponseListener that is used to store the total number of available
     * characters after a call to Marvel's API.
     */
    private Response.Listener<JSONObject> mCharactersAvailableListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

            CharacterCountHelper characterCountHelper;

            try {
                if(response.getInt("code") == 200) {
                    mCharactersAvailable = response.getJSONObject("data").getInt("total");
                    characterCountHelper = new CharacterCountHelper(new Date(), mCharactersAvailable);

                    ((ComicCollectorApp)getApplication()).writeObjectToPersistentStorage(FILENAME_MARVEL_CHARACTER_COUNT, characterCountHelper);
                }
                else {
                    Log.e(TAG, "Error: " + response.getString("status"));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error: Could not find/coerce mapping (" + e.getMessage() + ")");
            }
        }
    };

    /**
     * ResponseListener that is used to update a character after a call to
     * Marvel's API. This listener is used together with
     * mCharacterImageListener1 to retrieve a complete character.
     */
    private Response.Listener<JSONObject> mCharacterListener1 = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

            File pathCharacterImage;
            JSONObject characterJSON;
            String characterImageURL;
            String characterImageURLExtension;
            String characterImageURLPath;

            try {
                if(response.getInt("code") == 200) {
                    characterJSON = response.getJSONObject("data").getJSONArray("results").getJSONObject(0);

                    mCharacter1 = parseJSONCharacterToEntityCharacter(characterJSON);

                    characterImageURLPath = characterJSON.getJSONObject("thumbnail").getString("path");
                    characterImageURLExtension = characterJSON.getJSONObject("thumbnail").getString("extension");
                    characterImageURL = characterImageURLPath.concat("/standard_fantastic.").concat(characterImageURLExtension);

                    if(((ComicCollectorApp)getApplication()).isNetworkConnected()) {
                        if((pathCharacterImage = getPathCharacterImage(String.valueOf(mCharacter1.getId()))) != null) {
                            mCharacter1.setPathLocalImage(pathCharacterImage.toString());
                            ((ComicCollectorApp)getApplication()).getRequestQueue().addToRequestQueue(getRequestCharacterImage(characterImageURL, mCharacterImageListener1));
                        }
                    }
                }
                else {
                    Log.e(TAG, "Error: " + response.getString("status"));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error: Could not find/coerce mapping (" + e.getMessage() + ")");
            }
        }
    };

    /**
     * ResponseListener that is used to update a character's image after a call
     * to Marvel's API. This listener is used together with mCharacterListener1
     * to retrieve a complete character.
     */
    private Response.Listener<Bitmap> mCharacterImageListener1 = new Response.Listener<Bitmap>() {
        @Override
        public void onResponse(final Bitmap response) {
            ((ComicCollectorApp)getApplication()).getAppExecutors().backgroundThreads().execute(new Runnable() {
                @Override
                public void run() {
                    if(!writeCharacterBitmapToPersistentStorage(mCharacter1.getPathLocalImage(), response)) {
                        Log.e(TAG, "Error: Could not compress image");
                    }

                    ((ComicCollectorApp)getApplication()).getRepository().updateCharacter(mCharacter1);

                    mOutdatedCharacters.remove(0);

                    if(!mOutdatedCharacters.isEmpty()) {
                        if(((ComicCollectorApp)getApplication()).isNetworkConnected()) {
                            ((ComicCollectorApp)getApplication()).getRequestQueue().addToRequestQueue(getRequestCharacterByID(mOutdatedCharacters.get(0).getId()));
                        }
                    }
                }
            });
        }
    };

    /**
     * ResponseListener that is used to store a character after a call to
     * Marvel's API. This listener is used together with
     * mCharacterImageListener2 to retrieve a complete character.
     */
    private Response.Listener<JSONObject> mCharacterListener2 = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(final JSONObject response) {
            ((ComicCollectorApp)getApplication()).getAppExecutors().backgroundThreads().execute(new Runnable() {
                @Override
                public void run() {

                    Card card;
                    File pathCharacterImage;
                    JSONObject characterJSON;
                    Player player;
                    String characterImageURL;
                    String characterImageURLExtension;
                    String characterImageURLPath;

                    try {
                        if(response.getInt("code") == 200) {
                            characterJSON = response.getJSONObject("data").getJSONArray("results").getJSONObject(0);

                            if(((ComicCollectorApp)getApplication()).getRepository().isCharacterInDatabase(characterJSON.getInt("id"))) {
                                player = ((ComicCollectorApp)getApplication()).getRepository().loadPlayer();

                                card = new Card(player.getId(), characterJSON.getInt("id"), getRandomCardCondition(), new Date());

                                ((ComicCollectorApp)getApplication()).getRepository().insertCard(card);

                                updateAchievements();
                            }
                            else {
                                mCharacter2 = parseJSONCharacterToEntityCharacter(characterJSON);

                                characterImageURLPath = characterJSON.getJSONObject("thumbnail").getString("path");
                                characterImageURLExtension = characterJSON.getJSONObject("thumbnail").getString("extension");
                                characterImageURL = characterImageURLPath.concat("/standard_fantastic.").concat(characterImageURLExtension);

                                if(((ComicCollectorApp)getApplication()).isNetworkConnected()) {
                                    if((pathCharacterImage = getPathCharacterImage(String.valueOf(mCharacter2.getId()))) != null) {
                                        mCharacter2.setPathLocalImage(pathCharacterImage.toString());
                                        ((ComicCollectorApp)getApplication()).getRequestQueue().addToRequestQueue(getRequestCharacterImage(characterImageURL, mCharacterImageListener2));
                                    }
                                }
                            }
                        }
                        else {
                            Log.e(TAG, "Error: " + response.getString("status"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error: Could not find/coerce mapping (" + e.getMessage() + ")");
                    }
                }
            });
        }
    };

    /**
     * ResponseListener that is used to store a character's image after a call
     * to Marvel's API. This listener is used together with mCharacterListener2
     * to retrieve a complete character.
     */
    private Response.Listener<Bitmap> mCharacterImageListener2 = new Response.Listener<Bitmap>() {
        @Override
        public void onResponse(final Bitmap response) {
            ((ComicCollectorApp)getApplication()).getAppExecutors().backgroundThreads().execute(new Runnable() {
                @Override
                public void run() {

                    Card card;
                    Player player;

                    if(!writeCharacterBitmapToPersistentStorage(mCharacter2.getPathLocalImage(), response)) {
                        Log.e(TAG, "Error: Could not compress image");
                    }

                    ((ComicCollectorApp)getApplication()).getRepository().insertCharacter(mCharacter2);

                    player = ((ComicCollectorApp)getApplication()).getRepository().loadPlayer();

                    card = new Card(player.getId(), mCharacter2.getId(), getRandomCardCondition(), new Date());

                    ((ComicCollectorApp)getApplication()).getRepository().insertCard(card);

                    updateAchievements();
                }
            });
        }
    };

    /**
     * ResponsListener that is used when the time is retrieved after a call to
     * WorldTimeAPI's time API. Updates the achievements.
     */
    private Response.Listener<JSONObject> mTimeListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(final JSONObject response) {
            ((ComicCollectorApp)getApplication()).getAppExecutors().backgroundThreads().execute(new Runnable() {
                @Override
                public void run() {

                    Achievement achievement;
                    int unixtime;

                    try {
                        achievement = null;
                        unixtime = response.getInt("unixtime");

                        switch(((ComicCollectorApp)getApplication()).getRepository().getNumberOfCards()) {
                            case 3:
                                achievement = ((ComicCollectorApp)getApplication()).getRepository().loadAchievement("Novice collector");
                                break;
                            case 10:
                                achievement = ((ComicCollectorApp)getApplication()).getRepository().loadAchievement("1010");
                                break;
                            case 111:
                                achievement = ((ComicCollectorApp)getApplication()).getRepository().loadAchievement("CXI");
                                break;
                            case 666:
                                achievement = ((ComicCollectorApp)getApplication()).getRepository().loadAchievement("The number of the beast");
                                break;
                            case 1337:
                                achievement = ((ComicCollectorApp)getApplication()).getRepository().loadAchievement("Elite collector");
                                break;
                        }

                        achievement.setDate_completed(new Date((long)unixtime * 1000));

                        ((ComicCollectorApp)getApplication()).getRepository().updateAchievements(achievement);

                    } catch (JSONException e) {
                        Log.e(TAG, "Error: Could not find/coerce mapping (" + e.getMessage() + ")");
                    } catch (Exception e) {
                        Log.e(TAG, "Error: " + e.getMessage());
                    }
                }
            });
        }
    };

    /**
     * Called when the activity is starting. Initializes UI components.
     *
     * @param savedInstanceState    Bundle with data to restore state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActionBar actionBar;
        Toolbar toolbar;

        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open_description, R.string.drawer_close_description);

        setSupportActionBar(toolbar);

        if((actionBar = getSupportActionBar()) != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_toolbar_hamburger);
        }

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);

        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mGeofenceReceiver, new IntentFilter(ACTION_GEOFENCE_2));

        if(!mIsDrawerEnabled) {
            setDrawerState(false);
        }

        if(((ComicCollectorApp)getApplication()).isNetworkConnected()) {
            run();
        }
        else {
            showNetworkAccessAlertDialog();
        }
    }

    /**
     * Called when the activity is destroyed. Unregisters the broadcast receiver
     * for geofencing broadcasts.
     */
    @Override
    protected void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(mGeofenceReceiver);
        super.onDestroy();
    }

    /**
     * Called when an item in the options menu is selected. Calls corresponding
     * methods in fragments and manages the navigation drawer's state.
     *
     * @param menuItem      The menu item that was selected.
     * @return              Return false to allow normal menu processing to
     *                      proceed, true to consume it here.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        Fragment fragmentCurrent;

        if((fragmentCurrent = mFragmentManager.findFragmentById(R.id.content_frame)) == null) {
            Log.e(TAG, "Error: Could not find fragment");
            return false;
        }

        if(menuItem.getItemId() == android.R.id.home) {
            if((fragmentCurrent.getClass() == CharacterDetailsFragment.class) || (fragmentCurrent.getClass() == AchievementDetailsFragment.class)) {
                setDrawerState(true);
                mIsDrawerEnabled = true;
                super.onBackPressed();
            }
            else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }

            return true;
        }
        else if((menuItem.getItemId() == R.id.toolbar_map_new_geofences) || (menuItem.getItemId() == R.id.toolbar_collection_sort) || (menuItem.getItemId() == R.id.toolbar_achievements_sort)) {
            fragmentCurrent.onOptionsItemSelected(menuItem);
            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    /**
     * Called when an item in the navigation menu is selected. Replaces
     * fragments in the content frame.
     *
     * @param menuItem      The menu item that was selected.
     * @return              Return true to display the item as the selected
     *                      item.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        boolean isSameItemSelected;
        Class fragmentClass;
        Fragment fragmentCurrent, fragmentNew;

        isSameItemSelected = true;
        fragmentClass = null;

        if((fragmentCurrent = mFragmentManager.findFragmentById(R.id.content_frame)) == null) {
            Log.e(TAG, "Error: Could not find fragment");
            return false;
        }

        if((menuItem.getItemId() == R.id.drawer_item_map) && (fragmentCurrent.getClass() != MapFragment.class)) {
            fragmentClass = MapFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.drawer_item_collection) && (fragmentCurrent.getClass() != CollectionFragment.class)) {
            fragmentClass = CollectionFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.drawer_item_achievements) && (fragmentCurrent.getClass() != AchievementsFragment.class)) {
            fragmentClass = AchievementsFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.drawer_item_about) && (fragmentCurrent.getClass() != AboutFragment.class)) {
            fragmentClass = AboutFragment.class;
            isSameItemSelected = false;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        if(!isSameItemSelected) {
            try {
                fragmentNew = (Fragment)fragmentClass.newInstance();
                mFragmentManager.beginTransaction().replace(R.id.content_frame, fragmentNew, fragmentNew.getClass().getName()).addToBackStack(fragmentNew.getClass().getName()).commit();
            } catch(Exception e) {
                Log.e(TAG, "Error: " + e.getMessage());
            }

            menuItem.setChecked(true);
        }
        else {
            return false;
        }

        return true;
    }

    /**
     * Called whenever the contents of the back stack change. Checks the menu
     * item for the current fragment in the navigation drawer.
     */
    @Override
    public void onBackStackChanged() {

        Fragment currentBackStackFragment;

        if((currentBackStackFragment = mFragmentManager.findFragmentById(R.id.content_frame)) == null) {
            Log.e(TAG, "Error: Could not find fragment");
        }

        if(currentBackStackFragment instanceof MapFragment) {
            mNavigationView.setCheckedItem(R.id.drawer_item_map);
        }
        else if(currentBackStackFragment instanceof CollectionFragment) {
            mNavigationView.setCheckedItem(R.id.drawer_item_collection);
        }
        else if(currentBackStackFragment instanceof AchievementsFragment) {
            mNavigationView.setCheckedItem(R.id.drawer_item_achievements);
        }
        else if(currentBackStackFragment instanceof AboutFragment) {
            mNavigationView.setCheckedItem(R.id.drawer_item_about);
        }
    }

    /**
     * Called when the activity has detected the user's press of the back key.
     * Closes the navigation drawer if it is open. Closes the activity if
     * there's only one fragment left in the backstack (i.e. the user pressed
     * back from the initial MapFragment). Also manages the navigation drawer's
     * state.
     */
    @Override
    public void onBackPressed() {

        Fragment fragmentCurrent;

        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            if(mFragmentManager.getBackStackEntryCount() == 1) {
                finish();
            }
            else {
                if((fragmentCurrent = mFragmentManager.findFragmentById(R.id.content_frame)) == null) {
                    Log.e(TAG, "Error: Could not find fragment");
                }

                if((fragmentCurrent.getClass() == CharacterDetailsFragment.class) || (fragmentCurrent.getClass() == AchievementDetailsFragment.class)) {
                    setDrawerState(true);
                    mIsDrawerEnabled = true;
                }

                super.onBackPressed();
            }
        }
    }

    /**
     * Receives the result from MapFragment's call to
     * startResolutionForResult(). Simply calls the corresponding method in
     * MapFragment.
     *
     * @param requestCode   The integer request code originally supplied to
     *                      startResolutionForResult(), allowing you to identify
     *                      who this result came from.
     * @param resultCode    The integer result code returned by the child
     *                      activity through its setResult().
     * @param data          An Intent, which can return result data to the
     *                      caller (various data can be attached to Intent
     *                      "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Fragment currentFragment;

        super.onActivityResult(requestCode, resultCode, data);

        if((currentFragment = mFragmentManager.findFragmentById(R.id.content_frame)) != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * This method is called before an activity may be killed so that when it
     * comes back some time in the future it can restore its state.
     *
     * @param outState      Bundle in which to place your saved state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_FIRST_TIME_SETUP_DONE, mFirstTimeSetupDone);
        outState.putBoolean(KEY_IS_DRAWER_ENABLED, mIsDrawerEnabled);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called to restore state from data in bundle.
     *
     * @param savedInstanceState    Bundle containing state data.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            return;
        }

        if(savedInstanceState.containsKey(KEY_FIRST_TIME_SETUP_DONE)) {
            mFirstTimeSetupDone = savedInstanceState.getBoolean(KEY_FIRST_TIME_SETUP_DONE);
        }

        if(savedInstanceState.containsKey(KEY_IS_DRAWER_ENABLED)) {
            mIsDrawerEnabled = savedInstanceState.getBoolean(KEY_IS_DRAWER_ENABLED);
        }
    }

    /**
     * Displays an alert dialog with information about internet access.
     */
    private void showNetworkAccessAlertDialog() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.alert_internet_message_string)
                .setPositiveButton(R.string.alert_internet_positive_button_string, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        run();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Performs a one time setup once that initializes the system.
     */
    private void run() {
        if(!mFirstTimeSetupDone) {
            updateCharacterCount();
            updateCharacters();

            MapFragment mapFragment = new MapFragment();
            mFragmentManager.beginTransaction().add(R.id.content_frame, mapFragment, mapFragment.getClass().getName()).addToBackStack(mapFragment.getClass().getName()).commit();

            mFirstTimeSetupDone = true;
        }
    }

    /**
     * Examines if there's a stored file with information about the number of
     * available characters in the Marvel's API. Also examines if the
     * information in the file is up to date. If no file exists or it's outdated
     * a call to the Marvel's API is made.
     */
    private void updateCharacterCount() {
        boolean isCharacterCountOutdated;
        CharacterCountHelper characterCountHelper;

        isCharacterCountOutdated = true;

        if(((ComicCollectorApp)getApplication()).isFileInPersistentStorage(FILENAME_MARVEL_CHARACTER_COUNT)) {
            characterCountHelper = (CharacterCountHelper)((ComicCollectorApp)getApplication()).readObjectFromPersistentStorage(FILENAME_MARVEL_CHARACTER_COUNT);

            mCharactersAvailable = characterCountHelper.getCharacterCount();

            if(characterCountHelper.getLastUpdated().after(twentyFourHoursAgo())) {
                isCharacterCountOutdated = false;
            }
        }

        if(isCharacterCountOutdated) {
            if(((ComicCollectorApp)getApplication()).isNetworkConnected()) {
                ((ComicCollectorApp)getApplication()).getRequestQueue().addToRequestQueue(getRequestCharactersAvailable());
            }
        }
    }

    /**
     * Runs a background thread that starts updating outdated characters in the
     * database.
     */
    private void updateCharacters() {
        ((ComicCollectorApp)getApplication()).getAppExecutors().backgroundThreads().execute(new Runnable() {
            @Override
            public void run() {
                mOutdatedCharacters = ((ComicCollectorApp)getApplication()).getRepository().loadCharactersNotUpdatedAfter(twentyFourHoursAgo());

                if(!mOutdatedCharacters.isEmpty()) {
                    if(((ComicCollectorApp)getApplication()).isNetworkConnected()) {
                        ((ComicCollectorApp)getApplication()).getRequestQueue().addToRequestQueue(getRequestCharacterByID(mOutdatedCharacters.get(0).getId()));
                    }
                }
            }
        });
    }

    /**
     * Returns a request for available characters in the Marvel API.
     *
     * @return      A JsonObjectRequest that requests the number of available
     * characters from the Marvel API.
     */
    private JsonObjectRequest getRequestCharactersAvailable() {

        String keyMarvelPrivate;
        String keyMarvelPublic;
        String timestamp;
        String URLBase;
        String URLFinal;

        keyMarvelPrivate = getString(R.string.marvelPrivateKey);
        keyMarvelPublic = getString(R.string.marvelPublicKey);
        timestamp = String.valueOf(new Date().getTime());
        URLBase = "https://gateway.marvel.com/v1/public/characters?limit=1";

        if((URLFinal = URLCreator(URLBase, timestamp, keyMarvelPublic, keyMarvelPrivate)) == null) {
            return null;
        }

        return new JsonObjectRequest(Request.Method.GET, URLFinal, null, mCharactersAvailableListener, mResponseListenerError);
    }

    /**
     * Returns a request for a character in the Marvel API, by character ID.
     *
     * @param ID    The character ID.
     * @return      A JsonObjectRequest that requests the character from the
     * Marvel API.
     */
    private JsonObjectRequest getRequestCharacterByID(int ID) {

        String keyMarvelPrivate;
        String keyMarvelPublic;
        String timestamp;
        String URLBase;
        String URLFinal;

        keyMarvelPrivate = getString(R.string.marvelPrivateKey);
        keyMarvelPublic = getString(R.string.marvelPublicKey);
        timestamp = String.valueOf(new Date().getTime());
        URLBase = "https://gateway.marvel.com/v1/public/characters/" + String.valueOf(ID) + "?";

        if((URLFinal = URLCreator(URLBase, timestamp, keyMarvelPublic, keyMarvelPrivate)) == null) {
            return null;
        }

        return new JsonObjectRequest(Request.Method.GET, URLFinal, null, mCharacterListener1, mResponseListenerError);
    }

    /**
     * Returns a request for a character in the Marvel API, by character offset.
     *
     * @param offset    The charcter offset.
     * @return          A JsonObjectRequest that requests the character from the
     * Marvel API.
     */
    private JsonObjectRequest getRequestCharacterByOffset(int offset) {

        String keyMarvelPrivate;
        String keyMarvelPublic;
        String timestamp;
        String URLBase;
        String URLFinal;

        keyMarvelPrivate = getString(R.string.marvelPrivateKey);
        keyMarvelPublic = getString(R.string.marvelPublicKey);
        timestamp = String.valueOf(new Date().getTime());
        URLBase = "https://gateway.marvel.com/v1/public/characters?limit=1&offset=" + String.valueOf(offset);

        if((URLFinal = URLCreator(URLBase, timestamp, keyMarvelPublic, keyMarvelPrivate)) == null) {
            return null;
        }

        return new JsonObjectRequest(Request.Method.GET, URLFinal, null, mCharacterListener2, mResponseListenerError);
    }

    /**
     * Returns a request for a character's image in the Marvel API.
     *
     * @param URL           URL of the image.
     * @param listener      Listener to receive the decoded bitmap.
     * @return              An ImageRequest that requests the character's image
     * from the Marvel API.
     */
    private ImageRequest getRequestCharacterImage(String URL, Response.Listener<Bitmap> listener) {
        return new ImageRequest(URL, listener, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, mResponseListenerError);
    }

    /**
     * Returns a request for the time in the WorldTimeAPI's time API.
     *
     * @return      A JsonObjectRequest that requests the time from the
     * WorldTimeAPI's time API.
     */
    private JsonObjectRequest getRequestTime() {
        return new JsonObjectRequest(Request.Method.GET, "http://worldtimeapi.org/api/ip", null, mTimeListener, mResponseListenerError);
    }

    /**
     * Method that facilitates the creation of the final URL for calls to
     * Marvel's API. Marvel's API demands a timestamp, a API key and a MD5 hash
     * be included in the final URL. For more information see
     * https://developer.marvel.com/documentation/authorization.
     *
     * @param URLBase               The endpoint part of the URL.
     * @param timestamp             The timestamp to be used.
     * @param keyMarvelPublic       The public key provided by Marvel.
     * @param keyMarvelPrivate      The private key provided by Marvel.
     * @return                      The final URL.
     */
    private String URLCreator(String URLBase, String timestamp, String keyMarvelPublic, String keyMarvelPrivate) {

        byte[] bytesToHash;
        byte[] hashAsByteArray;
        MessageDigest md;
        String filler;
        String hashAsString;

        try {
            filler = "&";

            md = MessageDigest.getInstance("MD5");
            bytesToHash = (timestamp + keyMarvelPrivate + keyMarvelPublic).getBytes();
            hashAsByteArray = md.digest(bytesToHash);
            hashAsString = (new BigInteger(1, hashAsByteArray)).toString(16);

            // Perform zero-padding
            while(hashAsString.length() < 32) {
                hashAsString = "0" + hashAsString;
            }

            if(URLBase.substring(URLBase.length() - 1).equals("?")) {
                filler = "";
            }

            return URLBase + filler + "ts=" + timestamp + "&apikey=" + keyMarvelPublic + "&hash=" + hashAsString;

        } catch (Exception e) {
            Log.e(TAG, "Error: Could not create URL (" + e.getMessage() + ")");
        }

        return null;
    }

    /**
     * Parses a character from the JSONObject format to the (entity) Character
     * format.
     *
     * @param JSONCharacter     JSONObject format of the character.
     * @return                  Character format of the character.
     */
    private Character parseJSONCharacterToEntityCharacter(JSONObject JSONCharacter) {

        ArrayList<String> comics;
        Date lastUpdated;
        int id;
        int returned;
        JSONArray items;
        JSONArray urls;
        String description;
        String name;
        String tempName;
        String urlWiki;

        comics = new ArrayList<>();
        urlWiki = "http://marvel.com";

        try {
            id = JSONCharacter.getInt("id");
            lastUpdated = new Date();
            name = JSONCharacter.getString("name");
            description = JSONCharacter.getString("description");

            if(description.equals("")) {
                description = "No description.";
            }

            returned = JSONCharacter.getJSONObject("comics").getInt("returned");
            items = JSONCharacter.getJSONObject("comics").getJSONArray("items");

            for(int i = 0; i < returned; i++) {
                tempName = items.getJSONObject(i).getString("name");
                comics.add(tempName);
            }

            urls = JSONCharacter.getJSONArray("urls");

            for(int i = 0; i < urls.length(); i++) {
                if(urls.getJSONObject(i).getString("type").equals("wiki")) {
                    urlWiki = urls.getJSONObject(i).getString("url");
                    break;
                }
                else if(urls.getJSONObject(i).getString("type").equals("detail")) {
                    urlWiki = urls.getJSONObject(i).getString("url");
                }
                else if(urls.getJSONObject(i).getString("type").equals("comiclink")) {
                    urlWiki = urls.getJSONObject(i).getString("url");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error: Could not find/coerce mapping (" + e.getMessage() + ")");
            return null;
        }

        return new Character(id, lastUpdated, name, description, "", comics, urlWiki);
    }

    /**
     * Returns a File representing a path where a character's image can be
     * stored.
     *
     * @param filename      The name of the file.
     * @return              A File representing the path to a character's image.
     */
    private File getPathCharacterImage(String filename) {

        File directoryRoot;

        directoryRoot = new File(getFilesDir(), FILENAME_DIRECTORY_CHARACTERS_IMAGES);

        if(!directoryRoot.exists()) {
            if(!directoryRoot.mkdirs()) {
                Log.e(TAG, "Error: Could not create directory/directories");
                return null;
            }
        }

        return new File(directoryRoot, filename);
    }

    /**
     * Writes a bitmap to the file system.
     *
     * @param pathFile      Path specifying where to store the bitmap.
     * @param bitmap        The bitmap to be stored.
     * @return              True if successfully stored in the file system.
     */
    private boolean writeCharacterBitmapToPersistentStorage(String pathFile, Bitmap bitmap) {

        boolean isCompressSuccessful;
        FileOutputStream fos;

        isCompressSuccessful = false;

        try {
            fos = new FileOutputStream(pathFile);
            isCompressSuccessful = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return isCompressSuccessful;
    }

    /**
     * Return a randomly generated CardCondition.
     *
     * @return      A randomly generated CardCondition.
     */
    private CardCondition getRandomCardCondition() {
        int x;

        x = (new Random()).nextInt(CardCondition.values().length);

        return CardCondition.values()[x];
    }

    /**
     * Returns a Date representing the time 24 hours earlier than the current
     * system time.
     *
     * @return      A Date representing the time 24 hours earlier than the
     *              current system time.
     */
    private Date twentyFourHoursAgo() {
        Calendar calendar;

        calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        return calendar.getTime();
    }

    /**
     * Replaces the current fragment in the content frame with a fragment
     * showing the details of a character. Also disables the navigation drawer.
     *
     * @param character     The character to show details about in the new
     *                      fragment.
     */
    public void show(Character character) {
        CharacterDetailsFragment characterDetailsFragment;

        characterDetailsFragment = CharacterDetailsFragment.forItem(character.getId());

        mFragmentManager.beginTransaction().replace(R.id.content_frame, characterDetailsFragment, characterDetailsFragment.getClass().getName()).addToBackStack(characterDetailsFragment.getClass().getName()).commit();

        setDrawerState(false);
        mIsDrawerEnabled = false;
    }

    /**
     * Replaces the current fragment in the content frame with a fragment
     * showing the details of an achievement. Also disables the navigation
     * drawer.
     *
     * @param achievement       The achievement to show details about in the new
     *                          fragment.
     */
    public void show(Achievement achievement) {
        AchievementDetailsFragment achievementDetailsFragment;

        achievementDetailsFragment = AchievementDetailsFragment.forItem(achievement.getId());

        mFragmentManager.beginTransaction().replace(R.id.content_frame, achievementDetailsFragment, achievementDetailsFragment.getClass().getName()).addToBackStack(achievementDetailsFragment.getClass().getName()).commit();

        setDrawerState(false);
        mIsDrawerEnabled = false;
    }

    /**
     * Sets the navigation drawer's state.
     *
     * @param isEnabled     True if navigation drawer is enabled.
     */
    private void setDrawerState(boolean isEnabled) {
        if(isEnabled) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mActionBarDrawerToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
            mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        }
        else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mActionBarDrawerToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mActionBarDrawerToggle.setDrawerIndicatorEnabled(false);
        }

        mActionBarDrawerToggle.syncState();
    }

    /**
     * Runs a background thread that checks if any achievement is completed.
     */
    private void updateAchievements() {
        ((ComicCollectorApp)getApplication()).getAppExecutors().backgroundThreads().execute(new Runnable() {
            @Override
            public void run() {

                boolean isAchievementCompleted;

                isAchievementCompleted = false;

                switch(((ComicCollectorApp)getApplication()).getRepository().getNumberOfCards()) {
                    case 3:
                        isAchievementCompleted = true;
                        break;
                    case 10:
                        isAchievementCompleted = true;
                        break;
                    case 111:
                        isAchievementCompleted = true;
                        break;
                    case 666:
                        isAchievementCompleted = true;
                        break;
                    case 1337:
                        isAchievementCompleted = true;
                        break;
                }

                if(isAchievementCompleted) {
                    if(((ComicCollectorApp)getApplication()).isNetworkConnected()) {
                        ((ComicCollectorApp)getApplication()).getRequestQueue().addToRequestQueue(getRequestTime());
                    }
                }
            }
        });
    }
}
