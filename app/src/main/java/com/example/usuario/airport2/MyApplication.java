package com.example.usuario.airport2;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.MacAddress;
import com.estimote.sdk.Region;
import com.estimote.sdk.eddystone.Eddystone;
import com.estimote.sdk.connection.settings.Sensors;
import com.estimote.sdk.location.EstimoteLocation;
import com.estimote.sdk.telemetry.EstimoteTelemetry;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class MyApplication extends Application {

    //private BeaconManager beaconManager;
    private BeaconManager prueba;
    private Beacon nearestBeacon;
    private Eddystone nearestEddystone;
    private String mensaje;
    private String titulo;
    public double distance;
    public double distanciaA;
    public double distanciaB;
    public double distanciaC;
    public double acumuladoB = 0.0;
    public double acumuladoC = 0.0;
    public double exponente;
    public int contador = 0;
    public double acumulado = 0.0;
    public double acumulado_total = 0.0;
    public double[] acumulado_array;
    public double[] acumulado_total_array;
    public double[] exponente_array;
    public double[] distance_array;

    private static final UUID ESTIMOTE_PROXIMITY_UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("rid", ESTIMOTE_PROXIMITY_UUID, null, null);

    public String trilateracion_3_puntos(double a, double b, double c){
        String posicion = "";

        double d, i, j;
        double ax, ay, bx, by, cx, cy, X, Y;
        double apw, bpw, cpw;

        d = 1.900;
        i = 1.300;
        j = -1.40;

        //se definen las coordenadas de la Antena A
        ax = 0;
        ay = 0;
        //se define la cobertura Antena A
        apw = a;
        //se definen las coordenadas de la Antena B
        bx = d;
        by = 0;
        //se definen las coordenadas de la Antena C
        bpw = b;
        cx = i;
        cy = j;
        //se define la cobertura de la Antena c
        cpw = c;


        //se localiza la ubicacion del receptor
        X = (Math.pow(apw, 2) - Math.pow(bpw, 2) + Math.pow(d, 2))/(2*d);
        Y = ((Math.pow(apw, 2) - Math.pow(cpw, 2)+Math.pow(i, 2)+Math.pow(j, 2))/(2*j))-(((i/j))*X);
        //print "Tu estas ubicado en -> (%s,%s)" %(X, Y)
        posicion = "X: " +  String.format( "%.2f", X ) + "Y: " +  String.format( "%.2f", Y );

        return posicion;

    }

    @Override
    public void onCreate() {
        super.onCreate();

        prueba = new BeaconManager(getApplicationContext());
        acumulado_array = new double[4];
        acumulado_total_array = new double[4];
        exponente_array = new double[4];
        distance_array = new double[4];
        acumulado_array[0] = 0.0;
        acumulado_array[1] = 0.0;
        acumulado_array[2] = 0.0;
        acumulado_array[3] = 0.0;
        acumulado_total_array[0] = 0.0;
        acumulado_total_array[1] = 0.0;
        acumulado_total_array[2] = 0.0;
        acumulado_total_array[3] = 0.0;
        exponente_array[0] = 0.0;
        exponente_array[1] = 0.0;
        exponente_array[2] = 0.0;
        exponente_array[3] = 0.0;
        distance_array[0] = 0.0;
        distance_array[1] = 0.0;
        distance_array[2] = 0.0;
        distance_array[3] = 0.0;

        prueba.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
                mensaje = "";
                for (int i=0; i<beacons.size(); i++) {
                    //exponente = (beacons.get(i).getMeasuredPower()- beacons.get(i).getRssi()) / 20.0;
                    //distance = Math.pow(10d, exponente);
                    //distance = roundTwoDecimals(distance);
                    //mensaje = mensaje + Double.toString(exponente) + "\n";
                    //mensaje = mensaje + "B(" + i + "): " + String.format( "%.2f", distance ) ;
                    /*Log.i("BEACON", "beaconID: " + beacons.get(i).getMacAddress() +
                        ", rssi: " + beacons.get(i).getRssi() + ", MeasuredPower: " + beacons.get(i).getMeasuredPower()
                            + ", Distancia:" + distance);*/
                    /*
                    switch (i) {
                        case 0:  distanciaA = distance;
                            break;
                        case 1:  distanciaB = distance;
                            break;
                        case 2:  distanciaC = distance;
                            break;
                    }
                    */

                    /*
                    if (beacons.size() == 3) {
                        mensaje = trilateracion(distanciaA, distanciaB, distanciaC);
                        showNotification("Posicion", mensaje);
                    }
                    */

                    /* INICIO UBICACION 1 NODO */
                    if (beacons.size() == 1) {
                        contador = contador + 1;
                        acumulado = acumulado + beacons.get(i).getRssi();

                        if ((contador % 10) == 0) {
                            acumulado_total = acumulado_total + acumulado;
                            exponente = (beacons.get(i).getMeasuredPower() - acumulado_total / contador) / 20.0;
                            distance = Math.pow(10d, exponente);
                            Log.i("BEACON", "beaconID: " + beacons.get(i).getMacAddress() +
                                    ", Media: " + acumulado / 10 + ", rssi: " + beacons.get(i).getRssi() + ", MeasuredPower: "
                                    + beacons.get(i).getMeasuredPower() + ", Distancia: " + distance
                                    + ", Medita_total: " + acumulado_total / contador);
                            acumulado = 0.0;
                        }
                    }
                    /*FIN UBICACION 1 NODO*/

                    // UBICACION 3 o MAS NODOS
                    if (beacons.size() > 2) {
                        acumulado_array[i] = acumulado_array[i] + beacons.get(i).getRssi();
                    }
                    // UBICACION 3 O MAS NODOS
                }

                /* INICIO UBICACION 3 O MAS NODOS */
                if (beacons.size() > 2){
                    contador = contador + 1;
                    if ( (contador % 10) == 0 && beacons.size() > 2) {
                        for (int i=0; i<beacons.size(); i++) {
                            switch (beacons.get(i).getMajor()) {
                                case 3000:
                                    acumulado_total_array[0] = acumulado_total_array[0] + acumulado_array[0];
                                    exponente_array[0] = (beacons.get(0).getMeasuredPower() -  (acumulado_total_array[0]/contador)) / 20.0;
                                    distance_array[0] = Math.pow(10d, exponente_array[0]);
                                    //Log.i( "DIST", "Distancias(" + 0 + "): " + distance_array[0]);
                                    Log.i("BEACON", "beaconID: " + beacons.get(0).getMacAddress() +
                                            ", Media: "  + acumulado_array[0]/10.0  + ", rssi: " + beacons.get(0).getRssi() + ", MeasuredPower: "
                                            + beacons.get(0).getMeasuredPower() + ", Distancia: " + distance_array[0]
                                            + ", Medita_total: " + acumulado_total_array[0]/contador);
                                    break;
                                case 4000:
                                    acumulado_total_array[1] = acumulado_total_array[1] + acumulado_array[1];
                                    exponente_array[1] = (beacons.get(1).getMeasuredPower() -  (acumulado_total_array[1]/contador)) / 20.0;
                                    distance_array[1] = Math.pow(10d, exponente_array[1]);
                                    //Log.i( "DIST", "Distancias(" + 0 + "): " + distance_array[1]);
                                    Log.i("BEACON", "beaconID: " + beacons.get(1).getMacAddress() +
                                            ", Media: "  + acumulado_array[1]/10.0  + ", rssi: " + beacons.get(1).getRssi() + ", MeasuredPower: "
                                            + beacons.get(1).getMeasuredPower() + ", Distancia: " + distance_array[1]
                                            + ", Medita_total: " + acumulado_total_array[1]/contador);
                                    break;
                                case 5000:
                                    acumulado_total_array[2] = acumulado_total_array[2] + acumulado_array[2];
                                    exponente_array[2] = (beacons.get(2).getMeasuredPower() -  (acumulado_total_array[2]/contador)) / 20.0;
                                    distance_array[2] = Math.pow(10d, exponente_array[2]);
                                    //Log.i( "DIST", "Distancias(" + 0 + "): " + distance_array[2]);
                                    Log.i("BEACON", "beaconID: " + beacons.get(2).getMacAddress() +
                                            ", Media: "  + acumulado_array[2]/10.0  + ", rssi: " + beacons.get(2).getRssi() + ", MeasuredPower: "
                                            + beacons.get(2).getMeasuredPower() + ", Distancia: " + distance_array[2]
                                            + ", Medita_total: " + acumulado_total_array[2]/contador);
                                    break;
                                case 6000:
                                    acumulado_total_array[3] = acumulado_total_array[3] + acumulado_array[3];
                                    exponente_array[3] = (beacons.get(3).getMeasuredPower() -  (acumulado_total_array[3]/contador)) / 20.0;
                                    distance_array[3] = Math.pow(10d, exponente_array[3]);
                                    //Log.i( "DIST", "Distancias(" + 0 + "): " + distance_array[2]);
                                    Log.i("BEACON", "beaconID: " + beacons.get(3).getMacAddress() +
                                            ", Media: "  + acumulado_array[3]/10.0  + ", rssi: " + beacons.get(3).getRssi() + ", MeasuredPower: "
                                            + beacons.get(3).getMeasuredPower() + ", Distancia: " + distance_array[3]
                                            + ", Medita_total: " + acumulado_total_array[3]/contador);
                                    break;
                            }
                            /* Sobra metido en el SWITCH
                            acumulado_total_array[i] = acumulado_total_array[i] + acumulado_array[i];
                            exponente_array[i] = (beacons.get(i).getMeasuredPower() -  (acumulado_total_array[i]/contador)) / 20.0;
                            distance_array[i] = Math.pow(10d, exponente_array[i]);
                            //Log.i( "DIST", "Distancias(" + i + "): " + distance_array[i]);
                            Log.i("BEACON", "beaconID: " + beacons.get(i).getMacAddress() +
                                    ", Media: "  + acumulado_array[i]/10.0  + ", rssi: " + beacons.get(i).getRssi() + ", MeasuredPower: "
                                    + beacons.get(i).getMeasuredPower() + ", Distancia: " + distance_array[i]
                                    + ", Medita_total: " + acumulado_total_array[i]/contador);
                             */
                        }
                        acumulado_array[0] = 0.0;
                        acumulado_array[1] = 0.0;
                        acumulado_array[2] = 0.0;
                        acumulado_array[3] = 1.0;
                        if (beacons.size() == 3) {
                            mensaje = trilateracion_3_puntos(distance_array[0], distance_array[1], distance_array[2]);
                        }
                        if (beacons.size() == 4) {
                            //mensaje = trilateracion_4_puntos(distance_array[0], distance_array[1], distance_array[2], distance_array[3 ]);
                        }
                        showNotification("Posicion", mensaje);
                        Log.i( "Posicion", mensaje );
                    }
                }
                /*FIN UBICACION 3 NODOS */
            }
        });

        /*
        prueba.setNearableListener(new BeaconManager.NearableListener() {
            @Override
            public void onNearablesDiscovered(List nearables) {
                Log.d("Nereable", "Discovered nearables: " + nearables);
            }
        });*/

        /*
        prueba.setEddystoneListener(new BeaconManager.EddystoneListener() {
            @Override
            public void onEddystonesFound(List<Eddystone> eddystones) {
                //nearestEddystone = eddystones.get(0);
                //showNotification(Integer.toString(nearestEddystone.calibratedTxPower), Integer.toString(nearestEddystone.rssi) );
                //showNotification("Hola", Integer.toString(eddystones.size()));
                for (Eddystone edd : eddystones) {
                    Log.i("EDDYSTONE", "beaconID: " + edd.instance +
                            ", rssi: " + edd.rssi + ", CalibratedTXPower: " + edd.calibratedTxPower);
                    contador = contador + 1;
                    acumulado = acumulado + edd.rssi;

                    if ( (contador % 10) == 0){
                        acumulado_total = acumulado_total + acumulado;
                        Log.i("EDDYSTONE", "beaconID: " + edd.instance +
                                ", Media: "  + acumulado/10 + ", Medita_ttoal: " + acumulado_total/contador +
                                "rssi: " + edd.rssi + ", CalibratedTXPower: " + edd.calibratedTxPower);
                        acumulado = 0.0;
                    }


                }
            }
        });
        */
        /*
        prueba.setTelemetryListener(new BeaconManager.TelemetryListener() {
            @Override
            public void onTelemetriesFound(List<EstimoteTelemetry> telemetries) {
                for (EstimoteTelemetry tlm : telemetries) {
                    Log.d("TELEMETRY", "beaconID: " + tlm.deviceId +
                            ", temperature: " + tlm.temperature + " °C");

                }
            }
        });

        prueba.setLocationListener(new BeaconManager.LocationListener() {
            @Override
            public void onLocationsFound(List<EstimoteLocation> locations) {
                for (EstimoteLocation loc : locations) {
                    rssionemeter = loc.txPower - 62;
                    //distance = Math.pow(10, (rssionemeter - loc.rssi ) / 20);
                    distance = Math.pow(10, (loc.txPower - loc.rssi ) / 20);
                    showNotification(Integer.toString(loc.txPower)+" " + Integer.toString(loc.rssi),
                            Double.toString(distance));
                    Log.d("LOCATION", "beaconID: " + loc.id +
                            ", rssi: " + loc.rssi + ", TXPower: " + loc.txPower );
                }
            }
        });
        */

        prueba.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                // Beacons ranging.
                prueba.setForegroundScanPeriod(200,0);
                prueba.setBackgroundScanPeriod(200,0);
                prueba.startRanging(ALL_ESTIMOTE_BEACONS);

                // Nearable discovery.
                //prueba.startNearableDiscovery();

                // Eddystone scanning.
                //prueba.startEddystoneScanning();

                //Telemetry scanning.
                //prueba.startTelemetryDiscovery();

                //Location scanning.
                //prueba.startLocationDiscovery();
            }
        });

    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    public double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#,##");
        return Double.valueOf(twoDForm.format(d));
    }
}
