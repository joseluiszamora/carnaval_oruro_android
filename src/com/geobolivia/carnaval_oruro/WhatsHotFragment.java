package com.geobolivia.carnaval_oruro;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WhatsHotFragment extends Fragment {
	
	public WhatsHotFragment(){}
	private MapView map;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_whats_hot, container, false);

        map = (MapView) getActivity().findViewById(R.id.openmapviewss);
        
        GeoPoint startPoint = new GeoPoint(-16.488880, -68.143616);
	    IMapController mapController = map.getController();
	    mapController.setZoom(15);
	    mapController.setCenter(startPoint);
	    map.invalidate();
	    
        return rootView;
    }
}