package cf.charly1811.android.sample.casttest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;


public class MainActivity extends ActionBarActivity {
    public static String TAG = MainActivity.class.getSimpleName();

    public static final String APP_ID = "F6D3E50B";

    MediaRouter mediaRouter;
    MediaRouteSelector mediaRouteSelector;
    MediaRouter.Callback mMediaRouterCallback;
    CastDevice device;
    Cast.CastOptions.Builder castOptionsBuilder;
    GoogleApiClient apiClient;
    Cast.Listener castClientListener;
    MyConnectionCallback connectionCallback ;
    MyConnectionFailedListener connectionFailedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Click on the Cast Button to start the Hello World app");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        mediaRouter = MediaRouter.getInstance(this);
        mediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID))
                .build();

        mMediaRouterCallback = new MyMediaRouterCallback();

        castClientListener = new Cast.Listener() {
            @Override
            public void onApplicationStatusChanged() {
                super.onApplicationStatusChanged();
                Log.d(TAG, "Statut changed");
                
            }

            @Override
            public void onApplicationDisconnected(int statusCode) {
                super.onApplicationDisconnected(statusCode);
                Log.d(TAG, "Application disconnected (Code: "+statusCode+")");
            }
        };

    }


    @Override
    protected void onStart() {
        super.onResume();
        mediaRouter.addCallback(mediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaRouter.removeCallback(mMediaRouterCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem media_route_item = menu.findItem(R.id.media_route_item);
        MediaRouteActionProvider actionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(media_route_item);
        actionProvider.setRouteSelector(mediaRouteSelector);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    // MediaRouterCallBack
    public class MyMediaRouterCallback extends MediaRouter.Callback
    {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteSelected(router, route);
            device = CastDevice.getFromBundle(route.getExtras());
            Log.d(TAG, "Connected to "+ route.getName());
            Toast.makeText(getApplicationContext(), "Connected to "+route.getName(), Toast.LENGTH_SHORT).show();

            castOptionsBuilder = Cast.CastOptions.builder(device, castClientListener);
            connectionCallback = new MyConnectionCallback();
            connectionFailedListener = new MyConnectionFailedListener();

            apiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(Cast.API, castOptionsBuilder.build())
                    .addConnectionCallbacks(connectionCallback)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .build();
            apiClient.connect();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteUnselected(router, route);
            device = null;
            Log.d(TAG, "Disconnected from "+ route.getName());
            Toast.makeText(getApplicationContext(), "Disconnected from "+route.getName(), Toast.LENGTH_SHORT).show();
        }
    }


    public class MyConnectionCallback implements GoogleApiClient.ConnectionCallbacks
    {

        @Override
        public void onConnected(Bundle bundle) {
            com.google.android.gms.cast.Cast.CastApi.launchApplication(apiClient, APP_ID);
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG, "Connection Suspended");
        }
    }

    public class MyConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener
    {

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG, "Connection failed. (Cause:"+connectionResult+")");
        }
    }
}
