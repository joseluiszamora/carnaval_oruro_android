package com.geobolivia.carnaval_oruro;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.geobolivia.slider_menu.adapter.NavDrawerListAdapter;
import com.geobolivia.slider_menu.adapter.TitleNavigationAdapter;
import com.geobolivia.slider_menu.model.NavDrawerItem;
import com.geobolivia.slider_menu.model.SpinnerNavItem;

public class MainActivity extends Activity {
	// action bar
	ActionBar actionBar;
	// Title navigation Spinner data
	private ArrayList<SpinnerNavItem> navSpinner;
	// Navigation adapter
	private TitleNavigationAdapter adapter2;
	
	
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
 
    // nav drawer title
    private CharSequence mDrawerTitle;
    // used to store app title
    private CharSequence mTitle;
    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
	
	@SuppressLint("NewApi")
	@Override protected void onCreate(Bundle savedInstanceState) {
		//Introduction
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// copy KML files to sdcard
		CopyAssets();

		mTitle = mDrawerTitle = getTitle();
		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
		// nav drawer icons from resources
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		// Home
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1)));
		// Oruro
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1)));
		// La Paz
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1)));
		// Santa Cruz
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1)));
		// Cochabamba
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1)));
		// What's hot, We  will add a counter here
		//navDrawerItems.add(new NavDrawerItem(navMenuTitles[5], navMenuIcons.getResourceId(5, -1), true, "50+"));
		

		// Recycle the typed array
		navMenuIcons.recycle();
		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setNavigationMode(0);

		
		/*
		// Hide the action bar title
		//actionBar.setDisplayShowTitleEnabled(false);

		// Enabling Spinner dropdown navigation
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		// Spinner title navigation data
		navSpinner = new ArrayList<SpinnerNavItem>();
		navSpinner.add(new SpinnerNavItem("Todos", R.drawable.ic_pages));
		navSpinner.add(new SpinnerNavItem("Comida", R.drawable.ic_home));
		navSpinner.add(new SpinnerNavItem("Entretenimiento", R.drawable.ic_menu_poi));
		navSpinner.add(new SpinnerNavItem("Servicios Higienicos", R.drawable.ic_photos));
		// title drop down adapter
		adapter2 = new TitleNavigationAdapter(getApplicationContext(), navSpinner);
		
		// assigning the spinner navigation
		
		actionBar.setListNavigationCallbacks(adapter2, null);

		// Changing the action bar icon
		// actionBar.setIcon(R.drawable.ico_actionbar);
		
			*/	
		
		
		
		
		
		
		
		
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
		) {
			public void onDrawerClosed(View view) {
				actionBar.setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				actionBar.setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}
	}
	
	public OnNavigationListener clicked123(){
		Integer bbb = this.getTaskId();
		Log.d("CordovaLog", "CLICKKKKKKKKKK" + bbb);
		return null;
	}
	
	

	/**
	 * Slide menu item click listener
	**/
	
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			Log.d("CordovaLog", "111111111");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}	
	}

	
	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 **/
	
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			fragment = new HomeFragment();
			break;
		case 1:
			fragment = new OruroFragment();
			break;
		case 2:
			fragment = new LaPazFragment();
			break;
		case 3:
			fragment = new SantaCruzFragment();
			break;
		case 4:
			fragment = new CochabambaFragment();
			break;
		case 5:
			fragment = new WhatsHotFragment();
			break;

		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		actionBar.setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	private void CopyAssets() {
		// create the folder if not exist
		File folder = new File(Environment.getExternalStorageDirectory() + "/kml");
		boolean success = true;
		if (!folder.exists()) {
			Log.e("CordovaLog", "folder not exist");
		    success = folder.mkdir();
		}
		if (success) {
			Log.e("CordovaLog", "folder created!");
		    // Do something on success
		} else {
			Log.e("CordovaLog", "folder not created!");
		    // Do something else on failure 
		}
		
		
	    AssetManager assetManager = getAssets(); 
	    String[] files = null; 
	    try { 
	        files = assetManager.list(""); 
	    } catch (IOException e) { 
	        Log.e("CordovaLog", e.getMessage()); 
	    } 
	    for(String filename : files) { 
	        InputStream in = null; 
	        OutputStream out = null; 
	        try {
	          in = assetManager.open(filename); 
	          out = new FileOutputStream("/sdcard/kml/" + filename); 
	          
	          copyFile(in, out); 
	          in.close(); 
	          in = null; 
	          out.flush(); 
	          out.close(); 
	          out = null; 
	        } catch(Exception e) { 
	            Log.e("tag", e.getMessage()); 
	        }        
	    } 
	} 
	
	private void copyFile(InputStream in, OutputStream out) throws IOException { 
	    byte[] buffer = new byte[1024]; 
	    int read; 
	    while((read = in.read(buffer)) != -1){ 
	      out.write(buffer, 0, read); 
	    } 
	}	
}