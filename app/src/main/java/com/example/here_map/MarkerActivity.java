package com.example.here_map;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.here.android.mpa.cluster.BasicClusterStyle;
import com.here.android.mpa.cluster.ClusterLayer;
import com.here.android.mpa.cluster.ClusterTheme;
import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.electronic_horizon.MetaData;
import com.here.android.mpa.mapping.AndroidXMapFragment;

import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapScreenMarker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MarkerActivity extends FragmentActivity {

    // map embedded in the map fragment
  private Map map;

    // map fragment embedded in this activity
    private AndroidXMapFragment mapFragment = null;

    private final LinkedList<MapMarker> m_map_markers = new LinkedList<>();
    private int mapMarkerCount = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        initialize();
    }

    private void initialize() {
        setContentView(R.layout.activity_marker);
        mapFragment = (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);

        com.here.android.mpa.common.MapSettings.setDiskCacheRootPath(
                getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps");

        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    map = mapFragment.getMap();
                    map.setCenter(new GeoCoordinate(13.648732, 100.681736, 0.0),
                            Map.Animation.NONE);
                    map.setZoomLevel(7);

                    Image image = new Image();

                    try {
                        image.setImageResource(R.drawable.marker);
                        List<GeoCoordinate> geoCoordinate =new ArrayList<>();
                        geoCoordinate.add(new GeoCoordinate(13.648732, 100.681736));
                        geoCoordinate.add(new GeoCoordinate(13.652400, 100.663573));
                        geoCoordinate.add(new GeoCoordinate(13.659739, 100.673014));
                        geoCoordinate.add(new GeoCoordinate(13.668614, 100.634535));
                        geoCoordinate.add(new GeoCoordinate(13.665676, 100.633445));
                        geoCoordinate.add(new GeoCoordinate(13.676815, 100.603450));
                        geoCoordinate.add(new GeoCoordinate(13.675806, 100.604978));
                        geoCoordinate.add(new GeoCoordinate(13.677355, 100.567108));
                        geoCoordinate.add(new GeoCoordinate(13.623562, 100.551380));
                        geoCoordinate.add(new GeoCoordinate(13.596756, 100.634796));
                        geoCoordinate.add(new GeoCoordinate(13.137719, 99.974994));
                        geoCoordinate.add(new GeoCoordinate(13.191049, 99.928873));
                        geoCoordinate.add(new GeoCoordinate(13.191049, 99.953375));
                        geoCoordinate.add(new GeoCoordinate(13.233144, 99.972112));
                        geoCoordinate.add(new GeoCoordinate(13.192452, 100.009586));
                        geoCoordinate.add(new GeoCoordinate(13.059106, 100.894548));
                        geoCoordinate.add(new GeoCoordinate(13.035236, 100.898872));
                        geoCoordinate.add(new GeoCoordinate(13.040852, 100.916168));
                        geoCoordinate.add(new GeoCoordinate(13.018385, 100.929140));
                        geoCoordinate.add(new GeoCoordinate(12.979063, 100.911844));

                        for(int i=0;i<geoCoordinate.size();i++) {
                            MapMarker marker = new MapMarker(geoCoordinate.get(i), image);
                            marker.setDraggable(true);
                            marker.setTitle("MapMarker id: " + mapMarkerCount++);
                            m_map_markers.add(marker);
                            Log.d("ID",i+"");
                        }

                        ClusterLayer cl = new ClusterLayer();
                        BasicClusterStyle redStyle = new BasicClusterStyle();
                        redStyle.setFillColor(Color.RED);
                        BasicClusterStyle greenStyle = new BasicClusterStyle();
                        greenStyle.setFillColor(Color.GREEN);

                        ClusterTheme theme = new ClusterTheme();
                        theme.setStyleForDensityRange(10, 19, greenStyle);
                        theme.setStyleForDensityRange(20, 49, redStyle);


                        for(int i=0;i<m_map_markers.size();i++) {
                            //map.addMapObject(m_map_markers.get(i));
                            cl.addMarker(m_map_markers.get(i));
                            cl.setTheme(theme);
                            map.addClusterLayer(cl);
                        }


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }


            }
        });
    }


}