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

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OruroFragment  extends Fragment {
	private MapView map;
	protected FolderOverlay kmlOverlay;
	public static KmlDocument kmlDocument;
	
	public OruroFragment(){}
	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
		 	View rootView = inflater.inflate(R.layout.fragment_oruro, container, false);
			map = (MapView) rootView.findViewById(R.id.openmapview);
	        map.setTileSource(TileSourceFactory.MAPNIK);
	        map.setClickable(true);
	        map.setBuiltInZoomControls(true);
	        map.setMultiTouchControls(true);
	        map.setUseDataConnection(true);

	        kmlDocument = new KmlDocument(); 
	        String kmlFile = "vturismo-pasarela.kml";
	        
	        // relative Points
	        GeoPoint startPoint = new GeoPoint(-17.961292 , -67.106058);
	        GeoPoint finishPoint = new GeoPoint(-17.967632, -67.119584);
	        
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
	        Log.d("CordovaLog", ">>**>>>>>> " + road.mNodes.size());
	        PathOverlay roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
	        map.getOverlays().add(roadOverlay);
	        map.invalidate();	
	        
	        
	        // ADD CUSTOMIZE POINTS AND BUBBLES
	        final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
	        ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(getActivity(), roadItems, map);
	        
	        Drawable marker;
			String title = "";
			String desc = "";
			
			for (int i = 0; i < waypointsSF.size(); i++) {
				if (i == 0){
					title = "Punto de Partida";
					desc = "Dirección";
					marker = getResources().getDrawable(R.drawable.flaggreen);
				}else{
					title = "Punto de Llegada";
					desc = "Dirección";
					marker = getResources().getDrawable(R.drawable.flagblue);
				}
				ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(title, desc, waypointsSF.get(i), getActivity());
	            nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
	            nodeMarker.setMarker(marker);
	            roadNodes.addItem(nodeMarker);
			}
	        
	        map.getOverlays().add(roadNodes);
	        map.invalidate();
	        
	        //. Loading KML content
			File file = kmlDocument.getDefaultPathForAndroid(kmlFile);
			boolean ok = kmlDocument.parseFile(file);
			Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_kml_point);
			if (ok){
				FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(getActivity(), map, defaultMarker, kmlDocument, false);
				map.getOverlays().add(kmlOverlay);
				map.invalidate();
				if (kmlDocument.kmlRoot.mBB != null){
					map.getController().setCenter(new GeoPoint(
						kmlDocument.kmlRoot.mBB.getLatSouthE6()+kmlDocument.kmlRoot.mBB.getLatitudeSpanE6()/2, 
						kmlDocument.kmlRoot.mBB.getLonWestE6()+kmlDocument.kmlRoot.mBB.getLongitudeSpanE6()/2)
					);
				}
			}
	        
	        return rootView;
	    }
	}