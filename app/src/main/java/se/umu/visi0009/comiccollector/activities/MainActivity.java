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

import se.umu.visi0009.comiccollector.R;
import se.umu.visi0009.comiccollector.fragments.AboutFragment;
import se.umu.visi0009.comiccollector.fragments.AchievementsFragment;
import se.umu.visi0009.comiccollector.fragments.CollectionFragment;
import se.umu.visi0009.comiccollector.fragments.MapFragment;
import se.umu.visi0009.comiccollector.fragments.SettingsFragment;
import se.umu.visi0009.comiccollector.fragments.StatsFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, FragmentManager.OnBackStackChangedListener {

    private DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActionBar actionBar;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.navigation_view);
        mToolbar = findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);

        actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);
        }

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().add(R.id.content_frame, new MapFragment()).commit();
        mFragmentManager.addOnBackStackChangedListener(this);

        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        boolean isSameItemSelected;
        Fragment currentFragment, newFragment;
        Class fragmentClass;

        isSameItemSelected = true;
        currentFragment = mFragmentManager.findFragmentById(R.id.content_frame);
        newFragment = null;
        fragmentClass = null;

        if((menuItem.getItemId() == R.id.nav_map) && (currentFragment.getClass() != MapFragment.class)) {
            fragmentClass = MapFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_collection) && (currentFragment.getClass() != CollectionFragment.class)) {
            fragmentClass = CollectionFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_achievements) && (currentFragment.getClass() != AchievementsFragment.class)) {
            fragmentClass = AchievementsFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_stats) && (currentFragment.getClass() != StatsFragment.class)) {
            fragmentClass = StatsFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_settings) && (currentFragment.getClass() != SettingsFragment.class)) {
            fragmentClass = SettingsFragment.class;
            isSameItemSelected = false;
        }
        else if((menuItem.getItemId() == R.id.nav_about) && (currentFragment.getClass() != AboutFragment.class)) {
            fragmentClass = AboutFragment.class;
            isSameItemSelected = false;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        if(!isSameItemSelected) {
            try {
                newFragment = (Fragment)fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mFragmentManager.beginTransaction().replace(R.id.content_frame, newFragment).addToBackStack(null).commit();

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
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment currentFragment;

        currentFragment = mFragmentManager.findFragmentById(R.id.content_frame);

        if(currentFragment != null) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
