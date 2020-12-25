package com.example.here_map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.LocationDataSourceHERE;
import com.here.android.mpa.common.MapEngine;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.RoadElement;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.guidance.LaneInformation;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.AndroidXMapFragment;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapState;
import com.here.android.mpa.mapping.OnMapRenderListener;
import com.here.android.mpa.mapping.PositionIndicator;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.RoutingError;
import com.here.android.positioning.StatusListener;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

public class TestActivity extends FragmentActivity implements PositioningManager.OnPositionChangedListener, Map.OnTransformListener {

    ///////////////////    ////////     ////         ////////     ////         ////   ////////      ///////////            ///////       ////////////    ////////        ///////         ////////      ////      ///////////////////
    ///////////////////    //// ////    ////        //// /////     ////       ////      ////     ////        ////         //// ////          ////          ////       ////    ////       ////  ////    ////      ///////////////////
    ///////////////////    ////  ////   ////       ////   /////     ////     ////       ////    ////                     ////   ////         ////          ////     ////        ////     ////   ////   ////      ///////////////////
    ///////////////////    ////   ////  ////      //////////////     ////   ////        ////    ////       ///////      /////////////        ////          ////      ////       ////     ////    ////  ////      ///////////////////
    ///////////////////    ////    //// ////     ////       /////     //// ////         ////    ////         ////      ////       ////       ////          ////       ////    ////       ////     //// ////      ///////////////////
    ///////////////////    ////     ////////    ////         /////     ///////        ////////    /////////////       ////         ////      ////        ////////       ///////          ////      ////////      ///////////////////


    // map embedded in the map fragment
    private Map map = null;

    // map fragment embedded in this activity
    private AndroidXMapFragment mapFragment = null;

    // HERE location data source instance
    private LocationDataSourceHERE mHereLocation;

    // flag that indicates whether maps is being transformed
    private boolean mTransforming;

    // callback that is called when transforming ends
    private Runnable mPendingUpdate;

    // text view instance for showing location information
    private TextView mLocationInfo;

    private PositioningManager mPositioningManager;
    private MapRoute m_currentRoute;
    private PointF m_mapTransformCenter;
    private LinearLayout m_laneInfoView;
    private boolean m_returningToRoadViewMode = false;
    private double m_lastZoomLevelInRoadViewMode = 0.0;
    private MapMarker m_positionIndicatorFixed = null;
    // permissions request code
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] RUNTIME_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };


    private AppCompatActivity m_activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        if (hasPermissions(this, RUNTIME_PERMISSIONS)) {
            initialize();
        } else {
            ActivityCompat
                    .requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE_ASK_PERMISSIONS);
        }
        initialize();
    }

    private void initialize() {

        m_laneInfoView = findViewById(R.id.laneInfoLayout);

        mapFragment = (AndroidXMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapfragment);

        com.here.android.mpa.common.MapSettings.setDiskCacheRootPath(
                getApplicationContext().getExternalFilesDir(null) + File.separator + ".here-maps");


        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    mapFragment.getMapGesture().addOnGestureListener(gestureListener, 100, true);
                    map = mapFragment.getMap();
                    // Set the map center to the Vancouver region (no animation)
                    map.setCenter(new GeoCoordinate(13.706677, 100.530656, 0.0),
                            Map.Animation.NONE);
                    // Set the zoom level to the average between min and max

                    map.addTransformListener(onTransformListener);
                    PositioningManager.getInstance().start(PositioningManager.LocationMethod.GPS_NETWORK);
                    mPositioningManager = PositioningManager.getInstance();
                    mHereLocation = LocationDataSourceHERE.getInstance(new StatusListener() {
                        @Override
                        public void onOfflineModeChanged(boolean b) {
                        }

                        @Override
                        public void onAirplaneModeEnabled() {
                        }

                        @Override
                        public void onWifiScansDisabled() {
                        }

                        @Override
                        public void onBluetoothDisabled() {
                        }

                        @Override
                        public void onCellDisabled() {
                        }

                        @Override
                        public void onGnssLocationDisabled() {
                        }

                        @Override
                        public void onNetworkLocationDisabled() {
                        }

                        @Override
                        public void onServiceError(ServiceError serviceError) {
                        }

                        @Override
                        public void onPositioningError(PositioningError positioningError) {
                        }

                        @Override
                        public void onWifiIndoorPositioningNotAvailable() {
                        }

                        @Override
                        public void onWifiIndoorPositioningDegraded() {
                        }
                    });


                    if (mHereLocation == null) {
                        Toast.makeText(TestActivity.this, "LocationDataSourceHERE.getInstance(): failed, exiting", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    mPositioningManager.setDataSource(mHereLocation);
                    mPositioningManager.addListener(new WeakReference<PositioningManager.OnPositionChangedListener>(
                            TestActivity.this));
                    // start position updates, accepting GPS, network or indoor positions
                    if (mPositioningManager.start(PositioningManager.LocationMethod.GPS_NETWORK_INDOOR)) {
                        mapFragment.getPositionIndicator().setVisible(true);


                    } else {
                        Toast.makeText(TestActivity.this, "PositioningManager.start: failed, exiting", Toast.LENGTH_LONG).show();
                        finish();
                    }


                } else {
                        new AlertDialog.Builder(m_activity).setMessage(
                                "Error : " + error.name() + "\n\n" + error.getDetails())
                                .setTitle(R.string.engine_init_error)
                                .setNegativeButton(android.R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                m_activity.finish();
                                            }
                                        }).create().show();

                }
            }
        });
        mapFragment.addOnMapRenderListener(new OnMapRenderListener() {
            @Override
            public void onPreDraw() {
                if (m_positionIndicatorFixed != null) {
                    if (NavigationManager.getInstance()
                            .getMapUpdateMode().equals(NavigationManager
                                    .MapUpdateMode.ROADVIEW)) {
                        if (!m_returningToRoadViewMode) {
                            // when road view is active, we set the position indicator to align
                            // with the current map transform center to synchronize map and map
                            // marker movements.
                            m_positionIndicatorFixed
                                    .setCoordinate(map.pixelToGeo(m_mapTransformCenter));
                        }
                    }
                }
            }

            @Override
            public void onPostDraw(boolean b, long l) {

            }

            @Override
            public void onSizeChanged(int i, int i1) {

            }

            @Override
            public void onGraphicsDetached() {

            }

            @Override
            public void onRenderBufferCreated() {

            }
        });
    }

    private PositioningManager.OnPositionChangedListener mapPositionHandler = new PositioningManager.OnPositionChangedListener() {
        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, @Nullable GeoPosition geoPosition, boolean b) {
            final GeoCoordinate coordinate = geoPosition.getCoordinate();
            if (mTransforming) {
                mPendingUpdate = new Runnable() {
                    @Override
                    public void run() {
                        onPositionUpdated(locationMethod, geoPosition, b);
                    }
                };
            } else {
                //map.setCenter(coordinate, Map.Animation.BOW);
                calculateAndStartNavigation(locationMethod, geoPosition);
            }
        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

        }
    };
    final private NavigationManager.RoadView.Listener roadViewListener = new NavigationManager.RoadView.Listener() {
        @Override
        public void onPositionChanged(GeoCoordinate geoCoordinate) {
            // an active RoadView provides coordinates that is the map transform center of it's
            // movements.
            m_mapTransformCenter = map.projectToPixel
                    (geoCoordinate).getResult();
        }
    };
    final private Map.OnTransformListener onTransformListener = new Map.OnTransformListener() {
        @Override
        public void onMapTransformStart() {
        }

        @Override
        public void onMapTransformEnd(MapState mapsState) {
            // do not start RoadView and its listener until moving map to current position has
            // completed
            if (m_returningToRoadViewMode) {
                NavigationManager.getInstance().setMapUpdateMode(NavigationManager.MapUpdateMode
                        .ROADVIEW);
                NavigationManager.getInstance().getRoadView()
                        .addListener(new WeakReference<>(roadViewListener));
                m_returningToRoadViewMode = false;
            }
        }
    };

    final private NavigationManager.NavigationManagerEventListener navigationManagerEventListener =
            new NavigationManager.NavigationManagerEventListener() {
                @Override
                public void onRouteUpdated(Route route) {
                    map.removeMapObject(m_currentRoute);
                    m_currentRoute = new MapRoute(route);
                    map.addMapObject(m_currentRoute);
                }
            };

    final private NavigationManager.LaneInformationListener
            m_laneInformationListener = new NavigationManager.LaneInformationListener() {
        @Override
        public void onLaneInformation(@NonNull List<LaneInformation> items,
                                      @Nullable RoadElement roadElement) {
            LaneInfoUtils.displayLaneInformation(m_laneInfoView, items);
        }
    };



    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                for (int index = 0; index < permissions.length; index++) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {

                        /*
                         * If the user turned down the permission request in the past and chose the
                         * Don't ask again option in the permission request system dialog.
                         */
                        if (!ActivityCompat
                                .shouldShowRequestPermissionRationale(this, permissions[index])) {
                            Toast.makeText(this, "Required permission " + permissions[index]
                                            + " not granted. "
                                            + "Please go to settings and turn on for sample app",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "Required permission " + permissions[index]
                                    + " not granted", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                initialize();
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }




    private void calculateAndStartNavigation(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition) {

        final GeoCoordinate coord = geoPosition.getCoordinate();

        if (map == null) {
            Toast.makeText(m_activity, "Map is not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }
        if (NavigationManager.getInstance().getRunningState()
                == NavigationManager.NavigationState.RUNNING) {

            return;
        }
        final RoutePlan routePlan = new RoutePlan();

        // these two waypoints cover suburban roads
        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(coord.getLatitude(),coord.getLongitude())));
        //  routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(13.676660, 100.603582)));
        routePlan.addWaypoint(new RouteWaypoint(new GeoCoordinate(13.648732, 100.681736)));



        // calculate a route for navigation
        CoreRouter coreRouter = new CoreRouter();
        coreRouter.calculateRoute(routePlan, new CoreRouter.Listener() {
            @Override
            public void onCalculateRouteFinished(List<RouteResult> list,
                                                 RoutingError routingError) {
                if (routingError == RoutingError.NONE) {
                    Route route = list.get(0).getRoute();

                    m_currentRoute = new MapRoute(route);
                    map.addMapObject(m_currentRoute);



                    // setting MapUpdateMode to RoadView will enable automatic map
                    // movements and zoom level adjustments
                    NavigationManager navigationManager =
                            NavigationManager.getInstance();
                    navigationManager.setMapUpdateMode(
                            NavigationManager.MapUpdateMode.ROADVIEW);

                    // adjust tilt to show 3D view
                    map.setTilt(80);

                    // adjust transform center for navigation experience in portrait
                    // view
                    m_mapTransformCenter = new PointF(map.getTransformCenter().x, (map
                            .getTransformCenter().y * 85 / 50));
                    map.setTransformCenter(m_mapTransformCenter);

                    // create a map marker to show current position
                    Image icon = new Image();
                    m_positionIndicatorFixed = new MapMarker();
                    try {
                        icon.setImageResource(R.drawable.gps_position);
                        m_positionIndicatorFixed.setIcon(icon);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    m_positionIndicatorFixed.setVisible(true);
                    m_positionIndicatorFixed.setCoordinate(map.getCenter());
                    map.addMapObject(m_positionIndicatorFixed);

                    mapFragment.getPositionIndicator().setVisible(false);

                    navigationManager.setMap(map);

                    // listen to real position updates. This is used when RoadView is
                    // not active.
                    PositioningManager.getInstance().addListener(
                            new WeakReference<>(mapPositionHandler));

                    // listen to updates from RoadView which tells you where the map
                    // center should be situated. This is used when RoadView is active.
                    navigationManager.getRoadView().addListener(
                            new WeakReference<>(roadViewListener));

                    // listen to navigation manager events.
                    navigationManager.addNavigationManagerEventListener(
                            new WeakReference<>(
                                    navigationManagerEventListener));

                    navigationManager.addLaneInformationListener(
                            new WeakReference<>(m_laneInformationListener));

                    // start navigation simulation travelling at 13 meters per second
                   // navigationManager.simulate(route, 100);
                    navigationManager.startNavigation(route);


                } else {
                    Toast.makeText(m_activity,
                            "Error:route calculation returned error code: " + routingError,
                            Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onProgress(int i) {

            }
        });
    }

    @Override
    public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, @Nullable GeoPosition geoPosition, boolean b) {
        final GeoCoordinate coordinate = geoPosition.getCoordinate();
        if (mTransforming) {
            mPendingUpdate = new Runnable() {
                @Override
                public void run() {
                    onPositionUpdated(locationMethod, geoPosition, b);
                }
            };
        } else {
           // map.setCenter(coordinate, Map.Animation.BOW);
            calculateAndStartNavigation(locationMethod, geoPosition);
        }
    }

    @Override
    public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

    }

    @Override
    public void onMapTransformStart() {

    }

    @Override
    public void onMapTransformEnd(MapState mapState) {
        if (m_returningToRoadViewMode) {
            NavigationManager.getInstance().setMapUpdateMode(NavigationManager.MapUpdateMode
                    .ROADVIEW);
            NavigationManager.getInstance().getRoadView()
                    .addListener(new WeakReference<>(roadViewListener));
            m_returningToRoadViewMode = false;
        }
    }
    private void pauseRoadView() {
        // pause RoadView so that map will stop moving, the map marker will use updates from
        // PositionManager callback to update its position.

        if (NavigationManager.getInstance().getMapUpdateMode().equals(NavigationManager.MapUpdateMode.ROADVIEW)) {
            NavigationManager.getInstance().setMapUpdateMode(NavigationManager.MapUpdateMode.NONE);
            NavigationManager.getInstance().getRoadView().removeListener(roadViewListener);
            m_lastZoomLevelInRoadViewMode = map.getZoomLevel();
        }
    }

    // application design suggestion: pause roadview when user gesture is detected.
    private MapGesture.OnGestureListener gestureListener = new MapGesture.OnGestureListener() {
        @Override
        public void onPanStart() {
            pauseRoadView();
        }

        @Override
        public void onPanEnd() {
        }

        @Override
        public void onMultiFingerManipulationStart() {
        }

        @Override
        public void onMultiFingerManipulationEnd() {
        }

        @Override
        public boolean onMapObjectsSelected(List<ViewObject> objects) {
            return false;
        }

        @Override
        public boolean onTapEvent(PointF p) {
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(PointF p) {
            return false;
        }

        @Override
        public void onPinchLocked() {
        }

        @Override
        public boolean onPinchZoomEvent(float scaleFactor, PointF p) {
            pauseRoadView();
            return false;
        }

        @Override
        public void onRotateLocked() {
        }

        @Override
        public boolean onRotateEvent(float rotateAngle) {
            return false;
        }

        @Override
        public boolean onTiltEvent(float angle) {
            pauseRoadView();
            return false;
        }

        @Override
        public boolean onLongPressEvent(PointF p) {
            return false;
        }

        @Override
        public void onLongPressRelease() {
        }

        @Override
        public boolean onTwoFingerTapEvent(PointF p) {
            return false;
        }
    };
    @Override
    public void onDestroy(){
        if (map != null) {
            map.removeMapObject(m_positionIndicatorFixed);
        }
        if (MapEngine.isInitialized()) {
            NavigationManager.getInstance().stop();
            PositioningManager.getInstance().stop();
        }

        NavigationManager.getInstance().removeLaneInformationListener(m_laneInformationListener);
        NavigationManager.getInstance()
                .removeNavigationManagerEventListener(navigationManagerEventListener);
        super.onDestroy();
    }

}