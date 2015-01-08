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

    //They are just samples, you just implement those "onEvent()" which you use to post, and correct method will be called

    public void onEvent(CustomObject customObjectReceived){
        Toast.makeText(MainWearActivity.this, "Object: " + customObjectReceived.getName(), Toast.LENGTH_SHORT).show();
    }

    public void onEvent(String stringReceived){
        Toast.makeText(MainWearActivity.this, "String: " + stringReceived, Toast.LENGTH_SHORT).show();
    }

    public void onEvent(Float floatReceived){
        Toast.makeText(MainWearActivity.this, "Received Float", Toast.LENGTH_SHORT).show();
    }

    public void onEvent(Double doubleReceived){
        Toast.makeText(MainWearActivity.this, "Received Double", Toast.LENGTH_SHORT).show();
    }

    public void onEvent(Long longReceived){
        Toast.makeText(MainWearActivity.this, "Received Long", Toast.LENGTH_SHORT).show();
    }

    public void onEvent(Integer integerReceived){
        Toast.makeText(MainWearActivity.this, "Received Integer", Toast.LENGTH_SHORT).show();
    }

    public void onEvent(Short shortReceived){
        Toast.makeText(MainWearActivity.this, "Received Short", Toast.LENGTH_SHORT).show();
    }
}
