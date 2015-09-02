package audio.lisn.activity;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import audio.lisn.R;
import audio.lisn.fragment.HomeFragment;
import audio.lisn.fragment.StoreFragment;

//import android.support.v7.widget.SearchView;

//import android.support.v7.widget.SearchView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,HomeFragment.OnHomeItemSelectedListener,StoreFragment.OnStoreBookSelectedListener {

    private static final String TAG = HomeActivity.class.getSimpleName();

    private DrawerLayout drawer;
    NavigationView navigationView;
//    PlayerControllerView audioPlayerLayout;
    private int mNavItemId;
    boolean isUserLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setActionBarIcon(R.drawable.ic_drawer);

        setContentView(R.layout.activity_home);

        initToolbar();
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        getSupportActionBar().setTitle(R.string.title_home);

        // listen for navigation events
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        isUserLogin=true;
        updateNavigationView();
        if(isUserLogin){
            mNavItemId=R.id.drawer_home;

        }else{
            mNavItemId=R.id.drawer_store;

        }

        navigateFragment(mNavItemId);

    }
    private void updateNavigationView() {
        navigationView.getMenu().clear();
        if (isUserLogin){
            navigationView.inflateMenu(R.menu.navigation_menu_member);

        }else{
            navigationView.inflateMenu(R.menu.navigation_menu_none_member);

        }
    }

    private void initToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
            actionBar.setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setLogo(R.drawable.ic_app_top_bar);
        }
    }


//    @Override protected int getLayoutResource() {
//        return R.layout.activity_home;
//    }



    @Override
    protected void onResume() {
        super.onResume();
       // mNavigationDrawerFragment.setMenuVisibility(false);
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

	        
			MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.home, menu);
            MenuItem searchItem = menu.findItem(R.id.action_search);

            SearchManager searchManager = (SearchManager) HomeActivity.this.getSystemService(Context.SEARCH_SERVICE);

            SearchView searchView = null;
            if (searchItem != null) {
                searchView = (SearchView) searchItem.getActionView();
            }
            if (searchView != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(HomeActivity.this.getComponentName()));
            }

            return true;



	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
	}


    private void navigateFragment(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);
        switch (position) {
            case  R.id.drawer_home:
                fragment = HomeFragment.newInstance();
                title = getString(R.string.title_home);
                break;
            case R.id.drawer_store:
                fragment = StoreFragment.newInstance();
                title = getString(R.string.title_store);
                break;
            case R.id.drawer_my_book:
                fragment = StoreFragment.newInstance();
                title = getString(R.string.title_my_book);
                break;
            case R.id.drawer_settings:
                fragment = StoreFragment.newInstance();
                title = getString(R.string.title_settings);
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        mNavItemId = menuItem.getItemId();
        navigateFragment(mNavItemId);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    @Override
    public void onHomeItemSelected(int position, boolean isDownloadedBook) {
       // navigationView.getMenu().getItem(3).setChecked(true);


    }

    @Override
    public void onOptionButtonClicked(int buttonIndex) {
        MenuItem menuItem=navigationView.getMenu().getItem(buttonIndex);
        menuItem.setChecked(true);
        mNavItemId = menuItem.getItemId();
        navigateFragment(mNavItemId);
    }

    @Override
    public void onStoreBookSelected(int position) {

    }
}
