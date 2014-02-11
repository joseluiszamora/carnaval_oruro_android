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

import com.geobolivia.slider_menu.adapter.TitleNavigationAdapter;
import com.geobolivia.slider_menu.model.SpinnerNavItem;

import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CochabambaFragment extends Fragment implements ActionBar.OnNavigationListener{
	private MapView map;
	protected FolderOverlay kmlOverlay; //root container of overlays from KML reading
	public static KmlDocument kmlDocument;
	
	public CochabambaFragment(){}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cochabamba, container, false);

        map = (MapView) getActivity().findViewById(R.id.openmapviewss);
        map.setVisibility(View.VISIBLE);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setClickable(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);

        kmlDocument = new KmlDocument(); 
        
        // ADD ROUTE AND START, FINISH POINTS
        addRoute();
        			
		// ADD ACTION BAR
        addActionBar();
        
        return rootView;
	}
	
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
		navSpinner.add(new SpinnerNavItem("Ninguno", R.drawable.ic_pages));
		
		// title drop down adapter
		adapter2 = new TitleNavigationAdapter(this.getActivity(), navSpinner);
		
		// assigning the spinner navigation
		actionBar.setListNavigationCallbacks(adapter2, this);
		
		// Changing the action bar icon
		// actionBar.setIcon(R.drawable.ico_actionbar);
	}
	
	public void addRoute(){
		kmlDocument = new KmlDocument();
		
        // References Points
        GeoPoint startPoint = new GeoPoint(-17.3896723364111, -66.14298435150044);
        GeoPoint finishPoint = new GeoPoint(-17.3820379695891, -66.15130424327675);
        
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        mapController.setCenter(startPoint);
        
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(finishPoint);
        
        // ADD CUSTOMIZE POINTS AND BUBBLES
        final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
        ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(getActivity(), roadItems, map);
        
        Drawable marker;
		String title = "";
		String desc = "";
		
		for (int i = 0; i < waypoints.size(); i++) {
			if (i == 0){
				title = "Punto de Partida";
				desc = "Dirección";
				marker = getResources().getDrawable(R.drawable.flaggreen);
			}else{
				title = "Punto de Llegada";
				desc = "Dirección";
				marker = getResources().getDrawable(R.drawable.flagblue);
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
		File file = kmlDocument.getDefaultPathForAndroid(kmlFile);
		boolean ok = kmlDocument.parseFile(file);
		Drawable defaultMarker = getResources().getDrawable(icon);
		if (ok){
			kmlOverlay = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(getActivity(), map, defaultMarker, kmlDocument, false);
			map.getOverlays().add(kmlOverlay);
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
		map.getOverlays().remove(kmlOverlay);
		map.invalidate();
	}
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		String kmlRoute = "cbba_corso_de_corsos.kml";
		removeAllKml();
		
		switch (itemPosition) {
		case 0:
			addkmlfile(kmlRoute, R.drawable.marker_poi_wikipedia_32);
			break;
		case 1:
			addkmlfile(kmlRoute, R.drawable.marker_kml_point);
			break;
		default:
			break;
		}
		
		return false;
	}
}