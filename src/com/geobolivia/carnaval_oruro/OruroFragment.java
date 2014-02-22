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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geobolivia.slider_menu.adapter.TitleNavigationAdapter;
import com.geobolivia.slider_menu.model.SpinnerNavItem;

public class OruroFragment extends Fragment  implements ActionBar.OnNavigationListener{
	MapView map;
	public static KmlDocument kmlDocument;
	
	// Progress Dialog
    private ProgressDialog pDialog;

	String kmlpasarela = "or_pasarela.kml";
	String kmlrestaurant = "or_restaurants.kml";
	
	String kmlfile;
	int icon;
	
	Drawable defaultMarker;
	
	public OruroFragment(){}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dakar, container, false);
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

	@SuppressLint("NewApi")
	private void addActionBar(){
		
		ActionBar actionBar;
		ArrayList<SpinnerNavItem> navSpinner;
		TitleNavigationAdapter adapter2;
		actionBar = getActivity().getActionBar();
		actionBar.removeAllTabs();
		actionBar.setDisplayShowTitleEnabled(true);
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
		GeoPoint startPoint = new GeoPoint(-17.961292 , -67.106058);
        GeoPoint finishPoint = new GeoPoint(-17.968015, -67.118502);
        
        IMapController mapController = map.getController();
        mapController.setZoom(15);
        mapController.setCenter(startPoint);
        
        RoadManager roadManager = new OSRMRoadManager();
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(new GeoPoint(-17.971635, -67.108864));
        waypoints.add(new GeoPoint(-17.970154, -67.114486));
        waypoints.add(new GeoPoint(-17.969127, -67.117911));
        waypoints.add(new GeoPoint(-17.968290, -67.117622));
        waypoints.add(new GeoPoint(-17.968015, -67.118502));
        waypoints.add(finishPoint);

        ArrayList<GeoPoint> waypointsSF = new ArrayList<GeoPoint>();
        waypointsSF.add(startPoint);
        //waypointsSF.add(finishPoint);
        
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
	
	
	public void addkmlfile(String kmlFiled, int icond){
		// Add KML with POI on the Map
		/*File file = kmlDocument.getDefaultPathForAndroid(kmlFile);
		boolean ok = kmlDocument.parseFile(file);
		*/
		
		/*
		Log.d("CordovaLog", "KmlProvider.parseFile:"+file.getAbsolutePath());
		KmlSaxHandler handler = new KmlSaxHandler(file.getAbsolutePath());
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(stream, handler);
			stream.close();
		} catch (Exception e){
			e.printStackTrace();
			handler.mKmlRoot = null;
		}
		Log.d("CordovaLog", "KmlProvider.parseFile - end");
		//kmlRoot = handler.mKmlRoot;
		//return (handler.mKmlRoot != null);
		*/
		
		/*
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
		*/
		kmlfile = kmlFiled;
		icon = icond;
		
		pDialog = new ProgressDialog(getActivity());
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage("Actualizando...");
		pDialog.setCancelable(false);
		pDialog.setMax(100);
		
		addKmlAsynck updateWork = new addKmlAsynck();
		updateWork.execute();
		
		//MyAsyncTask asyncthis = new MyAsyncTask(kmlFile, icon);
		//asyncthis.execute();
	}
	
	public void addALLkmlFiles(){
		pDialog = new ProgressDialog(getActivity());
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage("Actualizando...");
		pDialog.setCancelable(false);
		pDialog.setMax(100);
		addAllKmlAsynck updateWork = new addAllKmlAsynck();
		updateWork.execute();
		
		//addRoute();
	}
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {	
		switch (itemPosition) {
		case 0:
			addALLkmlFiles();
			addRoute();
			break;
		case 1:
			addkmlfile(kmlpasarela, R.drawable.marker_poi_picasa_24);
			addRoute();
			break;
		case 2:
			addkmlfile(kmlrestaurant, R.drawable.restaurant_white);
			addRoute();
			break;
		case 3:
			map.removeAllViews();
			map.getOverlays().clear();
			map.invalidate();
			addRoute();
			break;
		default:
			break;
		}
		return false;
	}

	private class addKmlAsynck extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
        	File file = kmlDocument.getDefaultPathForAndroid(kmlfile);
    		boolean ok = kmlDocument.parseFile(file);
    		Drawable defaultMarker = getResources().getDrawable(icon);
    		if (ok){
    			FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(getActivity(), 
    					map, defaultMarker, kmlDocument, false);
				map.getOverlays().add(kmlOverlay);
    		}
			return true;
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			int progreso = values[0].intValue();
			pDialog.setProgress(progreso);
		}
		@Override
		protected void onPreExecute() {
			map.removeAllViews();
			map.getOverlays().clear();
			map.invalidate();
			pDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					addKmlAsynck.this.cancel(true);
				}
			});
			pDialog.setProgress(0);
			pDialog.show();
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if(result) {
				map.invalidate();
	        	if (kmlDocument.kmlRoot.mBB != null){
					map.getController().setCenter(new GeoPoint(
						kmlDocument.kmlRoot.mBB.getLatSouthE6()+kmlDocument.kmlRoot.mBB.getLatitudeSpanE6()/2, 
						kmlDocument.kmlRoot.mBB.getLonWestE6()+kmlDocument.kmlRoot.mBB.getLongitudeSpanE6()/2)
					);
				}
				pDialog.dismiss();
			}
		}		
		@Override
		protected void onCancelled() {}
	}

	
	
	
	private class addAllKmlAsynck extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			File file;
			boolean ok;
			FolderOverlay kmlOverlay;
			
			try {
				file = kmlDocument.getDefaultPathForAndroid(kmlpasarela);
	    		ok = kmlDocument.parseFile(file);
	    		defaultMarker = getResources().getDrawable(R.drawable.bridge_old);
	    		if (ok){
	    			kmlOverlay = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(getActivity(), 
	    					map, defaultMarker, kmlDocument, false);
					map.getOverlays().add(kmlOverlay);
	    		}
	    		
	    		
	    		file = kmlDocument.getDefaultPathForAndroid(kmlrestaurant);
	    		ok = kmlDocument.parseFile(file);
	    		defaultMarker = getResources().getDrawable(R.drawable.restaurant);
	    		if (ok){
	    			kmlOverlay = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(getActivity(), 
	    					map, defaultMarker, kmlDocument, false);
					map.getOverlays().add(kmlOverlay);
	    		}
			} catch (Exception e) {
				Log.d("CordovaLog", "fail add kml");
			}
    		
			return true;
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			int progreso = values[0].intValue();
			pDialog.setProgress(progreso);
		}
		@Override
		protected void onPreExecute() {
			map.removeAllViews();
			map.getOverlays().clear();
			map.invalidate();
			
			pDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					addAllKmlAsynck.this.cancel(true);
				}
			});
			pDialog.setProgress(0);
			pDialog.show();
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if(result) {
				try {
					map.invalidate();
		        	if (kmlDocument.kmlRoot.mBB != null){
						map.getController().setCenter(new GeoPoint(
							kmlDocument.kmlRoot.mBB.getLatSouthE6()+kmlDocument.kmlRoot.mBB.getLatitudeSpanE6()/2, 
							kmlDocument.kmlRoot.mBB.getLonWestE6()+kmlDocument.kmlRoot.mBB.getLongitudeSpanE6()/2)
						);
					}
				} catch (Exception e) {
					Log.d("CordovaLog", "fail add kml");
				}
				pDialog.dismiss();
			}
		}
		@Override
		protected void onCancelled() {}
	}
	
	
} 