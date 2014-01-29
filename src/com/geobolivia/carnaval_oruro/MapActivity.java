package com.geobolivia.carnaval_oruro;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.Polyline;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.location.FlickrPOIProvider;
import org.osmdroid.bonuspack.location.GeoNamesPOIProvider;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.location.PicasaPOIProvider;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.overlays.MapEventsOverlay;
import org.osmdroid.bonuspack.overlays.MapEventsReceiver;
import org.osmdroid.bonuspack.overlays.Polygon;
import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.DirectedLocationOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MapActivity extends Activity implements MapEventsReceiver, LocationListener, SensorEventListener {
	protected MapView map;
	
	protected GeoPoint startPoint, destinationPoint;
	protected ArrayList<GeoPoint> viaPoints;
	protected static int START_INDEX=-2, DEST_INDEX=-1;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> itineraryMarkers; 
		//for departure, destination and viapoints
	protected ExtendedOverlayItem markerStart, markerDestination;
	DirectedLocationOverlay myLocationOverlay;
	//MyLocationNewOverlay myLocationNewOverlay;
	protected LocationManager locationManager;
	//protected SensorManager mSensorManager;
	//protected Sensor mOrientation;

	protected boolean mTrackingMode;
	Button mTrackingModeButton;
	float mAzimuthAngleSpeed = 0.0f;

	protected Polygon destinationPolygon; //enclosing polygon of destination location
	
	public static Road mRoad; //made static to pass between activities
	protected Polyline roadOverlay;
	protected ItemizedOverlayWithBubble<ExtendedOverlayItem> roadNodeMarkers;
	protected static final int ROUTE_REQUEST = 1;
	static final int OSRM=0, MAPQUEST_FASTEST=1, MAPQUEST_BICYCLE=2, MAPQUEST_PEDESTRIAN=3, GOOGLE_FASTEST=4;
	int whichRouteProvider;
	
	public static ArrayList<POI> mPOIs; //made static to pass between activities
	ItemizedOverlayWithBubble<ExtendedOverlayItem> poiMarkers;
	AutoCompleteTextView poiTagText;
	protected static final int POIS_REQUEST = 2;
	
	protected FolderOverlay kmlOverlay; //root container of overlays from KML reading
	protected static final int KML_TREE_REQUEST = 3;
	public static KmlDocument mKmlDocument = new KmlDocument(); //made static to pass between activities
	public static Stack<KmlFeature> mKmlStack = new Stack<KmlFeature>(); //passed between activities, top is the current KmlFeature to edit. 
	public static KmlFeature mKmlClipboard; //passed between activities. Folder for multiple items selection. 
	
	static String SHARED_PREFS_APPKEY = "OSMNavigator";
	static String PREF_LOCATIONS_KEY = "PREF_LOCATIONS";
	
	OnlineTileSourceBase MAPBOXSATELLITELABELLED;
	
	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
		
		//Integrating MapBox Satellite map, which is really an impressive service. 
		//We stole OSRM account at MapBox. Hope we will be forgiven... 
		/*MAPBOXSATELLITELABELLED = new XYTileSource("MapBoxSatelliteLabelled", ResourceProxy.string.mapquest_aerial, 1, 19, 256, ".png",
                "http://a.tiles.mapbox.com/v3/dennisl.map-6g3jtnzm/",
                "http://b.tiles.mapbox.com/v3/dennisl.map-6g3jtnzm/",
                "http://c.tiles.mapbox.com/v3/dennisl.map-6g3jtnzm/",
                "http://d.tiles.mapbox.com/v3/dennisl.map-6g3jtnzm/");*/
		TileSourceFactory.addTileSource(MAPBOXSATELLITELABELLED);
		
		map = (MapView) findViewById(R.id.map);
		
		String tileProviderName = prefs.getString("TILE_PROVIDER", "Mapnik");
		try {
			ITileSource tileSource = TileSourceFactory.getTileSource(tileProviderName);
			map.setTileSource(tileSource);
		} catch (IllegalArgumentException e) {
			map.setTileSource(TileSourceFactory.MAPNIK);
		}
		
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		IMapController mapController = map.getController();
		
		//To use MapEventsReceiver methods, we add a MapEventsOverlay:
		MapEventsOverlay overlay = new MapEventsOverlay(this, this);
		map.getOverlays().add(overlay);
		
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		//mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		//mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		//map prefs:
		mapController.setZoom(prefs.getInt("MAP_ZOOM_LEVEL", 5));
		mapController.setCenter(new GeoPoint((double)prefs.getFloat("MAP_CENTER_LAT", 48.5f), 
				(double)prefs.getFloat("MAP_CENTER_LON", 2.5f)));
		
		myLocationOverlay = new DirectedLocationOverlay(this);
		map.getOverlays().add(myLocationOverlay);

		if (savedInstanceState == null){
			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location == null)
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				//location known:
				onLocationChanged(location);
			} else {
				//no location known: hide myLocationOverlay
				myLocationOverlay.setEnabled(false);
			}
			startPoint = null;
			destinationPoint = null;
			viaPoints = new ArrayList<GeoPoint>();
		} else {
			myLocationOverlay.setLocation((GeoPoint)savedInstanceState.getParcelable("location"));
			//TODO: restore other aspects of myLocationOverlay...
			startPoint = savedInstanceState.getParcelable("start");
			destinationPoint = savedInstanceState.getParcelable("destination");
			viaPoints = savedInstanceState.getParcelableArrayList("viapoints");
		}
		
		//ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
		//map.getOverlays().add(scaleBarOverlay);
		
		// Itinerary markers:
		final ArrayList<ExtendedOverlayItem> waypointsItems = new ArrayList<ExtendedOverlayItem>();
		itineraryMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, 
				waypointsItems, 
				map, new ViaPointInfoWindow(R.layout.itinerary_bubble, map));
		map.getOverlays().add(itineraryMarkers);
		updateUIWithItineraryMarkers();
		
		//Tracking system:
		mTrackingModeButton = (Button)findViewById(R.id.buttonTrackingMode);
		mTrackingModeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				mTrackingMode = !mTrackingMode;
				updateUIWithTrackingMode();
			}
		});
		if (savedInstanceState != null){
			mTrackingMode = savedInstanceState.getBoolean("tracking_mode");
			updateUIWithTrackingMode();
		} else 
			mTrackingMode = false;
		
		AutoCompleteOnPreferences departureText = (AutoCompleteOnPreferences) findViewById(R.id.editDeparture);
		departureText.setPrefKeys(SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);
		
		Button searchDepButton = (Button)findViewById(R.id.buttonSearchDep);
		searchDepButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchButton(START_INDEX, R.id.editDeparture);
			}
		});
		
		AutoCompleteOnPreferences destinationText = (AutoCompleteOnPreferences) findViewById(R.id.editDestination);
		destinationText.setPrefKeys(SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);
		
		Button searchDestButton = (Button)findViewById(R.id.buttonSearchDest);
		searchDestButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				handleSearchButton(DEST_INDEX, R.id.editDestination);
			}
		});
		
		View expander = (View)findViewById(R.id.expander);
		expander.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				View searchPanel = (View)findViewById(R.id.search_panel);
				if (searchPanel.getVisibility() == View.VISIBLE){
					searchPanel.setVisibility(View.GONE);
				} else {
					searchPanel.setVisibility(View.VISIBLE);
				}
			}
		});
		View searchPanel = (View)findViewById(R.id.search_panel);
		searchPanel.setVisibility(prefs.getInt("PANEL_VISIBILITY", View.VISIBLE));

		registerForContextMenu(searchDestButton);
		//context menu for clicking on the map is registered on this button. 
		//(a little bit strange, but if we register it on mapView, it will catch map drag events)
		
		//Route and Directions
		whichRouteProvider = prefs.getInt("ROUTE_PROVIDER", OSRM);
		
		final ArrayList<ExtendedOverlayItem> roadItems = new ArrayList<ExtendedOverlayItem>();
    	roadNodeMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, roadItems, map);
		map.getOverlays().add(roadNodeMarkers);
		
		if (savedInstanceState != null){
			//STATIC mRoad = savedInstanceState.getParcelable("road");
			updateUIWithRoad(mRoad);
		}
		
		//POIs:
		//POI search interface:
		String[] poiTags = getResources().getStringArray(R.array.poi_tags);
		poiTagText = (AutoCompleteTextView) findViewById(R.id.poiTag);
		ArrayAdapter<String> poiAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, poiTags);
		poiTagText.setAdapter(poiAdapter);
		Button setPOITagButton = (Button) findViewById(R.id.buttonSetPOITag);
		setPOITagButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//Hide the soft keyboard:
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(poiTagText.getWindowToken(), 0);
				//Start search:
				String feature = poiTagText.getText().toString();
				if (!feature.equals(""))
					Toast.makeText(v.getContext(), "Searching:\n"+feature, Toast.LENGTH_LONG).show();
				getPOIAsync(feature);
			}
		});
		//POI markers:
		final ArrayList<ExtendedOverlayItem> poiItems = new ArrayList<ExtendedOverlayItem>();
		poiMarkers = new ItemizedOverlayWithBubble<ExtendedOverlayItem>(this, 
				poiItems, map, new POIInfoWindow(map));
		map.getOverlays().add(poiMarkers);
		if (savedInstanceState != null){
			//STATIC - mPOIs = savedInstanceState.getParcelableArrayList("poi");
			updateUIWithPOI(mPOIs);
		}

		//KML handling:
		kmlOverlay = null;
		if (savedInstanceState != null){
			//STATIC - mKmlDocument = savedInstanceState.getParcelable("kml");
			updateUIWithKml();
		} else { //first launch: 
			mKmlClipboard = new KmlFeature();
			mKmlClipboard.createAsFolder();
			//check if intent has been passed with a kml URI to load (url or file)
			Intent onCreateIntent = getIntent();
			if (onCreateIntent.getAction().equals(Intent.ACTION_VIEW)){
				String uri = onCreateIntent.getDataString();
				getKml(uri, true);
			}
		}
	}

	void setViewOn(BoundingBoxE6 bb){
		if (bb != null){
			map.zoomToBoundingBox(bb);
		}
	}
	
	void savePrefs(){
		SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
		SharedPreferences.Editor ed = prefs.edit();
		ed.putInt("MAP_ZOOM_LEVEL", map.getZoomLevel());
		GeoPoint c = (GeoPoint) map.getMapCenter();
		ed.putFloat("MAP_CENTER_LAT", c.getLatitudeE6()*1E-6f);
		ed.putFloat("MAP_CENTER_LON", c.getLongitudeE6()*1E-6f);
		MapTileProviderBase tileProvider = map.getTileProvider();
		String tileProviderName = tileProvider.getTileSource().name();
		View searchPanel = (View)findViewById(R.id.search_panel);
		ed.putInt("PANEL_VISIBILITY", searchPanel.getVisibility());
		ed.putString("TILE_PROVIDER", tileProviderName);
		ed.putInt("ROUTE_PROVIDER", whichRouteProvider);
		ed.commit();
	}
	
	/**
	 * callback to store activity status before a restart (orientation change for instance)
	 */
	@Override protected void onSaveInstanceState (Bundle outState){
		outState.putParcelable("location", myLocationOverlay.getLocation());
		outState.putBoolean("tracking_mode", mTrackingMode);
		outState.putParcelable("start", startPoint);
		outState.putParcelable("destination", destinationPoint);
		outState.putParcelableArrayList("viapoints", viaPoints);
		//STATIC - outState.putParcelable("road", mRoad);
		//STATIC - outState.putParcelableArrayList("poi", mPOIs);
		//STATIC - outState.putParcelable("kml", mKmlDocument);
		
		savePrefs();
	}
	
	@Override protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case ROUTE_REQUEST : 
			if (resultCode == RESULT_OK) {
				int nodeId = intent.getIntExtra("NODE_ID", 0);
				map.getController().setCenter(mRoad.mNodes.get(nodeId).mLocation);
				roadNodeMarkers.showBubbleOnItem(nodeId, map, true);
			}
			break;
		case POIS_REQUEST:
			if (resultCode == RESULT_OK) {
				int id = intent.getIntExtra("ID", 0);
				map.getController().setCenter(mPOIs.get(id).mLocation);
				poiMarkers.showBubbleOnItem(id, map, true);
			}
			break;
		case KML_TREE_REQUEST:
			KmlFeature result = mKmlStack.pop();
			if (resultCode == RESULT_OK) {
				//use the object which has been modified in KmlTreeActivity:
				mKmlDocument.kmlRoot = result; //intent.getParcelableExtra("KML");
				updateUIWithKml();
			}
			break;
		default: 
			break;
		}
	}
	
	/* String getBestProvider(){
		String bestProvider = null;
		//bestProvider = locationManager.getBestProvider(new Criteria(), true); // => returns "Network Provider"! 
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			bestProvider = LocationManager.GPS_PROVIDER;
		else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
			bestProvider = LocationManager.NETWORK_PROVIDER;
		return bestProvider;
	} */
	
	boolean startLocationUpdates(){
		boolean result = false;
		for (final String provider : locationManager.getProviders(true)) {
			locationManager.requestLocationUpdates(provider, 2*1000, 0.0f, this);
			result = true;
		}
		return result;
	}

	@Override protected void onResume() {
		super.onResume();
		boolean isOneProviderEnabled = startLocationUpdates();
		myLocationOverlay.setEnabled(isOneProviderEnabled);
		//TODO: not used currently
		//mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
			//sensor listener is causing a high CPU consumption...
	}

	@Override protected void onPause() {
		super.onPause();
		locationManager.removeUpdates(this);
		//TODO: mSensorManager.unregisterListener(this);
		savePrefs();
	}

	/**
     * Test MyItemizedOverlay object
     */
    public void putMyItemizedOverlay(GeoPoint p){
		ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();
		MyItemizedOverlay myOverlays = new MyItemizedOverlay(this, list);
		OverlayItem overlayItem = new OverlayItem("Home Sweet Home", "This is the place I live", p);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		Drawable marker = getResources().getDrawable(R.drawable.marker_departure);
		overlayItem.setMarker(marker);
		myOverlays.addItem(overlayItem);
		map.getOverlays().add(myOverlays);    	
		map.invalidate();
    }

    void updateUIWithTrackingMode(){
		if (mTrackingMode){
			mTrackingModeButton.setBackgroundResource(R.drawable.btn_tracking_on);
			if (myLocationOverlay.isEnabled()&& myLocationOverlay.getLocation() != null){
				map.getController().animateTo(myLocationOverlay.getLocation());
			}
			map.setMapOrientation(-mAzimuthAngleSpeed);
			mTrackingModeButton.setKeepScreenOn(true);
		} else {
			mTrackingModeButton.setBackgroundResource(R.drawable.btn_tracking_off);
			map.setMapOrientation(0.0f);
			mTrackingModeButton.setKeepScreenOn(false);
		}
    }

    //------------- Geocoding and Reverse Geocoding
    
    /** 
     * Reverse Geocoding
     */
    public String getAddress(GeoPoint p){
		GeocoderNominatim geocoder = new GeocoderNominatim(this);
		String theAddress;
		try {
			double dLatitude = p.getLatitudeE6() * 1E-6;
			double dLongitude = p.getLongitudeE6() * 1E-6;
			List<Address> addresses = geocoder.getFromLocation(dLatitude, dLongitude, 1);
			StringBuilder sb = new StringBuilder(); 
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				int n = address.getMaxAddressLineIndex();
				for (int i=0; i<=n; i++) {
					if (i!=0)
						sb.append(", ");
					sb.append(address.getAddressLine(i));
				}
				theAddress = new String(sb.toString());
			} else {
				theAddress = null;
			}
		} catch (IOException e) {
			theAddress = null;
		}
		if (theAddress != null) {
			return theAddress;
		} else {
			return "";
		}
    }
    
    /**
     * Geocoding of the departure or destination address
     */
	public void handleSearchButton(int index, int editResId){
		EditText locationEdit = (EditText)findViewById(editResId);
		//Hide the soft keyboard:
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(locationEdit.getWindowToken(), 0);
		
		String locationAddress = locationEdit.getText().toString();
		
		if (locationAddress.equals("")){
			removePoint(index);
			map.invalidate();
			return;
		}
		
		Toast.makeText(this, "Searching:\n"+locationAddress, Toast.LENGTH_LONG).show();
		AutoCompleteOnPreferences.storePreference(this, locationAddress, SHARED_PREFS_APPKEY, PREF_LOCATIONS_KEY);
		GeocoderNominatim geocoder = new GeocoderNominatim(this);
		geocoder.setOptions(true); //ask for enclosing polygon (if any)
		try {
			List<Address> foundAdresses = geocoder.getFromLocationName(locationAddress, 1);
			if (foundAdresses.size() == 0) { //if no address found, display an error
				Toast.makeText(this, "Address not found.", Toast.LENGTH_SHORT).show();
			} else {
				Address address = foundAdresses.get(0); //get first address
				if (index == START_INDEX){
					startPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
					markerStart = putMarkerItem(markerStart, startPoint, START_INDEX,
						R.string.departure, R.drawable.marker_departure, -1);
					map.getController().setCenter(startPoint);
				} else if (index == DEST_INDEX){
					destinationPoint = new GeoPoint(address.getLatitude(), address.getLongitude());
					markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
						R.string.destination, R.drawable.marker_destination, -1);
					map.getController().setCenter(destinationPoint);
				}
				getRoadAsync();
				//get and display enclosing polygon:
				Bundle extras = address.getExtras();
				if (extras != null && extras.containsKey("polygonpoints")){
					ArrayList<GeoPoint> polygon = extras.getParcelableArrayList("polygonpoints");
					//Log.d("DEBUG", "polygon:"+polygon.size());
					updateUIWithPolygon(polygon);
				} else {
					updateUIWithPolygon(null);
				}
			}
		} catch (Exception e) {
			Toast.makeText(this, "Geocoding error", Toast.LENGTH_SHORT).show();
		}
	}
	
	//add or replace the polygon overlay
	public void updateUIWithPolygon(ArrayList<GeoPoint> polygon){
		List<Overlay> mapOverlays = map.getOverlays();
		int location = -1;
		if (destinationPolygon != null)
			location = mapOverlays.indexOf(destinationPolygon);
		destinationPolygon = new Polygon(this);
		destinationPolygon.setFillColor(0x30FF0080);
		destinationPolygon.setStrokeColor(0x800000FF);
		destinationPolygon.setStrokeWidth(5.0f);
		BoundingBoxE6 bb = null;
		if (polygon != null){
			destinationPolygon.setPoints(polygon);
			bb = BoundingBoxE6.fromGeoPoints(polygon);
		}
		if (location != -1)
			mapOverlays.set(location, destinationPolygon);
		else
			mapOverlays.add(destinationPolygon);
		if (bb != null)
			setViewOn(bb);
		map.invalidate();
	}
	
	//Async task to reverse-geocode the marker position in a separate thread:
	private class GeocodingTask extends AsyncTask<Object, Void, String> {
		ExtendedOverlayItem marker;
		protected String doInBackground(Object... params) {
			marker = (ExtendedOverlayItem)params[0];
			return getAddress(marker.getPoint());
		}
		protected void onPostExecute(String result) {
			marker.setDescription(result);
			itineraryMarkers.showBubbleOnItem(marker, map, false); //open bubble on the item
		}
	}
	
    //------------ Itinerary markers
	
	/** add (or replace) an item in markerOverlays. p position. 
	 */
    public ExtendedOverlayItem putMarkerItem(ExtendedOverlayItem item, GeoPoint p, int index,
    		int titleResId, int markerResId, int iconResId) {
		if (item != null){
			itineraryMarkers.removeItem(item);
		}
		Drawable marker = getResources().getDrawable(markerResId);
		String title = getResources().getString(titleResId);
		ExtendedOverlayItem overlayItem = new ExtendedOverlayItem(title, "", p, this);
		overlayItem.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		overlayItem.setMarker(marker);
		if (iconResId != -1)
			overlayItem.setImage(getResources().getDrawable(iconResId));
		overlayItem.setRelatedObject(index);
		itineraryMarkers.addItem(overlayItem);
		map.invalidate();
		//Start geocoding task to update the description of the marker with its address:
		new GeocodingTask().execute(overlayItem);
		return overlayItem;
	}

	public void addViaPoint(GeoPoint p){
		viaPoints.add(p);
		putMarkerItem(null, p, viaPoints.size()-1,
			R.string.viapoint, R.drawable.marker_via, -1);
	}
    
	public void removePoint(int index){
		if (index == START_INDEX){
			startPoint = null;
			if (markerStart != null){
				itineraryMarkers.removeItem(markerStart);
				markerStart = null;
			}
		} else if (index == DEST_INDEX){
			destinationPoint = null;
			if (markerDestination != null){
				itineraryMarkers.removeItem(markerDestination);
				markerDestination = null;
			}
		} else {
			viaPoints.remove(index);
			updateUIWithItineraryMarkers();
		}
		getRoadAsync();
	}
	
	public void updateUIWithItineraryMarkers(){
		itineraryMarkers.removeAllItems();
		//Start marker:
		if (startPoint != null){
			markerStart = putMarkerItem(null, startPoint, START_INDEX, 
				R.string.departure, R.drawable.marker_departure, -1);
		}
		//Via-points markers if any:
		for (int index=0; index<viaPoints.size(); index++){
			putMarkerItem(null, viaPoints.get(index), index, 
				R.string.viapoint, R.drawable.marker_via, -1);
		}
		//Destination marker if any:
		if (destinationPoint != null){
			markerDestination = putMarkerItem(null, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1);
		}
	}
	
    //------------ Route and Directions
    
    private void putRoadNodes(Road road){
		roadNodeMarkers.removeAllItems();
		Drawable marker = getResources().getDrawable(R.drawable.marker_node);
		int n = road.mNodes.size();
		TypedArray iconIds = getResources().obtainTypedArray(R.array.direction_icons);
    	for (int i=0; i<n; i++){
    		RoadNode node = road.mNodes.get(i);
    		String instructions = (node.mInstructions==null ? "" : node.mInstructions);
    		ExtendedOverlayItem nodeMarker = new ExtendedOverlayItem(
    				"Step " + (i+1), instructions, 
    				node.mLocation, this);
    		nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
    		nodeMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
    		nodeMarker.setMarker(marker);
    		int iconId = iconIds.getResourceId(node.mManeuverType, R.drawable.ic_empty);
    		if (iconId != R.drawable.ic_empty){
	    		Drawable icon = getResources().getDrawable(iconId);
	    		nodeMarker.setImage(icon);
    		}
    		roadNodeMarkers.addItem(nodeMarker);
    	}
    	iconIds.recycle();
    }
    
	void updateUIWithRoad(Road road){
		roadNodeMarkers.removeAllItems();
		TextView textView = (TextView)findViewById(R.id.routeInfo);
		textView.setText("");
		List<Overlay> mapOverlays = map.getOverlays();
		if (roadOverlay != null){
			mapOverlays.remove(roadOverlay);
		}
		if (road == null)
			return;
		if (road.mStatus == Road.STATUS_DEFAULT)
			Toast.makeText(map.getContext(), "We have a problem to get the route", Toast.LENGTH_SHORT).show();
//		roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
//		Overlay removedOverlay = mapOverlays.set(1, roadOverlay);
			//we set the road overlay at the "bottom", just above the MapEventsOverlay,
			//to avoid covering the other overlays. 
//		mapOverlays.add(removedOverlay);
		putRoadNodes(road);
		map.invalidate();
		//Set route info in the text view:
		textView.setText(road.getLengthDurationText(-1));
    }
    
	/**
	 * Async task to get the road in a separate thread. 
	 */
	private class UpdateRoadTask extends AsyncTask<Object, Void, Road> {
		protected Road doInBackground(Object... params) {
			@SuppressWarnings("unchecked")
			ArrayList<GeoPoint> waypoints = (ArrayList<GeoPoint>)params[0];
			RoadManager roadManager = null;
			Locale locale = Locale.getDefault();
			switch (whichRouteProvider){
			case OSRM:
				roadManager = new OSRMRoadManager();
				break;
			case MAPQUEST_FASTEST:
				roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				break;
			case MAPQUEST_BICYCLE:
				roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				roadManager.addRequestOption("routeType=bicycle");
				break;
			case MAPQUEST_PEDESTRIAN:
				roadManager = new MapQuestRoadManager("Fmjtd%7Cluubn10zn9%2C8s%3Do5-90rnq6");
				roadManager.addRequestOption("locale="+locale.getLanguage()+"_"+locale.getCountry());
				roadManager.addRequestOption("routeType=pedestrian");
				break;
			case GOOGLE_FASTEST:
				roadManager = new GoogleRoadManager();
				//roadManager.addRequestOption("mode=driving"); //default
				break;
			default:
				return null;
			}
			return roadManager.getRoad(waypoints);
		}

		protected void onPostExecute(Road result) {
			mRoad = result;
			updateUIWithRoad(result);
			getPOIAsync(poiTagText.getText().toString());
		}
	}
	
	public void getRoadAsync(){
		mRoad = null;
		GeoPoint roadStartPoint = null;
		if (startPoint != null){
			roadStartPoint = startPoint;
		} else if (myLocationOverlay.isEnabled() && myLocationOverlay.getLocation() != null){
			//use my current location as itinerary start point:
			roadStartPoint = myLocationOverlay.getLocation();
		}
		if (roadStartPoint == null || destinationPoint == null){
			updateUIWithRoad(mRoad);
			return;
		}
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>(2);
		waypoints.add(roadStartPoint);
		//add intermediate via points:
		for (GeoPoint p:viaPoints){
			waypoints.add(p); 
		}
		waypoints.add(destinationPoint);
		new UpdateRoadTask().execute(waypoints);
	}

	//----------------- POIs
	
	void updateUIWithPOI(ArrayList<POI> pois){
		if (pois != null){
			for (POI poi:pois){
				ExtendedOverlayItem poiMarker = new ExtendedOverlayItem(
					poi.mType, poi.mDescription, 
					poi.mLocation, this);
				Drawable marker = null;
				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM){
					marker = getResources().getDrawable(R.drawable.marker_poi);
				} else if (poi.mServiceId == POI.POI_SERVICE_GEONAMES_WIKIPEDIA){
					if (poi.mRank < 90)
						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_16);
					else
						marker = getResources().getDrawable(R.drawable.marker_poi_wikipedia_32);
				} else if (poi.mServiceId == POI.POI_SERVICE_FLICKR){
					marker = getResources().getDrawable(R.drawable.marker_poi_flickr);
				} else if (poi.mServiceId == POI.POI_SERVICE_PICASA){
					marker = getResources().getDrawable(R.drawable.marker_poi_picasa_24);
					poiMarker.setSubDescription(poi.mCategory);
				}
				poiMarker.setMarker(marker);
				if (poi.mServiceId == POI.POI_SERVICE_NOMINATIM){
					poiMarker.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
				} else {
					poiMarker.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
				}
				//thumbnail loading moved in POIInfoWindow.onOpen for better performances. 
				poiMarker.setRelatedObject(poi);
				poiMarkers.addItem(poiMarker);
			}
		}
		map.invalidate();
	}
	
	ExecutorService mThreadPool = Executors.newFixedThreadPool(5);
	/** Loads all thumbnails in background */
	void startAsyncThumbnailsLoading(ArrayList<POI> pois){
		/* Try to stop existing threads:
		 * not sure it has any effect... 
		if (mThreadPool != null){
			//Stop threads if any:
			mThreadPool.shutdownNow();
		}
		mThreadPool = Executors.newFixedThreadPool(5);
		*/
		for (int i=0; i<pois.size(); i++){
			final int index = i;
			final POI poi = pois.get(index);
			mThreadPool.submit(new Runnable(){
				@Override public void run(){
					Bitmap b = poi.getThumbnail();
					if (b != null){
						/*
						//Change POI marker:
						ExtendedOverlayItem item = poiMarkers.getItem(index);
						b = Bitmap.createScaledBitmap(b, 48, 48, true);
						BitmapDrawable bd = new BitmapDrawable(getResources(), b);
						item.setMarker(bd);
						*/
					}
				}
			});
		}
	}
	
	private class POITask extends AsyncTask<Object, Void, ArrayList<POI>> {
		String mTag;
		protected ArrayList<POI> doInBackground(Object... params) {
			mTag = (String)params[0];
			
			if (mTag == null || mTag.equals("")){
				return null;
			} else if (mTag.equals("wikipedia")){
				GeoNamesPOIProvider poiProvider = new GeoNamesPOIProvider("mkergall");
				//Get POI inside the bounding box of the current map view:
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mTag.equals("flickr")){
				FlickrPOIProvider poiProvider = new FlickrPOIProvider("c39be46304a6c6efda8bc066c185cd7e");
				BoundingBoxE6 bb = map.getBoundingBox();
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 30);
				return pois;
			} else if (mTag.startsWith("picasa")){
				PicasaPOIProvider poiProvider = new PicasaPOIProvider(null);
				BoundingBoxE6 bb = map.getBoundingBox();
				//allow to search for keywords among picasa photos:
				String q = mTag.substring("picasa".length());
				ArrayList<POI> pois = poiProvider.getPOIInside(bb, 50, q);
				return pois;
			} else {
				NominatimPOIProvider poiProvider = new NominatimPOIProvider();
				//poiProvider.setService(NominatimPOIProvider.MAPQUEST_POI_SERVICE);
				ArrayList<POI> pois;
				if (mRoad == null){
					BoundingBoxE6 bb = map.getBoundingBox();
					pois = poiProvider.getPOIInside(bb, mTag, 100);
				} else {
					pois = poiProvider.getPOIAlong(mRoad.getRouteLow(), mTag, 100, 2.0);
				}
				return pois;
			}
		}
		protected void onPostExecute(ArrayList<POI> pois) {
			mPOIs = pois;
			if (mTag.equals("")){
				//no search, no message
			} else if (mPOIs == null){
				Toast.makeText(getApplicationContext(), "Technical issue when getting "+mTag+ " POI.", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), ""+mPOIs.size()+" "+mTag+ " entries found", Toast.LENGTH_LONG).show();
				if (mTag.equals("flickr")||mTag.startsWith("picasa")||mTag.equals("wikipedia"))
					startAsyncThumbnailsLoading(mPOIs);
			}
			updateUIWithPOI(mPOIs);
		}
	}
	
	void getPOIAsync(String tag){
		poiMarkers.removeAllItems();
		new POITask().execute(tag);
	}
	
	//------------ KML handling

	void openKMLUrlDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("KML url");
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		String defaultUri = "http://mapsengine.google.com/map/kml?mid=z6IJfj90QEd4.kUUY9FoHFRdE";
		//String defaultUri = "http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat=52.215676&flon=5.963946&tlat=52.2573&tlon=6.1799";
		SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
		String uri = prefs.getString("KML_URI", defaultUri);
		input.setText(uri);
		builder.setView(input);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
			@Override public void onClick(DialogInterface dialog, int which) {
				String uri = input.getText().toString();
				SharedPreferences prefs = getSharedPreferences("OSMNAVIGATOR", MODE_PRIVATE);
				SharedPreferences.Editor ed = prefs.edit();
				ed.putString("KML_URI", uri);
				ed.commit();
				dialog.cancel();
				getKml(uri, false);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}
	
	void getKml(String uri, boolean onCreate){
		//TODO: use an Async task
		mKmlDocument = new KmlDocument();
		boolean ok;
		if (uri.startsWith("file:/")){
			uri = uri.substring("file:/".length());
			File file = new File(uri);
			ok = mKmlDocument.parseFile(file);
		} else  {
			ok = mKmlDocument.parseUrl(uri);
		}
		if (!ok)
			Toast.makeText(this, "Sorry, unable to read "+uri, Toast.LENGTH_SHORT).show();
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
	
	void saveKml(String fileName){
		boolean result = mKmlDocument.saveAsKML(mKmlDocument.getDefaultPathForAndroid(fileName));
		if (result)
			Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
		else 
			Toast.makeText(this, "Unable to save "+fileName, Toast.LENGTH_SHORT).show();
	}
	
	void updateUIWithKml(){
		if (kmlOverlay != null)
			map.getOverlays().remove(kmlOverlay);
		if (mKmlDocument.kmlRoot != null){
			Drawable defaultKmlMarker = getResources().getDrawable(R.drawable.marker_kml_point);
			kmlOverlay = (FolderOverlay)mKmlDocument.kmlRoot.buildOverlays(this, map, defaultKmlMarker, mKmlDocument, true);
			map.getOverlays().add(kmlOverlay);
		}
		map.invalidate();
	}
	
	void insertOverlaysInKml(){
		//Ensure the root is a folder:
		if (mKmlDocument.kmlRoot == null || mKmlDocument.kmlRoot.mObjectType != KmlFeature.FOLDER){
			mKmlDocument.kmlRoot = new KmlFeature();
			mKmlDocument.kmlRoot.createAsFolder();
		}
		//Insert relevant overlays inside:
		mKmlDocument.kmlRoot.addOverlay(itineraryMarkers, mKmlDocument);
//		mKmlDocument.kmlRoot.addOverlay(roadOverlay, mKmlDocument);
		mKmlDocument.kmlRoot.addOverlay(roadNodeMarkers, mKmlDocument);
		mKmlDocument.kmlRoot.addOverlay(destinationPolygon, mKmlDocument);
		mKmlDocument.kmlRoot.addOverlay(poiMarkers, mKmlDocument);
	}
	
	//------------ MapEventsReceiver implementation

	GeoPoint tempClickedGeoPoint; //any other way to pass the position to the menu ???
	
	@Override public boolean longPressHelper(IGeoPoint p) {
		tempClickedGeoPoint = new GeoPoint((GeoPoint)p);
		Button searchButton = (Button)findViewById(R.id.buttonSearchDest);
		openContextMenu(searchButton); 
			//menu is hooked on the "Search Destination" button, as it must be hooked somewhere. 
		return true;
	}

	@Override public boolean singleTapUpHelper(IGeoPoint p) {
		return false;
	}

	//----------- Context Menu when clicking on the map
	@Override public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
	}

	@Override public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_departure:
			startPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			markerStart = putMarkerItem(markerStart, startPoint, START_INDEX,
				R.string.departure, R.drawable.marker_departure, -1);
			getRoadAsync();
			return true;
		case R.id.menu_destination:
			destinationPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			markerDestination = putMarkerItem(markerDestination, destinationPoint, DEST_INDEX,
				R.string.destination, R.drawable.marker_destination, -1);
			getRoadAsync();
			return true;
		case R.id.menu_viapoint:
			GeoPoint viaPoint = new GeoPoint((GeoPoint)tempClickedGeoPoint);
			addViaPoint(viaPoint);
			getRoadAsync();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	//------------ Option Menu implementation
	
	@Override public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		
		switch (whichRouteProvider){
		case OSRM: 
			menu.findItem(R.id.menu_route_osrm).setChecked(true);
			break;
		case MAPQUEST_FASTEST:
			menu.findItem(R.id.menu_route_mapquest_fastest).setChecked(true);
			break;
		case MAPQUEST_BICYCLE:
			menu.findItem(R.id.menu_route_mapquest_bicycle).setChecked(true);
			break;
		case MAPQUEST_PEDESTRIAN:
			menu.findItem(R.id.menu_route_mapquest_pedestrian).setChecked(true);
			break;
		case GOOGLE_FASTEST:
			menu.findItem(R.id.menu_route_google).setChecked(true);
			break;
		}
		
		if (map.getTileProvider().getTileSource() == TileSourceFactory.MAPNIK)
			menu.findItem(R.id.menu_tile_mapnik).setChecked(true);
		else if (map.getTileProvider().getTileSource() == TileSourceFactory.MAPQUESTOSM)
			menu.findItem(R.id.menu_tile_mapquest_osm).setChecked(true);
		else if (map.getTileProvider().getTileSource() == MAPBOXSATELLITELABELLED)
			menu.findItem(R.id.menu_tile_mapbox_satellite).setChecked(true);
		
		return true;
	}
	
	@Override public boolean onPrepareOptionsMenu(Menu menu) {
		if (mRoad != null && mRoad.mNodes.size()>0)
			menu.findItem(R.id.menu_itinerary).setEnabled(true);
		else
			menu.findItem(R.id.menu_itinerary).setEnabled(false);
		if (mPOIs != null && mPOIs.size()>0)
			menu.findItem(R.id.menu_pois).setEnabled(true);
		else 
			menu.findItem(R.id.menu_pois).setEnabled(false);
		return true;
	}
	
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		Intent myIntent;
		switch (item.getItemId()) {
		case R.id.menu_itinerary:
			myIntent = new Intent(this, RouteActivity.class);
			myIntent.putExtra("NODE_ID", roadNodeMarkers.getBubbledItemId());
			startActivityForResult(myIntent, ROUTE_REQUEST); //TODO: crash if road is too big. Pass only needed info?
			return true;
		case R.id.menu_pois:
			myIntent = new Intent(this, POIActivity.class);
			//STATIC myIntent.putParcelableArrayListExtra("POI", mPOIs);
			myIntent.putExtra("ID", poiMarkers.getBubbledItemId());
			startActivityForResult(myIntent, POIS_REQUEST);
			return true;
		case R.id.menu_kml_url:
			openKMLUrlDialog();
			return true;
		case R.id.menu_kml_file:
			//TODO : openKMLFileDialog();
			File file = mKmlDocument.getDefaultPathForAndroid("current.kml");
			getKml("file:/"+file.toString(), false);
			return true;
		case R.id.menu_kml_get_overlays:
			insertOverlaysInKml();
			updateUIWithKml();
			return true;
		case R.id.menu_kml_tree:
			if (mKmlDocument.kmlRoot==null)
				return false;
			myIntent = new Intent(this, KmlTreeActivity.class);
			//myIntent.putExtra("KML", mKmlDocument.kmlRoot);
			mKmlStack.push(mKmlDocument.kmlRoot.clone());
			startActivityForResult(myIntent, KML_TREE_REQUEST);
			return true;
		case R.id.menu_kml_save:
			//TODO: openKMLSaveDialog();
			saveKml("current.kml");
			return true;
		case R.id.menu_kml_clear:
			mKmlDocument = new KmlDocument();
			updateUIWithKml();
			return true;
		case R.id.menu_route_osrm:
			whichRouteProvider = OSRM;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_mapquest_fastest:
			whichRouteProvider = MAPQUEST_FASTEST;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_mapquest_bicycle:
			whichRouteProvider = MAPQUEST_BICYCLE;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_mapquest_pedestrian:
			whichRouteProvider = MAPQUEST_PEDESTRIAN;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_route_google:
			whichRouteProvider = GOOGLE_FASTEST;
			item.setChecked(true);
			getRoadAsync();
			return true;
		case R.id.menu_tile_mapnik:
			map.setTileSource(TileSourceFactory.MAPNIK);
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapquest_osm:
			map.setTileSource(TileSourceFactory.MAPQUESTOSM);
			item.setChecked(true);
			return true;
		case R.id.menu_tile_mapbox_satellite:
			map.setTileSource(MAPBOXSATELLITELABELLED);
			item.setChecked(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	//------------ LocationListener implementation
	private final NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();
	long mLastTime = 0; // milliseconds
	double mSpeed = 0.0; // km/h
	@Override public void onLocationChanged(final Location pLoc) {
		long currentTime = System.currentTimeMillis();
		if (mIgnorer.shouldIgnore(pLoc.getProvider(), currentTime))
            return;
		double dT = currentTime - mLastTime;
		if (dT < 100.0){
			//Toast.makeText(this, pLoc.getProvider()+" dT="+dT, Toast.LENGTH_SHORT).show();
			return;
		}
		mLastTime = currentTime;
		
		GeoPoint newLocation = new GeoPoint(pLoc);
		if (!myLocationOverlay.isEnabled()){
			//we get the location for the first time:
			myLocationOverlay.setEnabled(true);
			map.getController().animateTo(newLocation);
		}
		
		GeoPoint prevLocation = myLocationOverlay.getLocation();
		myLocationOverlay.setLocation(newLocation);
		myLocationOverlay.setAccuracy((int)pLoc.getAccuracy());

		if (prevLocation != null && pLoc.getProvider().equals(LocationManager.GPS_PROVIDER)){
			/*
			double d = prevLocation.distanceTo(newLocation);
			mSpeed = d/dT*1000.0; // m/s
			mSpeed = mSpeed * 3.6; //km/h
			*/
			mSpeed = pLoc.getSpeed() * 3.6;
			long speedInt = Math.round(mSpeed);
			TextView speedTxt = (TextView)findViewById(R.id.speed);
			speedTxt.setText(speedInt + " km/h");
			
			//TODO: check if speed is not too small
			if (mSpeed >= 0.1){
				//mAzimuthAngleSpeed = (float)prevLocation.bearingTo(newLocation);
				mAzimuthAngleSpeed = (float)pLoc.getBearing();
				myLocationOverlay.setBearing(mAzimuthAngleSpeed);
			}
		}
		
		if (mTrackingMode){
			//keep the map view centered on current location:
			map.getController().animateTo(newLocation);
			map.setMapOrientation(-mAzimuthAngleSpeed);
		} else {
			//just redraw the location overlay:
			map.invalidate();
		}
	}

	@Override public void onProviderDisabled(String provider) {}

	@Override public void onProviderEnabled(String provider) {}

	@Override public void onStatusChanged(String provider, int status, Bundle extras) {}

	//------------ SensorEventListener implementation
	@Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
		myLocationOverlay.setAccuracy(accuracy);
		map.invalidate();
	}

	static float mAzimuthOrientation = 0.0f;
	@Override public void onSensorChanged(SensorEvent event) {
		switch (event.sensor.getType()){
			case Sensor.TYPE_ORIENTATION: 
				if (mSpeed < 0.1){
					/* TODO Filter to implement...
					float azimuth = event.values[0];
					if (Math.abs(azimuth-mAzimuthOrientation)>2.0f){
						mAzimuthOrientation = azimuth;
						myLocationOverlay.setBearing(mAzimuthOrientation);
						if (mTrackingMode)
							map.setMapOrientation(-mAzimuthOrientation);
						else
							map.invalidate();
					}
					*/
				}
				//at higher speed, we use speed vector, not phone orientation. 
				break;
			default:
				break;
		}
	}
}
