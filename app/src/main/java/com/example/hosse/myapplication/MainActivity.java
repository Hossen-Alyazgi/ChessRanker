package com.example.hosse.myapplication;

import android.animation.ValueAnimator;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ComponentCallbacks2 {

    TextView connectionStatus;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    SendReceive sendReceive;

    Dialog connectDialog;
    Dialog versusDialog;
    Dialog resultDialog;
    static int elo;

    Dialog getNameDialog;

    private static String sentMessage;

    private static File ELO_FILE;

    String username;

    static boolean lock = true;

    int opponentElo;

    private ViewPager mSlideViewPager;
    private LinearLayout mDotLayout;
    private SliderAdapter sliderAdapter;
    private TextView[] mDots;
    private Button mNextBtn;
    private Button mBackBtn;
    private int mCurrentPage;

    String opponentMessage;
    static String choice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ELO_FILE = new File(this.getFilesDir(), "elo");
        if (!ELO_FILE.exists()) {
            setContentView(R.layout.intro_slides);
            mSlideViewPager = findViewById(R.id.slideViewPager);
            mDotLayout = findViewById(R.id.dotsLayout);

            mNextBtn = findViewById(R.id.nextButton);
            mBackBtn = findViewById(R.id.backButton);

            sliderAdapter = new SliderAdapter(this);


            mSlideViewPager.setAdapter(sliderAdapter);

            addDotsIndicator(0);
            mSlideViewPager.addOnPageChangeListener(viewListener);

            mNextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNextBtn.getText().toString().equals("Finish")) {
                        setContentView(R.layout.activity_main);
                        initialWork();
                        exqListener();
                        openNameDialog();

                    } else {
                        mSlideViewPager.setCurrentItem(mCurrentPage + 1);
                    }

                }
            });
            mBackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSlideViewPager.setCurrentItem(mCurrentPage - 1);
                }
            });


        } else {
            setContentView(R.layout.activity_main);
            initialWork();
            exqListener();
        }

        load(ELO_FILE, true);//File and save to username and elo variables
    }

    private void initialWork() {

        connectionStatus = findViewById(R.id.connectionStatus);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        connectDialog = new Dialog(this);
        versusDialog = new Dialog(this);
        getNameDialog = new Dialog(this);
        resultDialog = new Dialog(this);

        mSlideViewPager = findViewById(R.id.slideViewPager);
        mDotLayout = findViewById(R.id.dotsLayout);
        mNextBtn = findViewById(R.id.nextButton);
        mBackBtn = findViewById(R.id.backButton);
        sliderAdapter = new SliderAdapter(this);
    }

    public void addDotsIndicator(int position) {
        mDots = new TextView[3];
        mDotLayout.removeAllViews();

        for (int i = 0; i < mDots.length; i++) {

            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226"));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.introcolorTransparentWhite));

            mDotLayout.addView(mDots[i]);

        }
        if (mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.colorwhite));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            addDotsIndicator(i);
            mCurrentPage = i;

            if (i == 0) {

                mNextBtn.setEnabled(true);
                mBackBtn.setEnabled(false);
                mBackBtn.setVisibility(View.INVISIBLE);

                mNextBtn.setText("Next");
                mBackBtn.setText("");

            } else if (i == mDots.length - 1) {
                mNextBtn.setEnabled(true);
                mBackBtn.setEnabled(true);
                mBackBtn.setVisibility(View.VISIBLE);

                mNextBtn.setText("Finish");
                mBackBtn.setText("Back");
            } else {
                mNextBtn.setEnabled(true);
                mBackBtn.setEnabled(true);
                mBackBtn.setVisibility(View.VISIBLE);

                mNextBtn.setText("Next");
                mBackBtn.setText("Back");
            }

        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public void load(File name, boolean exist) {

        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream;
        try {
            if (exist) {
                String user = "";
                String eloe = "";
                fileInputStream = new FileInputStream(name);
                int content;
                boolean userelo = true;
                while ((content = fileInputStream.read()) != -1) {
                    if (content == ',') {
                        userelo = false;
                        continue;
                    }
                    if (userelo) {
                        user += (char) content;
                        continue;
                    }
                    eloe += (char) content;
                }
                username = user;
                elo = Integer.parseInt(eloe);
            } else {
                fileOutputStream = new FileOutputStream(name);
                fileOutputStream.write((username + "," + elo).getBytes());
            }


        } catch (IOException e) {
            Log.v("main", "one" + e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.v("main", "two" + e);
                }
            }
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == MESSAGE_READ) {
                byte[] readBuff = (byte[]) msg.obj;
                String tempMsg = new String(readBuff, 0, msg.arg1);
                char select = (char) tempMsg.getBytes()[tempMsg.length() - 1];
                String message = tempMsg.substring(0, tempMsg.length() - 1);

                switch (select) {
                    case '2':
                        ((TextView) versusDialog.findViewById(R.id.opponentName)).setText(message);

                        break;
                    case '3':
                        versusDialog.dismiss();
                        openResultDialog();

                        break;
                    case '4':
                        opponentMessage = message;
                        new Thread() {
                            @Override
                            public void run() {
                                new CheckDialog(opponentMessage).execute();
                            }
                        }.start();
                        break;

                    case '5':
                        ((TextView) versusDialog.findViewById(R.id.opponent_elo_number)).setText(message);
                        opponentElo = Integer.parseInt(message);

                        break;
                    default:
                        break;

                }
            }
            return true;
        }
    });

    private void exqListener() {
        (findViewById(R.id.discover)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wifi.setWifiEnabled(true);
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText(R.string.discovery_start);
                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionStatus.setText(R.string.discovery_fail);
                    }
                });
            }
        });

        ((ListView) findViewById(R.id.peerListView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final WifiP2pDevice device = deviceArray[position];
                final WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                connectDialog.setContentView(R.layout.popup_connect);

                Objects.requireNonNull(connectDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                connectDialog.show();
                (connectDialog.findViewById(R.id.closePopupConnect)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connectDialog.dismiss();
                    }
                });

                (connectDialog.findViewById(R.id.btnaccept)).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getApplicationContext(), "connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(int reason) {
                                Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_SHORT).show();
                            }
                        });

                        connectDialog.dismiss();

                    }
                });

            }
        });

    }


    public void openResultDialog() {
        resultDialog.setContentView(R.layout.result_dialog);
        Objects.requireNonNull(resultDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        resultDialog.show();


        (resultDialog.findViewById(R.id.opponnentbtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice = "opponent";
                new resultTask().execute();

            }
        });
        (resultDialog.findViewById(R.id.mebtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choice = "me";
                new resultTask().execute();
            }
        });
    }

    public void openVersusDialog() {
        versusDialog.setContentView(R.layout.versus_popup);
        Objects.requireNonNull(versusDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ((TextView) versusDialog.findViewById(R.id.YourName)).setText(username);
        String eloWord = Integer.toString(elo);
        ((TextView) versusDialog.findViewById(R.id.elo_number)).setText(eloWord);


        Objects.requireNonNull(versusDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        (versusDialog.findViewById(R.id.finish_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                versusDialog.dismiss();
                new versusTask().execute();
                openResultDialog();

            }
        });
        versusDialog.show();

    }

    public void setMessage(String message) {
        sentMessage = message;

    }

    public static String getMessage() {
        return sentMessage;
    }


    public void openNameDialog() {

        getNameDialog.setContentView(R.layout.name_input);
        Objects.requireNonNull(getNameDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        getNameDialog.findViewById(R.id.donenamepopup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                versusDialog.dismiss();
                username = ((TextView) getNameDialog.findViewById(R.id.writeName)).getText().toString();
                elo = 400;
                load(ELO_FILE, false);
                getNameDialog.dismiss();
                disconnect();
            }
        });
        getNameDialog.show();

    }

    public void openWinDialog(int initialValue, int finalValue) {

        final Dialog winDialog = new Dialog(this);
        winDialog.setContentView(R.layout.win_dialog);
        Objects.requireNonNull(winDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        winDialog.show();
        TextView ep = winDialog.findViewById(R.id.eloUpdate);
        startCountAnimation(initialValue, finalValue, ep);

        elo = finalValue;
        load(ELO_FILE, false);

        winDialog.findViewById(R.id.btnaccept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                winDialog.dismiss();
                disconnect();





            }
        });

    }

    public void openLoseDialog(int initialValue, int finalValue) {
        final Dialog loseDialog = new Dialog(this);
        loseDialog.setContentView(R.layout.lose_dialog);
        Objects.requireNonNull(loseDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        loseDialog.show();
        loseDialog.show();
        TextView ep = loseDialog.findViewById(R.id.eloUpdate);
        startCountAnimation(initialValue, finalValue, ep);

        elo = finalValue;
        load(ELO_FILE, false);

        loseDialog.findViewById(R.id.btnaccept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loseDialog.dismiss();
                disconnect();




            }
        });

    }
    public static void doRestart(Context c) {
        try {
            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis(), mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e("Main", "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e("Main", "Was not able to restart application, PM null");
                }
            } else {
                Log.e("Main", "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e("Main", "Was not able to restart application");
        }
    }


    private void startCountAnimation(int in, int fin, final TextView pe) {
        ValueAnimator animator = ValueAnimator.ofInt(in, fin);
        animator.start();
        animator.setDuration(5000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {

                pe.setText(animation.getAnimatedValue().toString());
            }
        });

    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            List<WifiP2pDevice> peers = new ArrayList<>();

            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;
                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                ((ListView) findViewById(R.id.peerListView)).setAdapter(adapter);
            }
            if (peers.size() == 0) {
                Toast.makeText(getApplicationContext(), "No Devices Found", Toast.LENGTH_SHORT).show();

            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable( WifiP2pInfo wifiP2pinfo) {
             InetAddress groupOwnerAddress = wifiP2pinfo.groupOwnerAddress;

            if (wifiP2pinfo.groupFormed && wifiP2pinfo.isGroupOwner) {

                connectionStatus.setText(R.string.host);
                new ServerClass().start();
                setMessage(username);
                openVersusDialog();
                new credentialTask().execute();
                new eloSend().execute();

            } else if (wifiP2pinfo.groupFormed) {

                connectionStatus.setText(R.string.client);
                new ClientClass(groupOwnerAddress).start();
                setMessage(username);
                openVersusDialog();
                new credentialTask().execute();
                new eloSend().execute();

            }

        }

    };


    public class ServerClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            super.run();
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                Log.v("MainActivity", "three" + e);
            }
        }
    }

    private class SendReceive extends Thread {
        Socket socket;
        OutputStream outputStream;
        InputStream inputStream;


        private SendReceive(Socket skt) {
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                Log.v("MainActivity", "four" + e);
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);
                    //System.out.println(new String(buffer, "UTF8"));
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    Log.v("MainActivity", "five" + e);
                }
            }
        }

        private void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.v("MainActivity", "six" + e);
            }
        }
    }

    public class ClientClass extends Thread {
        Socket socket;
        String hostAdd;

        private ClientClass(InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                Log.v("MainActivity", "seven" + e);
            }
        }
    }

    public class CheckDialog extends AsyncTask<String, String, String> {
        String opponentAnswer;

        public CheckDialog(String opponentAnswer) {
            this.opponentAnswer = opponentAnswer;
        }

        @Override
        protected String doInBackground(String... strings) {
            while (choice == null) ;
            return choice;
        }

        @Override
        protected void onPostExecute(String s) {

            if (!s.equals(opponentAnswer)) {
                resultDialog.dismiss();
                if (s.equals("me")) {
                    openWinDialog(elo, new Calculator().eloCalculator(elo, opponentElo, 1));
                } else {
                    openLoseDialog(elo, new Calculator().eloCalculator(elo, opponentElo, 0));
                }
                choice = null;
                opponentAnswer = null;
                opponentMessage = null;
            }  else if (opponentAnswer.equals(s)) {
                choice = null;
                opponentAnswer = null;
                opponentMessage = null;
                openResultDialog();
                Toast.makeText(getApplicationContext(), "you said the same thing", Toast.LENGTH_SHORT).show();

            }
        }
    }

    public class resultTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            if (choice.equals("me")) {
                sendReceive.write(("me4").getBytes());
            } else {
                sendReceive.write(("opponent4").getBytes());
            }
            return null;
        }
    }


    public class versusTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            while (sendReceive == null) ;
            while (!lock) ;
            lock = false;
            sendReceive.write(("finish3").getBytes());
            lock = true;
            return null;
        }
    }

    public class credentialTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            while (sendReceive == null) ;
            while (!lock) ;
            lock = false;
            sendReceive.write((getMessage() + 2).getBytes());

            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Log.v("MainActivity", "eight" + e);
            }
            lock = true;
            return null;

        }
    }

    public class eloSend extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            while (sendReceive == null) ;
            while (!lock) ;
            lock = false;
            sendReceive.write((Integer.toString(elo) + 5).getBytes());

            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Log.v("MainActivity", "nine" + e);
            }
            lock = true;
            return null;
        }
    }


    public void disconnect() {
        if (mManager != null && mChannel != null) {

            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("main", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("main", "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });

        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        connectDialog.dismiss();
        if (ELO_FILE.exists()) {
            registerReceiver(mReceiver, mIntentFilter);
        }

    }
    @Override
    protected void onPause(){
        super.onPause();
        connectDialog.dismiss();
        if (ELO_FILE.exists()) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        disconnect();

        unregisterReceiver(mReceiver);
        super.onDestroy();



    }



}
