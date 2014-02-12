package com.geobolivia.carnaval_oruro;

import org.osmdroid.views.MapView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class infoFragment extends Fragment {
	public infoFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_info, container, false);
        MapView map = (MapView) getActivity().findViewById(R.id.openmapviewss);
	 	map.setVisibility(View.INVISIBLE);
	 	
        return rootView;
    }
}