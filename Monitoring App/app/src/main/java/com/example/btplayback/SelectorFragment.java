package com.example.btplayback;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SelectorFragment extends Fragment {

    private static final String TAG = "SelectorFragment";
    private Button btnAux,btnBt;
    private MainInterface mainInterface;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_selector,container,false);
        btnAux = view.findViewById(R.id.btnAux);
        btnBt = view.findViewById(R.id.btnBt);
        btnAux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainInterface.onAuxSelected();
            }
        });
        btnBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainInterface.onBtSelected();
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainInterface = (MainInterface) getActivity();
    }
}
