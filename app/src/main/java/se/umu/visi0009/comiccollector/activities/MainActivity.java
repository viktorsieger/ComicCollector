package se.umu.visi0009.comiccollector.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.Stack;

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.fragments.AboutFragment;
import se.umu.visi0009.comiccollector.fragments.AchievementsFragment;
import se.umu.visi0009.comiccollector.fragments.CollectionFragment;
import se.umu.visi0009.comiccollector.fragments.MapFragment;
import se.umu.visi0009.comiccollector.fragments.SettingsFragment;
import se.umu.visi0009.comiccollector.fragments.StatsFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener {

    private static final String KEY_MAP_FRAGMENT_ADDED = "mMapFragmentAdded";

    private boolean mMapFragmentAdded = false;
    private DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar;
        Toolbar toolbar;

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

        if(!mMapFragmentAdded) {
            MapFragment mapFragment = new MapFragment();
            mFragmentManager.beginTransaction().add(R.id.content_frame, mapFragment, mapFragment.getClass().getName()).addToBackStack(mapFragment.getClass().getName()).commit();
            mMapFragmentAdded = true;
        }

        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        Fragment currentFragment;

        switch(menuItem.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.toolbar_new_geofences:
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
        Fragment fragmentCurrent, fragmentNew, fragmentLast, fragmentFirst, fragmentMiddle;
        int i, fragmentIndexInStack;
        Stack<Fragment> stack;

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

            // Examine if the selected fragment is already in the back stack
            if((fragmentLast = mFragmentManager.findFragmentByTag(fragmentClass.getName())) != null) {

                fragmentIndexInStack = -1;

                // Find index in back stack
                for(i = 0; i < mFragmentManager.getBackStackEntryCount(); i++) {
                    if(fragmentClass.getName().equals(mFragmentManager.getBackStackEntryAt(i).getName())) {
                        fragmentIndexInStack = i;
                    }
                }

                stack = new Stack<>();

                // Push subsequent fragments (i.e. fragments after the selected fragment) to stack
                for(i = (mFragmentManager.getBackStackEntryCount() - 1); i > fragmentIndexInStack; i--) {
                    stack.push(mFragmentManager.findFragmentByTag(mFragmentManager.getBackStackEntryAt(i).getName()));
                }

                // Remove back stack completely
                mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                fragmentFirst = stack.pop();
                mFragmentManager.beginTransaction().add(R.id.content_frame, fragmentFirst, fragmentFirst.getClass().getName()).addToBackStack(fragmentFirst.getClass().getName()).commit();

                while(!stack.isEmpty()) {
                    fragmentMiddle = stack.pop();
                    mFragmentManager.beginTransaction().replace(R.id.content_frame, fragmentMiddle, fragmentMiddle.getClass().getName()).addToBackStack(fragmentMiddle.getClass().getName()).commit();
                }

                mFragmentManager.beginTransaction().replace(R.id.content_frame, fragmentLast, fragmentLast.getClass().getName()).addToBackStack(fragmentLast.getClass().getName()).commit();
            }
            else {
                try {
                    fragmentNew = (Fragment)fragmentClass.newInstance();
                    mFragmentManager.beginTransaction().replace(R.id.content_frame, fragmentNew, fragmentNew.getClass().getName()).addToBackStack(fragmentNew.getClass().getName()).commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            if((mFragmentManager.getBackStackEntryCount() == 1) && (!mFragmentManager.getBackStackEntryAt(0).getName().equals(MapFragment.class.getName()))) {
                // Remove back stack completely
                mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                Fragment fragmentMap;
                fragmentMap = new MapFragment();
                mFragmentManager.beginTransaction().add(R.id.content_frame, fragmentMap, fragmentMap.getClass().getName()).addToBackStack(fragmentMap.getClass().getName()).commit();
            }
            else if(mFragmentManager.getBackStackEntryCount() == 1) {
                finish();
            }
            else {
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment currentFragment;

        if((currentFragment = mFragmentManager.findFragmentById(R.id.content_frame)) != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_MAP_FRAGMENT_ADDED, mMapFragmentAdded);
        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            return;
        }

        if(savedInstanceState.containsKey(KEY_MAP_FRAGMENT_ADDED)) {
            mMapFragmentAdded = savedInstanceState.getBoolean(KEY_MAP_FRAGMENT_ADDED);
        }
    }
}

/*
* TODO: Change hardcoded strings to string resources.
* TODO: Restrict access level (private/public...) for variables/methods, when appropriate.
* TODO: Remove function "findUniqueKey" (and copy code) since it is only used in one place?
* TODO: Change debug logs to other kind of logs (e.g. "Log.d" to "Log.e").
* TODO: Write better strings.
* TODO: Comment code.
*
* TODO: Write about the known bug. [The bug where the location settings dialog is shown twice after first denying access and restarting the activity.]
*/

// https://stackoverflow.com/questions/34384101/after-geofence-transition-how-do-i-do-something-on-main-activity