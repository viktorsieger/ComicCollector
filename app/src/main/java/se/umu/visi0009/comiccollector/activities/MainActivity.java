package se.umu.visi0009.comiccollector.activities;

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

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);
        }

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mNavigationView = findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                        menuItem.setChecked(true);

                        mDrawerLayout.closeDrawers();

                        Fragment fragment = null;
                        Class fragmentClass;

                        switch(menuItem.getItemId()) {
                            case R.id.nav_map:
                                fragmentClass = MapFragment.class;
                                break;
                            case R.id.nav_collection:
                                fragmentClass = CollectionFragment.class;
                                break;
                            case R.id.nav_achievements:
                                fragmentClass = AchievementsFragment.class;
                                break;
                            case R.id.nav_stats:
                                fragmentClass = StatsFragment.class;
                                break;
                            case R.id.nav_settings:
                                fragmentClass = SettingsFragment.class;
                                break;
                            case R.id.nav_about:
                                fragmentClass = AboutFragment.class;
                                break;
                            default:
                                fragmentClass = MapFragment.class;
                                break;
                        }

                        try {
                            fragment = (Fragment)fragmentClass.newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

                        return true;
                    }
                }
        );
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

}
