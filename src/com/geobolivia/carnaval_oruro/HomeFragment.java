package com.geobolivia.carnaval_oruro;

import org.osmdroid.views.MapView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

public class HomeFragment extends Fragment {
	ImageButton imageButton;
	
	public HomeFragment(){}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
		View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        MapView map = (MapView) getActivity().findViewById(R.id.openmapviewss);
	 	map.setVisibility(View.INVISIBLE);
	 	
	 	imageButton = (ImageButton) rootView.findViewById(R.id.imageButtonLp);
		imageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Toast.makeText(getActivity(),
					"ImageButton (selector) is clicked!",
					Toast.LENGTH_SHORT).show();
				
				((MainActivity)getActivity()).displayView(2); 
			}
		});
		
        return rootView;
    }
}