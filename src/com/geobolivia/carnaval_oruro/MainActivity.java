package com.geobolivia.carnaval_oruro;

import java.util.ArrayList;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		MapView map = (MapView) findViewById(R.id.openmapview);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setClickable(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        
                
        // start point Oruro
        GeoPoint startPoint = new GeoPoint(-17.961292 , -67.106058);
        
        // start point La Paz
        //GeoPoint startPoint = new GeoPoint(-16.488880, -68.143616);
        
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
        waypoints.add(new GeoPoint(-17.967632, -67.119584));
                
        // Finish Points La Paz
     // waypoints.add(new GeoPoint(-16.489893, -68.142333));
     // waypoints.add(new GeoPoint(-16.491701, -68.138359));
     // waypoints.add(new GeoPoint(-16.493390, -68.137127));
     // waypoints.add(new GeoPoint(-16.496091, -68.136774));
     // waypoints.add(new GeoPoint(-16.497683, -68.136095));
     // waypoints.add(new GeoPoint(-16.498189, -68.135542));
     // waypoints.add(new GeoPoint(-16.498985, -68.134033));
     // waypoints.add(new GeoPoint(-16.500673, -68.130537));
     // waypoints.add(new GeoPoint(-16.500691, -68.126953));
     // waypoints.add(new GeoPoint(-16.499606, -68.124513));
        
        Road road = roadManager.getRoad(waypoints);
        Log.d("CordovaLog", ">>**>>>>>> " + road.mNodes.size());
        PathOverlay roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
        map.getOverlays().add(roadOverlay);
        map.invalidate();
        
        
        
        //* ADD CUSTOMIZE POINTS AND BUBBLES
        final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
		ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, map);
		Drawable marker = getResources().getDrawable(R.drawable.marker_poi_picasa_24);
		
        for (GeoPoint geoPoint : waypoints) {
    		ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem("Insert title Here", "Insert Amazing Description here", geoPoint, this);
            nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
            nodeMarker.setMarker(marker);
            roadNodes.addItem(nodeMarker);
		}
        
        map.getOverlays().add(roadNodes);
        map.invalidate();
        
        
        /* ROAD MANAGER ONLINE
        RoadManager roadManager2 = new OSRMRoadManager();
        ArrayList<GeoPoint> waypoints2 = new ArrayList<GeoPoint>();
        waypoints2.add(startPoint);
        waypoints2.add(new GeoPoint(48.4, -1.9)); //end point
        Road road2 = roadManager2.getRoad(waypoints2);
        PathOverlay roadOverlay2 = RoadManager.buildRoadOverlay(road2, map.getContext());
        map.getOverlays().add(roadOverlay2);
        map.invalidate();*/
	}
}