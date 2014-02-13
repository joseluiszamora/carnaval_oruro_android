package com.geobolivia.carnaval_oruro;

import java.io.File;
import java.util.ArrayList;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geobolivia.slider_menu.adapter.TitleNavigationAdapter;
import com.geobolivia.slider_menu.model.SpinnerNavItem;

public class OruroFragment extends Fragment implements ActionBar.OnNavigationListener{
	//  MAPS
	private MapView map;
	protected FolderOverlay kmlOverlay1;
	protected FolderOverlay kmlOverlay2;
	public static KmlDocument kmlDocument;
	
	public OruroFragment(){}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	 	View rootView = inflater.inflate(R.layout.fragment_oruro, container, false);
        
	 	map = (MapView) getActivity().findViewById(R.id.openmapviewss);
	 	map.setVisibility(View.VISIBLE);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setClickable(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);

        /*GeoPoint startPoint = new GeoPoint(-16.488880, -68.143616);
 	    IMapController mapController = map.getController();
 	    mapController.setZoom(10);
 	    mapController.setCenter(startPoint);*/
        

       map.invalidate();

 	   kmlDocument = new KmlDocument();
 	   
       // ADD ROUTE AND START, FINISH POINTS
       addRoute();
        			
       // ADD ACTION BAR
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
		navSpinner.add(new SpinnerNavItem("Todos", R.drawable.ic_pages));
		navSpinner.add(new SpinnerNavItem("Pasarelas", R.drawable.bridge_old_white));
		navSpinner.add(new SpinnerNavItem("Restaurantes", R.drawable.restaurant_white));
		navSpinner.add(new SpinnerNavItem("Ninguno", 0));

		// title drop down adapter
		adapter2 = new TitleNavigationAdapter(this.getActivity(), navSpinner);
		
		// assigning the spinner navigation
		actionBar.setListNavigationCallbacks(adapter2, this);
		// Changing the action bar icon
		actionBar.setIcon(R.drawable.oricondef);
	}
	
	public void addRoute(){
		// ADD ROUTE
        GeoPoint startPoint = new GeoPoint(-17.961292 , -67.106058);
        GeoPoint finishPoint = new GeoPoint(-17.968015, -67.118502);
        
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        mapController.setCenter(startPoint);
        
        RoadManager roadManager = new OSRMRoadManager();
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        // Finish Points Oruro
        waypoints.add(new GeoPoint(-17.971635, -67.108864));
        waypoints.add(new GeoPoint(-17.970154, -67.114486));
        waypoints.add(new GeoPoint(-17.969127, -67.117911));
        waypoints.add(new GeoPoint(-17.968290, -67.117622));
        waypoints.add(new GeoPoint(-17.968015, -67.118502));
        waypoints.add(finishPoint);
                
        
        ArrayList<GeoPoint> waypointsSF = new ArrayList<GeoPoint>();
        waypointsSF.add(startPoint);
        waypointsSF.add(finishPoint);
        
        Road road = roadManager.getRoad(waypoints);
        PathOverlay roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
        map.getOverlays().add(roadOverlay);
        map.invalidate();	
        
        // ADD CUSTOMIZE POINTS AND BUBBLES
        final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
        ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(getActivity(), roadItems, map);
        
        Drawable marker;
		String title;
		String desc;
		
		for (int i = 0; i < waypointsSF.size(); i++) {
			if (i == 0){
				title = "Punto de Partida";
				desc = "Dirección";
				marker = getResources().getDrawable(R.drawable.start);
			}else{
				title = "Punto de Llegada";
				desc = "Dirección";
				marker = getResources().getDrawable(R.drawable.finish);
			}
			ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(title, desc, waypoints.get(i), getActivity());
            nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            nodeMarker.setMarker(marker);
            roadNodes.addItem(nodeMarker);
		}
        
        map.getOverlays().add(roadNodes);
        map.invalidate();
	}
	
	public void addkmlfile(String kmlFile, int icon){
		Log.d("CordovaLog", "001>>> " + kmlFile);
		Log.d("CordovaLog", "002>>> " + icon);
		// Add KML with POI on the Map
		File file = kmlDocument.getDefaultPathForAndroid(kmlFile);
		//
		//Log.d("CordovaLog", "004>>> " + file.toString());
		boolean ok = kmlDocument.parseFile(file);
		Log.d("CordovaLog", "003>>> " + ok);
		Drawable defaultMarker = getResources().getDrawable(icon);
		if (ok){
			if (kmlFile.equals("or_restaurants.kml")) {
				kmlOverlay2 = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(getActivity(), map, defaultMarker, kmlDocument, false);
				map.getOverlays().add(kmlOverlay2);
			}else{
				kmlOverlay1 = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(getActivity(), map, defaultMarker, kmlDocument, false);
				map.getOverlays().add(kmlOverlay1);
			}

			map.invalidate();
			if (kmlDocument.kmlRoot.mBB != null){
				map.getController().setCenter(new GeoPoint(
					kmlDocument.kmlRoot.mBB.getLatSouthE6()+kmlDocument.kmlRoot.mBB.getLatitudeSpanE6()/2, 
					kmlDocument.kmlRoot.mBB.getLonWestE6()+kmlDocument.kmlRoot.mBB.getLongitudeSpanE6()/2)
				);
			}
		}
	}
	
	public void removeAllKml(){
		map.removeAllViews();
		map.getOverlays().remove(kmlOverlay1);
		map.getOverlays().remove(kmlOverlay2);
		map.invalidate();
	}
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {	
		String kmlPasarela = "or_pasarela.kml";
		String kmlRestaurant = "or_restaurants.kml";
		removeAllKml();
		
		switch (itemPosition) {
		case 0:
			addkmlfile(kmlPasarela, R.drawable.bridge_old);
			addkmlfile(kmlRestaurant, R.drawable.restaurant);	
			break;
		case 1:
			addkmlfile(kmlPasarela, R.drawable.bridge_old);
			break;
		case 2:
			addkmlfile(kmlRestaurant, R.drawable.restaurant);
			break;
		case 3:
			Log.d("CordovaLog", "Nothing");
			break;
		default:
			break;
		}
		
		return false;
	}
}