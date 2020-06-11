package com.example.subway_for_pregnant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.socket.client.IO;
import io.socket.emitter.Emitter;
import io.socket.client.Socket;


public class SampleBluetoothActivity extends AppCompatActivity implements BeaconConsumer {

    private static final String TAG = "Beacontest";
    private static final String TAG2 = "NodeTest";

    private static final int TIME_START = 1000;

    static Socket socket = null;
    static String minor_to;
    String[] room = {"room1", "room2", "room3"};
    static int num = 0;
    FirebaseUser user;
    static String name;
    static boolean is_reservation = false;

    boolean on = false;

    private BeaconManager beaconManager;

    private List<Beacon> beaconList = new ArrayList<>();
    TextView textView;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_bluetooth);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        name = user.getEmail();


                //비콘 매니저 생성,
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //textView = (TextView) findViewById(R.id.textView7);//비콘검색후 검색내용 뿌려주기위한 textview

        //비콘 매니저에서 layout 설정 'm:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25'
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        //beaconManager 설정 bind
        beaconManager.bind(this);

        db.collection("user").document(user.getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.getData().get("reservation_info") != ""){
                                is_reservation = true;
                            }
                        }
                    }
                });

        //beacon 을 활용하려면 블루투스 권한획득(Andoird M버전 이상)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access" );
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok,null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        findViewById(R.id.Run).setOnClickListener(onClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        beaconList.add(beacon);
                    }
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 아래에 있는 handleMessage를 부르는 함수. 맨 처음에는 0초간격이지만 한번 호출되고 나면
            // 1초마다 불러온다.
            handler.sendEmptyMessage(0);
        }
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //textView.setText("test");

            Log.d(TAG2, "for문전에 진입");

            // 비콘의 아이디와 거리를 측정하여 textView에 넣는다.
            for(Beacon beacon : beaconList){

                Log.d(TAG2, "for문에 진입");

                String uuid = beacon.getId1().toString(); //beacon uuid
                int major = beacon.getId2().toInt(); //beacon major
                int minor = beacon.getId3().toInt();// beacon minor
                String address = beacon.getBluetoothAddress();

                minor_to = Integer.toHexString(minor);

                Log.d(TAG2, "마이너 설정");

                if (major == 30288) {
                    Log.d(TAG2, "메이너 진입");
                    //beacon 의 식별을 위하여 major값으로 확인
                    //이곳에 필요한 기능 구현
                    //textView.append("ID 1 : " + beacon.getId2() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
                    //textView.append("임산부석 블루투스 TEST...\n");
                    //textView.append("Beacon Bluetooth Id : " + address + "\n");
                    //textView.append("Beacon UUID : " + uuid + "\n");
                    //textView.append("Beacon MAJOR : " + major + "\n");
                    //textView.append("Beacon MINOR : " + minor_to + "\n");
                }

                    //int txpower = beacon.getTxPower();
                if(Double.parseDouble((String.format("%.3f", beacon.getDistance())))<3) { // 거리가 1m이내일 경우만

                    Log.d(TAG2, "메이저 진입 전");
                    if (major == 30288) {
                        Log.d(TAG2, "메이너 진입");
                        //beacon 의 식별을 위하여 major값으로 확인
                        //이곳에 필요한 기능 구현
                        //textView.append("ID 1 : " + beacon.getId2() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
                        //textView.append("임산부석 블루투스 TEST...\n");
                        //textView.append("Beacon Bluetooth Id : " + address + "\n");
                        //textView.append("Beacon UUID : " + uuid + "\n");
                        //textView.append("Beacon MAJOR : " + major + "\n");
                        //textView.append("Beacon MINOR : " + minor_to + "\n");

                        try {
                            Log.d(TAG2, "try문 진입");
                            socket = IO.socket("http://ad5345db51b6.ngrok.io");
                            Log.d(TAG2, "소켓 생성");
                            socket.on(Socket.EVENT_CONNECT, onConnect);
                            Log.d(TAG2, "연결");
                            socket.connect();
                            socket.on("joinRoom", joinNewRoom);
                            socket.on("myMsg", onNewMessage);
                            Log.d(TAG2, "마이너 보냄 및 disconnect 보냄");
                            Log.d(TAG2,""+minor_to);

                            socket.on("leaveRoom", leaveNewRoom);

                            //socket.disconnect();


                            //if(handler !=null) {
                            Log.d(TAG2, "handler 삭제");
                            handler.removeMessages(0);
                            on = true;
                            break;
                            //}

                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        //textView.append("Beacon TXPOWER : "+txpower+"\n");

                        //myStartActivity(NodeTestActivity.class, minor);


                    } else {
                        //나머지 비콘검색
                        //textView.append("ID 2: " + beacon.getId2() + " / " + "Distance : " + Double.parseDouble(String.format("%.3f", beacon.getDistance())) + "m\n");
                    }
                    handler.removeMessages(0);
                    on = true;
                }


            }
            // 자기 자신을 1초마다 호출
            if(on==false) {
                Log.d(TAG2, "handler 호출");
                handler.sendEmptyMessageDelayed(0, 100); // 0.1초
            }
        }


    };

    static Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socket.emit("say","hi");
            socket.emit("joinRoom",num);
            Log.d(TAG2, "say 이벤트");
        }
    };

    static Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            socket.emit("login",minor_to,num,name,is_reservation);

            socket.emit("leaveRoom",num);

            num++;
            num = num%3;

            Log.d(TAG2, "login 이벤트");

        }
    };

    static Emitter.Listener joinNewRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG2, "joinNewRoom on 이벤트");

        }
    };

    static Emitter.Listener leaveNewRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG2, "joinNewRoom on 이벤트 && 이벤트 끝");
        }
    };



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }

    /*
    private void myStartActivity(Class c, int minor) {
        Intent intent = getIntent();
        intent.getExtras();
        Intent intent2 = new Intent(this, c);
        intent2.putExtras(intent);
        intent2.putExtra("minor",minor);
        startActivity(intent2);
    }*/
}

