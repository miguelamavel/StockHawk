package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID_DETAIL_LOADER = 242;

    private static final String[] STOCK_DETAIL_PROJECTION = {
            Contract.Quote.COLUMN_SYMBOL,
            Contract.Quote.COLUMN_PRICE,
            Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
            Contract.Quote.COLUMN_PERCENTAGE_CHANGE,
            Contract.Quote.COLUMN_HISTORY,
    };
    private static final int INDEX_SYMBOL = 0;
    private static final int INDEX_PRICE = 1;
    private static final int INDEX_ABSOLUTE_CHANGE = 2;
    private static final int INDEX_PERCENTAGE_CHANGE = 3;
    private static final int INDEX_HISTORY = 4;

    private static final int NUMBER_X_LABELS = 10;
    private static final int NUMBER_Y_LABELS = 5;

    private Uri mUri;
    private DecimalFormat mDollarFormatWithPlus;
    private DecimalFormat mPercentageFormat;

    @BindView(R.id.activity_detail_current_price)
    TextView mPriceTextView;
    @BindView(R.id.activity_detail_abs_change)
    TextView mAbsChangeTextView;
    @BindView(R.id.activity_detail_percentage_change)
    TextView mPercentageChangeTextView;

    @BindView(R.id.activity_detail_plot)
    XYPlot mHistoryPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        mDollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        mDollarFormatWithPlus.setPositivePrefix("+$");
        mPercentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        mPercentageFormat.setMaximumFractionDigits(2);
        mPercentageFormat.setMinimumFractionDigits(2);
        mPercentageFormat.setPositivePrefix("+");

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {

        switch (loaderId) {
            case ID_DETAIL_LOADER:
                return new CursorLoader(this,
                        mUri,
                        STOCK_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        String symbol = data.getString(INDEX_SYMBOL);

        if (getSupportActionBar()!=null)
            getSupportActionBar().setTitle(symbol);

        mPriceTextView.setText(String.format(Locale.US, "%.2f", data.getDouble(INDEX_PRICE)));
        mAbsChangeTextView.setText(mDollarFormatWithPlus.format(data.getDouble(INDEX_ABSOLUTE_CHANGE)));
        mPercentageChangeTextView.setText(mPercentageFormat.format(data.getDouble(INDEX_PERCENTAGE_CHANGE)/100));

        String history = data.getString(INDEX_HISTORY);
        String[] values = history.split("\n");

        final List<String> xDates = new ArrayList<>();
        List<Number> yPrices = new ArrayList<>();
        Format format = new SimpleDateFormat("MMM dd", Locale.US);

        for (String value : values) {
            String[] datePrice = value.split(", ");

            xDates.add(format.format(new Date(Long.valueOf(datePrice[0]))));
            yPrices.add(Double.valueOf(datePrice[1]));
        }

        final List<String> lastDates = new ArrayList<>();
        final List<Number> lastPrices = new ArrayList<>();
        for (int i = NUMBER_X_LABELS -1; i>=0 && lastDates.size()< NUMBER_X_LABELS; i--) {
            lastDates.add(xDates.get(i));
            lastPrices.add(yPrices.get(i));
        }

        XYSeries historySeries = new SimpleXYSeries(lastPrices, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
        LineAndPointFormatter historySeriesFormat =
                new LineAndPointFormatter(this, R.xml.line_point_formatter_with_labels);

        historySeriesFormat.setPointLabeler(new PointLabeler() {
            @Override
            public String getLabel(XYSeries series, int index) {
                return null;
            }
        });
        mHistoryPlot.addSeries(historySeries, historySeriesFormat);

        mHistoryPlot.setRangeStep(StepMode.SUBDIVIDE, NUMBER_Y_LABELS);
        mHistoryPlot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            @Override
            public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {
                int i = Math.round(((Number) obj).floatValue());
                return i%2==1 ? toAppendTo.append(lastDates.get(i)) : toAppendTo.append("");
            }

            @Override
            public Object parseObject(String source, @NonNull ParsePosition pos) {
                return null;
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
