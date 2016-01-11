package com.alimuzaffar.newvolleyproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final NetworkImageView networkedImageView = (NetworkImageView) findViewById(R.id.imageView);
        final TextView textView = (TextView) findViewById(R.id.textView);

        VolleySingleton volleySingleton = VolleySingleton.getInstance(this);
        networkedImageView.setImageUrl("http://www.gravatar.com/avatar/e25a0239336112225f6d08f30703b615?s=200", volleySingleton.getImageLoader());
        volleySingleton
                .add(new StringRequest(Request.Method.GET, "https://medium.com/@ali.muzaffar", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        textView.setText(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }));
    }
}
