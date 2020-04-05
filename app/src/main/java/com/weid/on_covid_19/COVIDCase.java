package com.weid.on_covid_19;

import com.google.android.gms.maps.model.LatLng;

public class COVIDCase {
    LatLng coor;
    String status;
    String info;
    String name;

    public COVIDCase(LatLng coor, String status, String info, String name) {
        this.coor = coor;
        this.status = status;
        this.info = info;
        this.name = name;
    }
}
