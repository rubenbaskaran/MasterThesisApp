package rubenkarim.com.masterthesisapp.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;

public class MainActivity extends AppCompatActivity
{
    private UsbDevice usbDevice;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: MAKE ME A SANDWICH
        //TODO: This is a test
    }

    public void ChooseAlgorithmOnClick(View view)
    {
        int idOfChosenAlgorithm = Integer.parseInt(String.valueOf(view.getTag()));
        GlobalVariables.Algorithms chosenAlgorithm = GlobalVariables.Algorithms.values()[idOfChosenAlgorithm];
        GlobalVariables.setCurrentAlgorithm(chosenAlgorithm);
        Log.e("Chosen algorithm", GlobalVariables.getCurrentAlgorithm().toString());

        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
    }

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(usbDevice != null){
                            //call method to set up device communication

                        }
                    }
                    else {
                        Log.d("CameraActivity", "permission denied for device " + usbDevice);
                    }
                }
            }
        }
    };
}
