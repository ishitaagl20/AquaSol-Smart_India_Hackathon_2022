package com.example.btplayback;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class SongListFragment extends Fragment{

    private static final String TAG = "SongListFragment";
    private MainInterface mainInterface;

    //widgets
    private TextView ph, temperature, tds, oxygen, turbidity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_list,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ph = view.findViewById(R.id.ph);
        temperature = view.findViewById(R.id.temperature);
        tds = view.findViewById(R.id.tds);
        oxygen = view.findViewById(R.id.oxy);
        turbidity = view.findViewById(R.id.turb);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ph = temperature = tds = oxygen = turbidity = null;
    }

    public void updateTemp(String val){temperature.setText(val);}
    public void updateTds(String val){tds.setText(val);}
    public void updateph(String val){ph.setText(val);}
    public void updateoxy(String val){oxygen.setText(val);}
    public void updateturb(String val){turbidity.setText(val);}
}
