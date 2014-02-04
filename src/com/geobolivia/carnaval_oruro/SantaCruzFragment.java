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

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SantaCruzFragment extends Fragment {
	private MapView map;
	protected FolderOverlay kmlOverlay; //root container of overlays from KML reading
	public static KmlDocument kmlDocument;
	public SantaCruzFragment(){}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_santacruz, container, false);

        map = (MapView) rootView.findViewById(R.id.openmapview);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setClickable(true);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        
        kmlDocument = new KmlDocument();
        String kmlFile = "corso_sc.kml";
        
        // References Points
        GeoPoint startPoint = new GeoPoint(-17.79368480211741, -63.16620676309212);
        GeoPoint finishPoint = new GeoPoint(-17.77188068110373, -63.18845045412476);
        
        IMapController mapController = map.getController();
        mapController.setZoom(14);
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

        //. Loading KML content
		File file = kmlDocument.getDefaultPathForAndroid(kmlFile);
		boolean ok = kmlDocument.parseFile(file);
		Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_kml_point);
		if (ok){
			FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.kmlRoot.buildOverlays(getActivity(), map, defaultMarker, kmlDocument, false);
			map.getOverlays().add(kmlOverlay);
			map.invalidate();
			if (kmlDocument.kmlRoot.mBB != null){
				//Workaround:
				map.getController().setCenter(new GeoPoint(
					kmlDocument.kmlRoot.mBB.getLatSouthE6()+kmlDocument.kmlRoot.mBB.getLatitudeSpanE6()/2, 
					kmlDocument.kmlRoot.mBB.getLonWestE6()+kmlDocument.kmlRoot.mBB.getLongitudeSpanE6()/2));
			}
		}

        return rootView;
    }
}