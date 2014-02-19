package com.geobolivia.carnaval_oruro;

import java.io.File;
import java.util.ArrayList;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.geobolivia.slider_menu.adapter.TitleNavigationAdapter;
import com.geobolivia.slider_menu.model.SpinnerNavItem;

public class dakarFragment extends Fragment  implements ActionBar.OnNavigationListener{
	MapView map;
	public static KmlDocument kmlDocument;
	
	// Progress Dialog
    private ProgressDialog pDialog;
	
	String kmlDakar1 = "dakar_albergues_camping.kml";
	String kmlDakar2 = "dakar_banca_suroeste.kml";
	String kmlDakar3 = "dakar_hospedaje_suroeste3.kml";
	String kmlDakar4 = "dakar_restaurantes_suroeste.kml";
	String kmlDakar5 = "dakar_salud_suroeste.kml";
	String kmlDakar6 = "dakar_vida_nocturna_suroeste.kml";
	
	String kmlfile;
	int icon;
	
	
	public dakarFragment(){}
	
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
		navSpinner.add(new SpinnerNavItem("dakar1", R.drawable.ic_pages));
		navSpinner.add(new SpinnerNavItem("dakar2", R.drawable.ic_pages));
		navSpinner.add(new SpinnerNavItem("dakar3", R.drawable.ic_pages));
		navSpinner.add(new SpinnerNavItem("dakar4", R.drawable.ic_pages));
		navSpinner.add(new SpinnerNavItem("dakar5", R.drawable.ic_pages));
		navSpinner.add(new SpinnerNavItem("dakar6", R.drawable.ic_pages));
		navSpinner.add(new SpinnerNavItem("Ninguno", R.drawable.ic_pages));
		// title drop down adapter
		adapter2 = new TitleNavigationAdapter(this.getActivity(), navSpinner);
		
		// assigning the spinner navigation
		actionBar.setListNavigationCallbacks(adapter2, this);
		
		// Changing the action bar icon
		actionBar.setIcon(R.drawable.lpicondef);
	}
	
	public void addRoute(){
	    // References Points
	    GeoPoint startPoint = new GeoPoint(-20.459662, -66.822918);	    
	    //GeoPoint finishPoint = new GeoPoint(-16.499606, -68.124513);
	    
	    IMapController mapController = map.getController();
	    mapController.setZoom(15);
	    mapController.setCenter(startPoint);
	    
	    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
	    waypoints.add(startPoint);
	    //waypoints.add(finishPoint);
	    
	    
	    // ADD CUSTOMIZE POINTS AND BUBBLES
	    final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
	    ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodes = new 
	    		ItemizedOverlayWithBubble<ExtendedOverlayItem>(getActivity(), roadItems, map);
	    
	    Drawable marker;
		String title = "";
		String desc = "";
		
		for (int i = 0; i < waypoints.size(); i++) {
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
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {	
		switch (itemPosition) {
		case 0:
			/*addkmlfile(kmlDakar1, R.drawable.hotel_0star);		
			addkmlfile(kmlDakar2, R.drawable.marker_node);
			addkmlfile(kmlDakar3, R.drawable.marker_poi);
			addkmlfile(kmlDakar4, R.drawable.marker_poi_flickr);
			addkmlfile(kmlDakar5, R.drawable.marker_poi_picasa_24);
			addkmlfile(kmlDakar6, R.drawable.restaurant_white);*/
			break;
		case 1:
			addkmlfile(kmlDakar1, R.drawable.hotel_0star);
			break;
		case 2:
			addkmlfile(kmlDakar2, R.drawable.marker_node);
			break;
		case 3:
			addkmlfile(kmlDakar3, R.drawable.marker_poi);
			break;
		case 4:
			addkmlfile(kmlDakar4, R.drawable.marker_poi_flickr);			
			break;
		case 5:
			addkmlfile(kmlDakar5, R.drawable.marker_poi_picasa_24);
			break;
		case 6:
			addkmlfile(kmlDakar6, R.drawable.restaurant_white);
			break;
		case 7:
			map.removeAllViews();
			map.getOverlays().clear();
			map.invalidate();
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

} 