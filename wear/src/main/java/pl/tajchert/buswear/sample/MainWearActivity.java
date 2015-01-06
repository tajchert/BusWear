package pl.tajchert.buswear.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import pl.tajchert.buswear.EventBus;


public class MainWearActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView clickToSend = (TextView) findViewById(R.id.textViewClickToSend);
        clickToSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CustomObject("Send from Wear"), MainWearActivity.this);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(CustomObject customObject){
        Toast.makeText(MainWearActivity.this, customObject.getName(), Toast.LENGTH_SHORT).show();
    }
}
