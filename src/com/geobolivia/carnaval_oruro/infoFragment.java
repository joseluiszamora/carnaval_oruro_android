package com.geobolivia.carnaval_oruro;

import java.util.ArrayList;

import org.osmdroid.views.MapView;

import com.geobolivia.slider_menu.adapter.TitleNavigationAdapter;
import com.geobolivia.slider_menu.model.SpinnerNavItem;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class infoFragment extends Fragment  implements ActionBar.OnNavigationListener{
	public infoFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);
        MapView map = (MapView) getActivity().findViewById(R.id.openmapviewss);
	 	map.setVisibility(View.INVISIBLE);
	 	addActionBar();
        return rootView;
    }
	
	@SuppressLint("NewApi")
	private void addActionBar(){
		// action bar
		ActionBar actionBar;
		// Title navigation Spinner data
		ArrayList<SpinnerNavItem> navSpinner;
		// Navigation adapter
		TitleNavigationAdapter adapter2;
		// enabling action bar app icon and behaving it as toggle button
		actionBar = getActivity().getActionBar();
		actionBar.removeAllTabs();
		
		// Hide the action bar title
		actionBar.setDisplayShowTitleEnabled(true);

		// Enabling Spinner dropdown navigation
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		// Spinner title navigation data
		navSpinner = new ArrayList<SpinnerNavItem>();
		navSpinner.add(new SpinnerNavItem("Información", R.drawable.ic_pages));

		// title drop down adapter
		adapter2 = new TitleNavigationAdapter(this.getActivity(), navSpinner);
		
		// assigning the spinner navigation
		actionBar.setListNavigationCallbacks(adapter2, this);
		// Changing the action bar icon
		actionBar.setIcon(R.drawable.ico_info);
	}

	@Override
	public boolean onNavigationItemSelected(int arg0, long arg1) {
		// TODO Auto-generated method stub
		return false;
	}
} 