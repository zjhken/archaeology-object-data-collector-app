// Manual Search
// @author: Andrej Ilic, Ben Greenberg, Anton Relin, and Tristrum Tuttle
package cis573.com.archaeology.ui;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import cis573.com.archaeology.R;
import cis573.com.archaeology.services.UpdateDatabase;
import cis573.com.archaeology.services.UpdateDatabaseMuseum;
import cis573.com.archaeology.util.HistoryHelper;
import cis573.com.archaeology.util.LocalRetriever;
public class ManualActivity extends AppCompatActivity
{
    HistoryHelper myDatabase;
    private LocalRetriever localRetriever;
    /**
     * Launch the activity
     * @param savedInstanceState - state from memory
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        UpdateDatabase uD = new UpdateDatabaseMuseum();
        if (!uD.updateNecessary())
        {
            uD.doUpdate(this);
        }
        myDatabase = new HistoryHelper(this);
        if (getIntent() != null)
        {
            Bitmap bmp = getIntent().getParcelableExtra("preview");
            ImageView iv = (ImageView) findViewById(R.id.prev);
            EditText et = (EditText) findViewById(R.id.editText);
            et.setText(getIntent().getStringExtra("search"));
            iv.setImageBitmap(bmp);
            iv.setVisibility(View.VISIBLE);
            et.requestFocus();
        }
        String[] allCodes = new String[0];
        // Creating the instance of ArrayAdapter containing list of language names
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.select_dialog_item, allCodes);
        // Getting the instance of AutoCompleteTextView
        AutoCompleteTextView autocomplete = (AutoCompleteTextView) findViewById(R.id.autocomplete);
        // will start working from first character
        autocomplete.setThreshold(1);
        // setting the adapter data into the AutoCompleteTextView
        autocomplete.setAdapter(adapter);
        autocomplete.setTextColor(Color.BLACK);
        localRetriever = new LocalRetriever(getApplicationContext(), uD.getDatabaseLocation());
    }

    /**
     * Click the screen
     * @param view - window
     */
    public void onClick(View view)
    {
        EditText t = (EditText) findViewById(R.id.editText);
        Intent intent = new Intent(this, SearchActivity.class);
        String search = t.getText().toString();
        // Abstract data retrieval
        String[] list = localRetriever.search(search);
        // Add all possible search features
        intent.putExtra("search", list[2]);
        intent.putExtra("searchnumber", list[0]);
        intent.putExtra("searchname", list[1]);
        intent.putExtra("searchdescription", list[3]);
        intent.putExtra("searchprovenience", list[4]);
        intent.putExtra("searchmaterial", list[5]);
        intent.putExtra("searchcuratorial_section", list[6]);
        // Add to history
        myDatabase.insertSearch(t.getText().toString(), list[1], list[2], list[3], list[4], list[5],
                list[6]);
        startActivity(intent);
    }
}