package se.umu.visi0009.comiccollector.activities;

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
import se.umu.visi0009.comiccollector.db.entities.Card;
import se.umu.visi0009.comiccollector.db.entities.Character;
import se.umu.visi0009.comiccollector.db.entities.Player;
import se.umu.visi0009.comiccollector.enums.CardCondition;
import se.umu.visi0009.comiccollector.fragments.AboutFragment;
import se.umu.visi0009.comiccollector.fragments.AchievementsFragment;
import se.umu.visi0009.comiccollector.fragments.CollectionFragment;
import se.umu.visi0009.comiccollector.fragments.MapFragment;
import se.umu.visi0009.comiccollector.fragments.SettingsFragment;
import se.umu.visi0009.comiccollector.fragments.StatsFragment;
import se.umu.visi0009.comiccollector.other.CharacterCountHelper;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener {

    public static final String ACTION_GEOFENCE_2 = "geofenceIntentFilter2";

    private static final String FILENAME_DIRECTORY_CHARACTERS_IMAGES = "imagesCharacters";
    private static final String FILENAME_MARVEL_CHARACTER_COUNT = "characterCountMarvel";
    private static final String KEY_FIRST_TIME_SETUP_DONE = "mFirstTimeSetupDone";

    private boolean mFirstTimeSetupDone = false;
    private Character mCharacter1;
    private Character mCharacter2;
    private DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;
    private int mCharactersAvailable = 1300;
    private List<Character> mOutdatedCharacters;
    private LocalBroadcastManager mLocalBroadcastManager;
    private NavigationView mNavigationView;

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

    private Response.ErrorListener mResponseListenerError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Toast.makeText(getApplicationContext(), "Error: Error in response", Toast.LENGTH_SHORT).show();
            Log.d("TEST", "Error: Error in response");
        }
    };

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
                    Log.d("TEST", "Error: " + response.getString("status"));
                }
            } catch (JSONException e) {
                Log.d("TEST", "Error: Could not find/coerce mapping (" + e.getMessage() + ")");
            }
        }
    };

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
                    Log.d("TEST", "Error: " + response.getString("status"));
                }
            } catch (JSONException e) {
                Log.d("TEST", "Error: Could not find/coerce mapping (" + e.getMessage() + ")");
            }
        }
    };

    private Response.Listener<Bitmap> mCharacterImageListener1 = new Response.Listener<Bitmap>() {
        @Override
        public void onResponse(final Bitmap response) {
            ((ComicCollectorApp)getApplication()).getAppExecutors().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    if(!writeCharacterBitmapToPersistentStorage(mCharacter1.getPathLocalImage(), response)) {
                        Log.d("TEST", "Error: Could not compress image");
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

    private Response.Listener<JSONObject> mCharacterListener2 = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(final JSONObject response) {
            ((ComicCollectorApp)getApplication()).getAppExecutors().diskIO().execute(new Runnable() {
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
                            Log.d("TEST", "Error: " + response.getString("status"));
                        }
                    } catch (JSONException e) {
                        Log.d("TEST", "Error: Could not find/coerce mapping (" + e.getMessage() + ")");
                    }
                }
            });
        }
    };

    private Response.Listener<Bitmap> mCharacterImageListener2 = new Response.Listener<Bitmap>() {
        @Override
        public void onResponse(final Bitmap response) {
            ((ComicCollectorApp)getApplication()).getAppExecutors().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    Card card;
                    Player player;

                    if(!writeCharacterBitmapToPersistentStorage(mCharacter2.getPathLocalImage(), response)) {
                        Log.d("TEST", "Error: Could not compress image");
                    }

                    ((ComicCollectorApp)getApplication()).getRepository().insertCharacter(mCharacter2);

                    player = ((ComicCollectorApp)getApplication()).getRepository().loadPlayer();

                    card = new Card(player.getId(), mCharacter2.getId(), getRandomCardCondition(), new Date());

                    ((ComicCollectorApp)getApplication()).getRepository().insertCard(card);
                }
            });
        }
    };

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

        setSupportActionBar(toolbar);

        if((actionBar = getSupportActionBar()) != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);
        }

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);

        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mGeofenceReceiver, new IntentFilter(ACTION_GEOFENCE_2));

        if(((ComicCollectorApp)getApplication()).isNetworkConnected()) {
            run();
        }
        else {
            showNetworkAccessAlertDialog();
        }
    }

    @Override
    protected void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(mGeofenceReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        Fragment currentFragment;

        if(menuItem.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        else if((menuItem.getItemId() == R.id.toolbar_new_geofences) || (menuItem.getItemId() == R.id.toolbar_sort)) {
            if((currentFragment = mFragmentManager.findFragmentById(R.id.content_frame)) != null) {
                currentFragment.onOptionsItemSelected(menuItem);
            }

            return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        boolean isSameItemSelected;
        Class fragmentClass;
        Fragment fragmentCurrent, fragmentNew;

        isSameItemSelected = true;
        fragmentCurrent = mFragmentManager.findFragmentById(R.id.content_frame);
        fragmentClass = null;

        if((menuItem.getItemId() == R.id.nav_map) && (fragmentCurrent.getClass() != MapFragment.class)) {
            fragmentClass = MapFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_collection) && (fragmentCurrent.getClass() != CollectionFragment.class)) {
            fragmentClass = CollectionFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_achievements) && (fragmentCurrent.getClass() != AchievementsFragment.class)) {
            fragmentClass = AchievementsFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_stats) && (fragmentCurrent.getClass() != StatsFragment.class)) {
            fragmentClass = StatsFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_settings) && (fragmentCurrent.getClass() != SettingsFragment.class)) {
            fragmentClass = SettingsFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_about) && (fragmentCurrent.getClass() != AboutFragment.class)) {
            fragmentClass = AboutFragment.class;
            isSameItemSelected = false;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        if(!isSameItemSelected) {
            try {
                fragmentNew = (Fragment)fragmentClass.newInstance();
                mFragmentManager.beginTransaction().replace(R.id.content_frame, fragmentNew, fragmentNew.getClass().getName()).addToBackStack(fragmentNew.getClass().getName()).commit();
            } catch(Exception e) {
                e.printStackTrace();
            }

            menuItem.setChecked(true);
        }
        else {
            return false;
        }

        return true;
    }

    @Override
    public void onBackStackChanged() {

        Fragment currentBackStackFragment;

        currentBackStackFragment = mFragmentManager.findFragmentById(R.id.content_frame);

        if(currentBackStackFragment instanceof MapFragment) {
            mNavigationView.setCheckedItem(R.id.nav_map);
        }
        else if(currentBackStackFragment instanceof CollectionFragment) {
            mNavigationView.setCheckedItem(R.id.nav_collection);
        }
        else if(currentBackStackFragment instanceof AchievementsFragment) {
            mNavigationView.setCheckedItem(R.id.nav_achievements);
        }
        else if(currentBackStackFragment instanceof StatsFragment) {
            mNavigationView.setCheckedItem(R.id.nav_stats);
        }
        else if(currentBackStackFragment instanceof SettingsFragment) {
            mNavigationView.setCheckedItem(R.id.nav_settings);
        }
        else if(currentBackStackFragment instanceof AboutFragment) {
            mNavigationView.setCheckedItem(R.id.nav_about);
        }
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            if(mFragmentManager.getBackStackEntryCount() == 1) {
                finish();
            }
            else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Fragment currentFragment;

        super.onActivityResult(requestCode, resultCode, data);

        if((currentFragment = mFragmentManager.findFragmentById(R.id.content_frame)) != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_FIRST_TIME_SETUP_DONE, mFirstTimeSetupDone);
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            return;
        }

        if(savedInstanceState.containsKey(KEY_FIRST_TIME_SETUP_DONE)) {
            mFirstTimeSetupDone = savedInstanceState.getBoolean(KEY_FIRST_TIME_SETUP_DONE);
        }
    }

    private void showNetworkAccessAlertDialog() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);

        builder.setMessage("The app needs internet access to function properly.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        run();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void run() {
        if(!mFirstTimeSetupDone) {
            updateCharacterCount();
            updateCharacters();

            MapFragment mapFragment = new MapFragment();
            mFragmentManager.beginTransaction().add(R.id.content_frame, mapFragment, mapFragment.getClass().getName()).addToBackStack(mapFragment.getClass().getName()).commit();
            mFirstTimeSetupDone = true;
        }
    }

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

    private void updateCharacters() {
        ((ComicCollectorApp)getApplication()).getAppExecutors().diskIO().execute(new Runnable() {
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

    private ImageRequest getRequestCharacterImage(String URL, Response.Listener<Bitmap> listener) {
        return new ImageRequest(URL, listener, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, mResponseListenerError);
    }

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
            Log.d("TEST", "Error: Could not create URL (" + e.getMessage() + ")");
        }

        return null;
    }

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
            Log.d("TEST", "Error: Could not find/coerce mapping (" + e.getMessage() + ")");
            return null;
        }

        return new Character(id, lastUpdated, name, description, "", comics, urlWiki);
    }

    private File getPathCharacterImage(String filename) {

        File directoryRoot;

        directoryRoot = new File(getFilesDir(), FILENAME_DIRECTORY_CHARACTERS_IMAGES);

        if(!directoryRoot.exists()) {
            if(!directoryRoot.mkdirs()) {
                Log.d("TEST", "Error: Could not create directory/directories");
                return null;
            }
        }

        return new File(directoryRoot, filename);
    }

    private boolean writeCharacterBitmapToPersistentStorage(String pathFile, Bitmap bitmap) {

        boolean isCompressSuccessful;
        FileOutputStream fos;

        isCompressSuccessful = false;

        try {
            fos = new FileOutputStream(pathFile);
            isCompressSuccessful = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch(Exception e) {
            Log.d("TEST", e.getMessage());
        }

        return isCompressSuccessful;
    }

    private CardCondition getRandomCardCondition() {
        int x;

        x = (new Random()).nextInt(CardCondition.values().length);

        return CardCondition.values()[x];
    }

    private Date twentyFourHoursAgo() {
        Calendar calendar;

        calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -24);
        return calendar.getTime();
    }

    public void show(Character character) {
        Log.d("TEST", "MainActivity - show");
        Log.d("TEST", character.getName());
    }
}
