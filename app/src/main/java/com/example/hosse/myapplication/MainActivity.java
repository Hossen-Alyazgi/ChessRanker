package com.example.hosse.myapplication;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

public class MainActivity extends AppCompatActivity {

    TextView read_msg_box;
    TextView connectionStatus;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    static SendReceive sendReceive;

    Dialog connectDialog;
    Dialog versusDialog;
    Dialog resultDialog;
    static String choice = "";
    static int elo;

    Dialog getNameDialog;

    private static String sentMessage;


    private static File ELO_FILE;

    String username;

    static boolean lock = true;


    int opponentElo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();

        ELO_FILE = new File(this.getFilesDir(), "elo");

        if (!ELO_FILE.exists()) {
            openNameDialog();
            return;
        }

        load(ELO_FILE, true);//File and save to username and elo variables
    }

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
            Log.v("main", "reason" + e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.v("main", "reason" + e);
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
                    case '1':
                        read_msg_box.setText(tempMsg);

                        break;
                    case '2':
                        ((TextView) versusDialog.findViewById(R.id.opponentName)).setText(message);

                        break;
                    case '3':
                        versusDialog.dismiss();
                        openResultDialog();

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

                resultDialog.dismiss();
                openLoseDialog(elo,new Calculator().eloCalculator(elo,opponentElo,0));
            }
        });
        (resultDialog.findViewById(R.id.mebtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultDialog.dismiss();
                openWinDialog(elo,new Calculator().eloCalculator(elo,opponentElo,1));
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
        startCountAnimation(initialValue,finalValue,ep);

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
        startCountAnimation(initialValue,finalValue,ep);

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
    private void startCountAnimation( int in, int fin, final TextView pe) {
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
        public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pinfo) {
            final InetAddress groupOwnerAddress = wifiP2pinfo.groupOwnerAddress;

            if (wifiP2pinfo.groupFormed && wifiP2pinfo.isGroupOwner) {

                connectionStatus.setText(R.string.host);
                serverClass = new ServerClass();
                serverClass.start();
                setMessage(username);
                openVersusDialog();
                credentialTask task = new credentialTask();
                task.execute();
                eloSend elosend = new eloSend();
                elosend.execute();
            } else if (wifiP2pinfo.groupFormed) {

                connectionStatus.setText(R.string.client);
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
                setMessage(username);
                openVersusDialog();
                credentialTask task = new credentialTask();
                task.execute();
                eloSend elosend = new eloSend();
                elosend.execute();

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
                Log.v("MainActivity", "" + e);
            }
        }
    }

    private class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        private SendReceive(Socket skt) {
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                Log.v("MainActivity", "" + e);
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
                    Log.v("MainActivity", "" + e);
                }
            }
        }

        private void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.v("MainActivity", "" + e);
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
                Log.v("MainActivity", "" + e);
            }
        }
    }


    public static class versusTask extends AsyncTask<String, String, String> {
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
    public static class credentialTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            while (sendReceive == null) ;
            while (!lock) ;
            lock = false;
            sendReceive.write((getMessage() + 2).getBytes());
            lock = true;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;

        }
    }

    public static class eloSend extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            while (sendReceive == null) ;
            while (!lock) ;
            lock = false;
            sendReceive.write((Integer.toString(elo) + 5).getBytes());
            lock = true;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        getNameDialog.dismiss();
        disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getNameDialog.dismiss();
        disconnect();

    }


}
