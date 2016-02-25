package pl.tajchert.buswear.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import pl.tajchert.buswear.wear.EventBus;

public class MainMobileActivity extends AppCompatActivity {

    private EditText editTextToSend;
    private Button buttonEverywhere;
    private Button buttonLocal;
    private Button buttonRemote;
    private Button buttonRemoteRandom;
    private Random rand = new Random();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault(this).register(this);
        editTextToSend = (EditText) findViewById(R.id.editTextToSend);
        buttonEverywhere = (Button) findViewById(R.id.buttonEverywhere);
        buttonLocal = (Button) findViewById(R.id.buttonLocal);
        buttonRemote = (Button) findViewById(R.id.buttonRemote);
        buttonRemoteRandom = (Button) findViewById(R.id.buttonRemoteRandom);

        setButtons();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault(this).unregister(this);
        super.onDestroy();
    }

    private void setButtons() {
        buttonEverywhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send Custom object to both local and remote EventBus
                EventBus.getDefault(v.getContext()).postGlobal(new CustomObject(editTextToSend.getText().toString()));
            }
        });
        buttonLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send Custom object only to local EventBus
                EventBus.getDefault(v.getContext()).post(new CustomObject(editTextToSend.getText().toString()));
            }
        });
        buttonRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send Custom object only to remote EventBus
                EventBus.getDefault(v.getContext()).postRemote(new CustomObject(editTextToSend.getText().toString()));
            }
        });

        buttonRemoteRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Object> messages = Arrays.<Object>asList(editTextToSend.getText().toString(), 1, 1L, 1.0f, 1.0, (short) 1);
                Object random = messages.get(rand.nextInt(messages.size()));
                EventBus.getDefault(v.getContext()).postGlobal(random);
            }
        });
    }

    /**
     * It receives events from event bus, also from Wear device if it send everywhere or remote.
     *
     * @param customObject
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCustomObject(CustomObject customObject) {
        Toast.makeText(this, "Object: " + customObject.getName(), Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onString(String stringReceived) {
        Toast.makeText(this, "String: " + stringReceived, Toast.LENGTH_SHORT).show();
    }
}
