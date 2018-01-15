package ch.epfl.esl.blankphonewearapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Claudio on 10.01.2018.
 */

public class FirstFragmentSwipe extends Fragment {

    private static final String TAG = "FirstFragmentSwipe";

    private TextView avg1;
    private TextView avg2;
    private TextView avg3;
    private TextView avg4;
    private TextView avg5;

    private TextView txtAvg1;
    private TextView txtAvg2;
    private TextView txtAvg3;
    private TextView txtAvg4;
    private TextView txtAvg5;

    public static FirstFragmentSwipe newInstance(){
        return new FirstFragmentSwipe();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.swipelayout1, container, false);

        avg1 = view.findViewById(R.id.pwrAvgNmbview1);
        avg2 = view.findViewById(R.id.pwrAvgNmbview2);
        avg3 = view.findViewById(R.id.pwrAvgNmbview3);
        avg4 = view.findViewById(R.id.pwrAvgNmbview4);
        avg5 = view.findViewById(R.id.pwrAvgNmbview5);

        txtAvg1 = view.findViewById(R.id.pwrAvgView1);
        txtAvg2 = view.findViewById(R.id.pwrAvgView2);
        txtAvg3 = view.findViewById(R.id.pwrAvgView3);
        txtAvg4 = view.findViewById(R.id.pwrAvgView4);
        txtAvg5 = view.findViewById(R.id.pwrAvgView5);

        avg1.setTextColor(Color.RED);
        avg2.setTextColor(Color.GREEN);
        avg3.setTextColor(Color.BLUE);
        avg4.setTextColor(Color.YELLOW);
        avg5.setTextColor(Color.MAGENTA);

        txtAvg1.setTextColor(Color.RED);
        txtAvg2.setTextColor(Color.GREEN);
        txtAvg3.setTextColor(Color.BLUE);
        txtAvg4.setTextColor(Color.YELLOW);
        txtAvg5.setTextColor(Color.MAGENTA);

        return view;
    }

    public void setTextViewsAvg(Float[] avgs){
        this.avg1.setText(Float.toString(avgs[0]));
        this.avg2.setText(Float.toString(avgs[1]));
        this.avg3.setText(Float.toString(avgs[2]));
        this.avg4.setText(Float.toString(avgs[3]));
        this.avg5.setText(Float.toString(avgs[4]));
    }

    public void setTextViewsTxt(String[] text){
        this.txtAvg1.setText(text[0]);
        this.txtAvg2.setText(text[1]);
        this.txtAvg3.setText(text[2]);
        this.txtAvg4.setText(text[3]);
        this.txtAvg5.setText(text[4]);
    }

    public void setVisibilityTextview(int visibleItems){

        TextView[] textViewArrayAvg = {avg1,avg2,avg3,avg4,avg5};
        TextView[] textViewArrayTxt = {txtAvg1,txtAvg2,txtAvg3,txtAvg4,txtAvg5};

        for(int i=0;i<visibleItems;i++){
            textViewArrayAvg[i].setVisibility(View.VISIBLE);
            textViewArrayTxt[i].setVisibility(View.VISIBLE);
        }






    }



}

