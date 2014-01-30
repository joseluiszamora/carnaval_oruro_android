package com.geobolivia.carnaval_oruro;

import java.io.File;
import java.util.ArrayList;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.overlays.DefaultInfoWindow;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {
	MapView map;
	protected FolderOverlay kmlOverlay; //root container of overlays from KML reading
	public static KmlDocument kmlDocument;
	
	@Override protected void onCreate(Bundle savedInstanceState) {
		
		//Introduction
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		

		map = (MapView) findViewById(R.id.openmapview);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setClickable(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        
        
        kmlDocument = new KmlDocument(); //made static to pass between activities
                
        // start point Oruro
        GeoPoint startPoint = new GeoPoint(-17.961292 , -67.106058);
        
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        mapController.setCenter(startPoint);
        
        RoadManager roadManager = new OSRMRoadManager();
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        // Finish Points Oruro
        //waypoints.add(new GeoPoint(-17.971635, -67.108864));
        //waypoints.add(new GeoPoint(-17.970154, -67.114486));
        //waypoints.add(new GeoPoint(-17.969127, -67.117911));
        //waypoints.add(new GeoPoint(-17.968290, -67.117622));
        //waypoints.add(new GeoPoint(-17.968015, -67.118502));
        waypoints.add(new GeoPoint(-17.967632, -67.119584));
                
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
        
   
		//10. Loading KML content
        
		String url = "http://mapsengine.google.com/map/kml?mid=z6IJfj90QEd4.kcfEKhi8r5LQ";
		KmlDocument kmlDocument = new KmlDocument();
		boolean ok = kmlDocument.parseUrl(url);
		//File file = kmlDocument.getDefaultPathForAndroid("my_routes.kml");
		//boolean ok = kmlDocument.parseFile(file);
		Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_kml_point);
		if (ok){
			FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(this, map, defaultMarker, kmlDocument, false);
			map.getOverlays().add(kmlOverlay);
			map.invalidate();
			if (kmlDocument.kmlRoot.mBB != null){
				//map.zoomToBoundingBox(kmlRoot.mBB); => not working in onCreate - this is a well-known osmdroid bug. 
				//Workaround:
				map.getController().setCenter(new GeoPoint(
						kmlDocument.kmlRoot.mBB.getLatSouthE6()+kmlDocument.kmlRoot.mBB.getLatitudeSpanE6()/2, 
						kmlDocument.kmlRoot.mBB.getLonWestE6()+kmlDocument.kmlRoot.mBB.getLongitudeSpanE6()/2));
			}
		}
		
		
		
		//11. Grab overlays in KML structure, save KML document locally
		/*if (kmlDocument.kmlRoot != null){
			kmlDocument.kmlRoot.addOverlay(roadOverlay, kmlDocument);
			kmlDocument.kmlRoot.addOverlay(roadNodes, kmlDocument);
			File localFile = kmlDocument.getDefaultPathForAndroid("my_routes.kml");
			kmlDocument.saveAsKML(localFile);
		}*/
	}
	
	//7. Customizing the bubble behaviour
	class CustomInfoWindow extends DefaultInfoWindow {
		POI selectedPoi;
		public CustomInfoWindow(MapView mapView) {
			super(R.layout.bonuspack_bubble, mapView);
			Button btn = (Button)(mView.findViewById(R.id.bubble_moreinfo));
			btn.setOnClickListener(new View.OnClickListener() {
			    public void onClick(View view) {
			        if (selectedPoi.mUrl != null){
			            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(selectedPoi.mUrl));
			            view.getContext().startActivity(myIntent);
			        } else {
			        	Toast.makeText(view.getContext(), "Button clicked", Toast.LENGTH_LONG).show();
			        }
			    }
			});
		}
		@Override public void onOpen(Object item){
			super.onOpen(item);
			ExtendedOverlayItem eItem = (ExtendedOverlayItem)item;
			selectedPoi = (POI)eItem.getRelatedObject();
			mView.findViewById(R.id.bubble_moreinfo).setVisibility(View.VISIBLE);
			
			//8. put thumbnail image in bubble, fetching the thumbnail in background:
			if (selectedPoi.mThumbnailPath != null){
				ImageView imageView = (ImageView)mView.findViewById(R.id.bubble_image);
				selectedPoi.fetchThumbnailOnThread(imageView);
			}
		}	
	}
	
	
	void setViewOn(BoundingBoxE6 bb){
		if (bb != null){
			map.zoomToBoundingBox(bb);
		}
	}
	
	void getKml(String uri, boolean onCreate){
		Log.d("CordovaLog", " 7777777777777777777777");
		//TODO: use an Async task
		KmlDocument mKmlDocument = new KmlDocument();
		boolean ok;
		if (uri.startsWith("file:/")){
			uri = uri.substring("file:/".length());
			File file = new File(uri);
			ok = mKmlDocument.parseFile(file);
		} else  {
			ok = mKmlDocument.parseUrl(uri);
		}
		
		if (!ok){
			Toast.makeText(this, "Sorry, unable to read "+uri, Toast.LENGTH_SHORT).show();
		}
		updateUIWithKml();
		if (mKmlDocument.kmlRoot != null && mKmlDocument.kmlRoot.mBB != null){
				if (!onCreate)
					setViewOn(mKmlDocument.kmlRoot.mBB); 
				else  //KO in onCreate - Workaround:
					map.getController().setCenter(new GeoPoint(
							mKmlDocument.kmlRoot.mBB.getLatSouthE6()+mKmlDocument.kmlRoot.mBB.getLatitudeSpanE6()/2, 
							mKmlDocument.kmlRoot.mBB.getLonWestE6()+mKmlDocument.kmlRoot.mBB.getLongitudeSpanE6()/2));
		}
	}
	
	void updateUIWithKml(){
		Log.d("CordovaLog", " UPDATEEEEEEEEEEEEEEEEEEEE");
		if (kmlOverlay != null)
			map.getOverlays().remove(kmlOverlay);
		if (kmlDocument.kmlRoot != null){
			Log.d("CordovaLog", "---- " + kmlDocument.kmlRoot);
			Log.d("CordovaLog", " UPDATEEE 2222222");
			Drawable defaultKmlMarker = getResources().getDrawable(R.drawable.marker_kml_point);
			kmlOverlay = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(this, map, defaultKmlMarker, kmlDocument, true);
			Log.d("CordovaLog", "++++ " + kmlOverlay);
			map.getOverlays().add(kmlOverlay);
		}
		map.invalidate();
	}
}
