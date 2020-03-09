package rubenkarim.com.masterthesisapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.live.Camera;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatus;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.live.discovery.DiscoveryFactory;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Camera cameraInstance = new Camera();
        ConnectionStatusListener listener = new ConnectionStatusListener() {
            @Override
            public void onConnectionStatusChanged(ConnectionStatus connectionStatus, ErrorCode errorCode) {
                if (connectionStatus == ConnectionStatus.CONNECTED) { // we are connected

                }
            }
        };

        DiscoveryEventListener aDiscoveryEventListener = new DiscoveryEventListener() {
            @Override
            public void onCameraFound(Identity identity) {
                Log.d("CAMERA_ACTIVITY", "onCameraFound: " + identity);

            }

            @Override
            public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
                Log.d("CAMERA_ACTIVITY", "Camera error");
            }

            @Override
            public void onCameraLost(Identity identity) {
                Log.d("CAMERA_ACTIVITY", "Camera Lost");

            }
        };
        DiscoveryFactory.getInstance().scan(aDiscoveryEventListener, CommunicationInterface.USB);

    }

    public void BackOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void TakePictureOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        startActivity(intent);
    }
}
