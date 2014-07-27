package de.bitdroid.flooding.utils;

import android.test.AndroidTestCase;

import com.github.amlcurran.showcaseview.ShowcaseView;

public class ShowcaseSeriesTest extends AndroidTestCase {

    private int counter = 0;

    @Override
    public void setUp() {
        counter = 0;
    }

    public void testEmptyShowcases() {

        ShowcaseSeries series = new ShowcaseSeries() {
            @Override
            public ShowcaseView getShowcase(int id) {
                assertTrue(id == 0);
                counter++;
                return null;
            }
        };

        series.start();
        assertEquals(1, counter);
    }

}
