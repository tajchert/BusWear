package pl.tajchert.buswear.sample;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import pl.tajchert.buswear.EventBus;


public class MainMobileActivity extends ActionBarActivity {
    private EditText editTextToSend;
    private Button buttonEverywhere;
    private Button buttonLocal;
    private Button buttonRemote;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        editTextToSend = (EditText) findViewById(R.id.editTextToSend);
        buttonEverywhere = (Button) findViewById(R.id.buttonEverywhere);
        buttonLocal = (Button) findViewById(R.id.buttonLocal);
        buttonRemote = (Button) findViewById(R.id.buttonRemote);

        setButtons();
    }

    private void setButtons() {
        buttonEverywhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new CustomObject(editTextToSend.getText().toString()), MainMobileActivity.this);
            }
        });
        buttonLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postLocal(new CustomObject(editTextToSend.getText().toString()));
            }
        });
        buttonRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postRemote(new CustomObject(editTextToSend.getText().toString()), MainMobileActivity.this);
            }
        });
    }

    public void onEvent(CustomObject customObject) {
        Toast.makeText(MainMobileActivity.this, "Received: " + customObject.getName(), Toast.LENGTH_SHORT).show();
    }
}
