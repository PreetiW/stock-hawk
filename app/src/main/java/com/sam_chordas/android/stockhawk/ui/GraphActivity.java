package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GraphActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Cursor>
{
    @BindView(R.id.line_graph) LineChartView lineGraph;
    LineSet dataset;
    private static final int GRAPH_LOADER = 0;
    private String selectedSymbol;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        initUI();
    }

    private void initUI()
    {
        ButterKnife.bind(this);

        if(getIntent().getExtras() != null)
        {
            selectedSymbol = getIntent().getExtras().getString(Utils.SELECTED_SYMBOL);
        }

        getSupportLoaderManager().initLoader(GRAPH_LOADER, null, this);


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.SYMBOL + " = ?", new String[]{selectedSymbol},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        showChat(data);
    }

    private void showChat(Cursor data)
    {
        if (data.getCount() != 0)
        {
            LineSet lineSet = new LineSet();
            float minimumPrice = Float.MAX_VALUE;
            float maximumPrice = Float.MIN_VALUE;

            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext())
            {
                String label = data.getString(data.getColumnIndexOrThrow(QuoteColumns.BIDPRICE));
                float price = Float.parseFloat(label);

                lineSet.addPoint(label, price);
                minimumPrice = Math.min(minimumPrice, price);
                maximumPrice = Math.max(maximumPrice, price);
            }

            lineSet.setColor(ContextCompat.getColor(this, R.color.material_blue_500)) .setThickness(4);
            lineGraph.setBorderSpacing(Tools.fromDpToPx(16)).setYLabels(AxisController.LabelPosition.OUTSIDE).setXLabels(AxisController.LabelPosition.NONE).setLabelsColor(ContextCompat.getColor(this, R.color.material_blue_500)).setXAxis(false).setYAxis(false).setAxisBorderValues(Math.round(Math.max(0f, minimumPrice - 5f)), Math.round(maximumPrice + 5f)).addData(lineSet);

            Animation anim = new Animation();

            if (lineSet.size() > 1)
            {
                lineGraph.show(anim);
            }
            else
            {
                Toast.makeText(this, "No data found for this symbol", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }
}
