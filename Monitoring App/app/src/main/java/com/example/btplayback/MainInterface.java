package com.example.btplayback;

public interface MainInterface {

    void onAuxSelected();
    void onBtSelected();
    void onBTConnected();
    void sendData(String val);

    void updateTemp(String val);
    void updatePh(String val);
    void updateOxy(String val);
    void updateTurb(String val);
    void updatetds(String val);
}
