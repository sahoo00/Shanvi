package com.shanvi.android.shanvi;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.sql.Time;

/**
 * Created by Debashis on 3/13/2018.
 */

/**
 * This class uses external library AChartEngine to show dynamic real time line graph for HR values
 */
public class LineGraphView {

    private static final String TAG = LineGraphView.class.getSimpleName();

    //TimeSeries will hold the data in x,y format for single chart
    private TimeSeries mSeries = new TimeSeries("Breathing pattern");
    private TimeSeries mScores = new TimeSeries("Score");
    //XYMultipleSeriesDataset will contain all the TimeSeries
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    //XYMultipleSeriesRenderer will contain all XYSeriesRenderer and it can be used to set the properties of whole Graph
    private XYMultipleSeriesRenderer mMultiRenderer = new XYMultipleSeriesRenderer();
    private static LineGraphView mInstance = null;

    private int mPointsCount = 0;
    private final int MAX_POINTS = 100;

    /**
     * singleton implementation of LineGraphView class
     */
    public static synchronized LineGraphView getLineGraphView() {
        if (mInstance == null) {
            mInstance = new LineGraphView();
            Log.d(TAG, "New LineGraphView");
        }
        return mInstance;
    }

    /**
     * This constructor will set some properties of single chart and some properties of whole graph
     */
    public LineGraphView() {
        //add single line chart mSeries
        mDataset.addSeries(mSeries);
        mDataset.addSeries(mScores);

        //XYSeriesRenderer is used to set the properties like chart color, style of each point, etc. of single chart
        final XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
        //set line chart color to Black
        seriesRenderer.setColor(Color.BLACK);
        //set line chart style to square points
        seriesRenderer.setPointStyle(PointStyle.SQUARE);
        seriesRenderer.setFillPoints(true);

        final XYMultipleSeriesRenderer renderer = mMultiRenderer;
        //set whole graph background color to transparent color
        renderer.setBackgroundColor(Color.TRANSPARENT);
        renderer.setMargins(new int[] { 50, 65, 40, 5 }); // top, left, bottom, right
        renderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
        renderer.setAxesColor(Color.BLACK);
        renderer.setAxisTitleTextSize(24);
        renderer.setShowGrid(true);
        renderer.setGridColor(Color.LTGRAY);
        renderer.setLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.DKGRAY);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setYLabelsPadding(4.0f);
        renderer.setXLabelsColor(Color.DKGRAY);
        renderer.setLabelsTextSize(20);
        renderer.setLegendTextSize(20);
        //Disable zoom
        renderer.setPanEnabled(false, false);
        renderer.setZoomEnabled(false, false);
        //set title to x-axis and y-axis
        renderer.setXTitle("    Time (seconds)");
        renderer.setYTitle("            Signal");

        renderer.addSeriesRenderer(seriesRenderer);

        //XYSeriesRenderer is used to set the properties like chart color, style of each point, etc. of single chart
        final XYSeriesRenderer seriesRenderer2 = new XYSeriesRenderer();
        //set line chart color to Black
        seriesRenderer2.setColor(Color.RED);
        //set line chart style to square points
        seriesRenderer2.setPointStyle(PointStyle.SQUARE);
        seriesRenderer2.setFillPoints(true);

        renderer.addSeriesRenderer(seriesRenderer2);
        Log.d(TAG, "Create LineGraphView");
    }

    /**
     * return graph view to activity
     */
    public GraphicalView getView(Context context) {
        final GraphicalView graphView = ChartFactory.getLineChartView(context, mDataset, mMultiRenderer);
        Log.d(TAG, "getView" + graphView.toString());
        return graphView;
    }

    /**
     * add new x,y value to chart
     */
    public void addValue(Point p, Point s) {
        mSeries.add(p.x, p.y);
        mScores.add(s.x, s.y);
        if (mPointsCount > MAX_POINTS) {
            mSeries.remove(0);
            mScores.remove(0);
        }
        mPointsCount++;
    }

    /**
     * clear all previous values of chart
     */
    public void clearGraph() {
        mSeries.clear();
        mScores.clear();
    }

}
