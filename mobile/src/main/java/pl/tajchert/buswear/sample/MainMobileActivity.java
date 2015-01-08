package pl.tajchert.buswear.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Random;

import pl.tajchert.buswear.EventBus;


public class MainMobileActivity extends ActionBarActivity {
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
        EventBus.getDefault().register(this);
        editTextToSend = (EditText) findViewById(R.id.editTextToSend);
        buttonEverywhere = (Button) findViewById(R.id.buttonEverywhere);
        buttonLocal = (Button) findViewById(R.id.buttonLocal);
        buttonRemote = (Button) findViewById(R.id.buttonRemote);
        buttonRemoteRandom = (Button) findViewById(R.id.buttonRemoteRandom);

        setButtons();
    }

    private void setButtons() {
        buttonEverywhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send Custom object to both local and remote EventBus
                EventBus.getDefault().post(new pl.tajchert.buswear.sample.CustomObject(editTextToSend.getText().toString()), MainMobileActivity.this);
            }
        });
        buttonLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send Custom object only to local EventBus
                EventBus.getDefault().postLocal(new CustomObject(editTextToSend.getText().toString()));
            }
        });
        buttonRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send Custom object only to remote EventBus
                EventBus.getDefault().postRemote(new CustomObject(editTextToSend.getText().toString()), MainMobileActivity.this);
            }
        });

        buttonRemoteRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(rand.nextInt(6)){
                    case 0:
                        //Send String object
                        EventBus.getDefault().post(editTextToSend.getText().toString(), MainMobileActivity.this);
                        break;
                    case 1:
                        //Send Integer
                        EventBus.getDefault().post(1, MainMobileActivity.this);
                        break;
                    case 2:
                        //Send Long
                        EventBus.getDefault().post(1l, MainMobileActivity.this);
                        break;
                    case 3:
                        //Send Float
                        EventBus.getDefault().post(1f, MainMobileActivity.this);
                        break;
                    case 4:
                        //Send Double
                        EventBus.getDefault().post(1d, MainMobileActivity.this);
                        break;
                    case 5:
                        //Send Short
                        EventBus.getDefault().post((short) 1, MainMobileActivity.this);
                        break;
                }
            }
        });
    }

    /**
     * It receives events from event bus, also from Wear device if it send everywhere or remote.
     * @param customObject
     */
    public void onEvent(CustomObject customObject) {
        Toast.makeText(MainMobileActivity.this, "Object: " + customObject.getName(), Toast.LENGTH_SHORT).show();
    }
    public void onEvent(String stringReceived) {
        Toast.makeText(MainMobileActivity.this, "String: " + stringReceived, Toast.LENGTH_SHORT).show();
    }
}
