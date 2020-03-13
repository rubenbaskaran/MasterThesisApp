package rubenkarim.com.masterthesisapp.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;

public class MarkerActivity extends AppCompatActivity
{
    ImageView imageView_markerImage;
    String filename = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "default_picture";
    String marker = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "marker";
    ImageView imageView_markerOne;
    ImageView imageView_markerTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        imageView_markerImage = findViewById(R.id.imageView_markerImage);
        imageView_markerOne = findViewById(R.id.imageView_markerOne);
        imageView_markerTwo = findViewById(R.id.imageView_markerTwo);
        SetOnTouchListener(imageView_markerOne);
        SetOnTouchListener(imageView_markerTwo);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("filename"))
        {
            filename = receivedIntent.getStringExtra("filename");
        }

        SetPicture();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void SetOnTouchListener(ImageView img)
    {
        img.setOnTouchListener(new View.OnTouchListener()
        {
            PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
            PointF StartPT = new PointF(); // Record Start Position of 'img'

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_MOVE:
                        img.setX((int) (StartPT.x + event.getX() - DownPT.x));
                        img.setY((int) (StartPT.y + event.getY() - DownPT.y));
                        StartPT.set(img.getX(), img.getY());
                        break;
                    case MotionEvent.ACTION_DOWN:
                        DownPT.set(event.getX(), event.getY());
                        StartPT.set(img.getX(), img.getY());
                        break;
                    case MotionEvent.ACTION_UP:
                        GetCoordinates(img);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void SetPicture()
    {
        imageView_markerImage.setImageURI(Uri.parse(filename));
        imageView_markerOne.setImageURI(Uri.parse(marker));
        imageView_markerTwo.setImageURI(Uri.parse(marker));
    }

    private void GetCoordinates(ImageView imageView)
    {
        int[] coordinates = new int[2];
        imageView.getLocationOnScreen(coordinates);
        int x = coordinates[0];
        int y = coordinates[1];
        Log.e(String.valueOf(imageView.getTag()), "x: " + x + ", y: " + y);
        GetPixelColor(x, y);
    }

    private void GetPixelColor(int x, int y)
    {
        final Bitmap bitmap = ((BitmapDrawable) imageView_markerImage.getDrawable()).getBitmap();
        int targetPixel = bitmap.getPixel(x, y);
        Log.e("Pixel color", Color.red(targetPixel) + "," + Color.green(targetPixel) + "," + Color.blue(targetPixel));
    }

    public void BackOnClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
    }

    public void SubmitOnClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
        intent.putExtra("filename", filename);
        startActivity(intent);
    }
}