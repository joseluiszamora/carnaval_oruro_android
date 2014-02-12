package com.geobolivia.carnaval_oruro;

import org.osmdroid.views.MapView;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class HomeFragment extends Fragment implements ActionBar.OnNavigationListener{
	public HomeFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
		View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        MapView map = (MapView) getActivity().findViewById(R.id.openmapviewss);
	 	map.setVisibility(View.INVISIBLE);
		
	 	// Select Oruro
 	 	ImageButton imageButtonOR = (ImageButton) rootView.findViewById(R.id.imageButtonOR);
 	 	imageButtonOR.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				((MainActivity)getActivity()).displayView(1); 
 			}
 		});
		
 	 	// Select La paz
	 	ImageButton imageButtonLP = (ImageButton) rootView.findViewById(R.id.imageButtonLP);
	 	imageButtonLP.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((MainActivity)getActivity()).displayView(2); 
			}
		});
		
		addActionBar();
		
        return rootView;
    }
	
	@SuppressLint("NewApi")
	private void addActionBar(){
		// action bar
		ActionBar actionBar;
		// enabling action bar app icon and behaving it as toggle button
		actionBar = getActivity().getActionBar();
		actionBar.removeAllTabs();
		// Hide the action bar title
		actionBar.setDisplayShowTitleEnabled(true);
		// Enabling Spinner dropdown navigation
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		// Changing the action bar icon
		actionBar.setIcon(R.drawable.ic_home);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}
}