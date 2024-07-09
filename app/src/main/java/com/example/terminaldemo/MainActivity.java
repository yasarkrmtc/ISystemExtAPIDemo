package com.example.terminaldemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.util.Logger;
import com.example.util.TextViewUtil;
import com.wizarpos.wizarviewagentassistant.aidl.ISystemExtApi;
import com.wizarpos.wizarviewagentassistant.aidl.NetworkType;

import java.lang.reflect.Field;
import java.util.Locale;

public class MainActivity extends AbstractActivity implements View.OnClickListener, ServiceConnection {
    ISystemExtApi systemExtApi;
    private String TAG = "MainActivity";
    private TextView log_text;
    private static Handler mHandler;
    private PhoneStateListener phoneStateListener;
    private int signalStrengthLevel = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize PhoneStateListener
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    signalStrengthLevel = signalStrength.getLevel(); // API level 23 and above
                }
            }
        };

        // Register the listener with TelephonyManager
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }

        log_text = findViewById(R.id.text_result);
        log_text.setMovementMethod(ScrollingMovementMethod.getInstance());
        log_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int lines = log_text.getHeight();
                int lastLine = log_text.getLayout().getHeight();
                if (lastLine >= lines) {
                    log_text.scrollTo(0, log_text.getLayout().getLineTop(log_text.getLineCount()) - log_text.getHeight());
                }
            }
        });
        findViewById(R.id.btnSetScreenOff).setOnClickListener(this);
        findViewById(R.id.getPreferredNetworkType).setOnClickListener(this);
        findViewById(R.id.setPreferredNetworkType).setOnClickListener(this);
        findViewById(R.id.startLockTaskMode).setOnClickListener(this);
        findViewById(R.id.setMobileDataEnabled).setOnClickListener(this);
        findViewById(R.id.setMobileDataRoamingEnabled).setOnClickListener(this);
        findViewById(R.id.getSupportedNetworkTypes).setOnClickListener(this);
        findViewById(R.id.setTouchScreenWakeupValue).setOnClickListener(this);
        findViewById(R.id.getTouchScreenWakeupValue).setOnClickListener(this);
        findViewById(R.id.enableAutoTimezone).setOnClickListener(this);
        findViewById(R.id.isEnableAutoTimezone).setOnClickListener(this);
        findViewById(R.id.enableAutoTimezoneGUI).setOnClickListener(this);
        findViewById(R.id.isEnableAutoTimezoneGUI).setOnClickListener(this);
        findViewById(R.id.enableAutoTime).setOnClickListener(this);
        findViewById(R.id.isEnableAutoTime).setOnClickListener(this);
        findViewById(R.id.enableAutoTimeGUI).setOnClickListener(this);
        findViewById(R.id.isEnableAutoTimeGUI).setOnClickListener(this);
        findViewById(R.id.setDeviceOwner).setOnClickListener(this);
        findViewById(R.id.setUsrProp).setOnClickListener(this);
        findViewById(R.id.enableShowTouches).setOnClickListener(this);
        findViewById(R.id.getShowTouchesState).setOnClickListener(this);
        findViewById(R.id.setStatusBarLocked).setOnClickListener(this);
        findViewById(R.id.setPowerKeyBlocked).setOnClickListener(this);
        findViewById(R.id.isPowerKeyBlocked).setOnClickListener(this);
        findViewById(R.id.enableMtp).setOnClickListener(this);
        findViewById(R.id.getMtpStatus).setOnClickListener(this);
        findViewById(R.id.setLanguage).setOnClickListener(this);
        findViewById(R.id.resetLanguage).setOnClickListener(this);
        findViewById(R.id.enableAirplaneMode).setOnClickListener(this);
        findViewById(R.id.settings).setOnClickListener(this);
        findViewById(R.id.simInfoButton).setOnClickListener(this);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == R.id.log_default) {
                    log_text.append("\t" + msg.obj + "\n");
                } else if (msg.what == R.id.log_success) {
                    String str = "\t" + msg.obj + "\n";
                    TextViewUtil.infoBlueTextView(log_text, str);
                } else if (msg.what == R.id.log_failed) {
                    String str = "\t" + msg.obj + "\n";
                    TextViewUtil.infoRedTextView(log_text, str);
                } else if (msg.what == R.id.log_clear) {
                    log_text.setText("");
                    log_text.scrollTo(0, 0);
                }
            }
        };

        bindSystemExtService();
    }

    public void bindSystemExtService() {
        try {
            startConnectService(MainActivity.this,
                    "com.wizarpos.wizarviewagentassistant",
                    "com.wizarpos.wizarviewagentassistant.SystemExtApiService", this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected boolean startConnectService(Context mContext, String packageName, String className, ServiceConnection connection) {
        boolean isSuccess = startConnectService(mContext, new ComponentName(packageName, className), connection);
        Logger.debug("bind service result (%s)", isSuccess);
        return isSuccess;
    }

    protected boolean startConnectService(Context context, ComponentName comp, ServiceConnection connection) {
        Intent intent = new Intent();
        intent.setPackage(comp.getPackageName());
        intent.setComponent(comp);
        boolean isSuccess = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Logger.debug("bind service (%s, %s)", isSuccess, comp.getPackageName(), comp.getClassName());
        return isSuccess;
    }

    @Override
    public void onClick(View v) {
        int index = v.getId();
        String btnText = "";
        if (v instanceof Button) {
            Button button = (Button) v;
            btnText = button.getText().toString();
        }
        try {
            boolean flag = false;
            writerInLog(btnText, R.id.log_default);
            if (index == R.id.btnSetScreenOff) {
                flag = systemExtApi.setScreenOffTimeout(30000);
            } else if (index == R.id.getPreferredNetworkType) {
                int type = systemExtApi.getPreferredNetworkType(NetworkType.PARCELABLE_WRITE_RETURN_VALUE);
                writerInSuccessLog("result is : " + type);
                return;
            } else if (index == R.id.setPreferredNetworkType) {
                flag = systemExtApi.setPreferredNetworkType(0, 9);
            } else if (index == R.id.startLockTaskMode) {
                flag = systemExtApi.startLockTaskMode(0);
            } else if (index == R.id.setMobileDataEnabled) {
                flag = systemExtApi.setMobileDataEnabled(0, true);
            } else if (index == R.id.setMobileDataRoamingEnabled) {
                flag = systemExtApi.setMobileDataRoamingEnabled(0, 1);
            } else if (index == R.id.getSupportedNetworkTypes) {
                NetworkType[] networkTypes = systemExtApi.getSupportedNetworkTypes();
                writerInSuccessLog("result is : " + JSON.toJSONString(networkTypes));
                return;
            } else if (index == R.id.setTouchScreenWakeupValue) {
                flag = systemExtApi.setTouchScreenWakeupValue("touch");
            } else if (index == R.id.getTouchScreenWakeupValue) {
                String value = systemExtApi.getTouchScreenWakeupValue();
                writerInSuccessLog("result is : " + value);
                return;
            } else if (index == R.id.enableAutoTimezone) {
                systemExtApi.enableAutoTimezone(true);
                flag = true;
            } else if (index == R.id.isEnableAutoTimezone) {
                flag = systemExtApi.isEnableAutoTimezone();
            } else if (index == R.id.enableAutoTimezoneGUI) {
                systemExtApi.enableAutoTimeGUI(true);
                flag = true;
            } else if (index == R.id.isEnableAutoTimezoneGUI) {
                flag = systemExtApi.isEnableAutoTimeGUI();
            } else if (index == R.id.enableAutoTime) {
                systemExtApi.enableAutoTime(true);
                flag = true;
            } else if (index == R.id.isEnableAutoTime) {
                flag = systemExtApi.isEnableAutoTime();
            } else if (index == R.id.enableAutoTimeGUI) {
                systemExtApi.enableAutoTimeGUI(true);
                flag = true;
            } else if (index == R.id.isEnableAutoTimeGUI) {
                flag = systemExtApi.isEnableAutoTimeGUI();
            } else if (index == R.id.setDeviceOwner) {
                flag = systemExtApi.setDeviceOwner(this.getPackageName(), LockReceiver.class.getName());
            } else if (index == R.id.setUsrProp) {
                flag = systemExtApi.setUsrProp("fingerprint" + 0, "1");
            } else if (index == R.id.enableShowTouches) {
                systemExtApi.enableShowTouches(true);
                flag = true;
            } else if (index == R.id.getShowTouchesState) {
                flag = systemExtApi.getShowTouchesState();
            } else if (index == R.id.setStatusBarLocked) {
                systemExtApi.setStatusBarLocked(true);
                flag = true;
            } else if (index == R.id.setPowerKeyBlocked) {
                systemExtApi.setPowerKeyBlocked(true);
                flag = true;
            } else if (index == R.id.isPowerKeyBlocked) {
                flag = systemExtApi.isPowerKeyBlocked();
            } else if (index == R.id.enableMtp) {
                systemExtApi.enableMtp(true);
                flag = true;
            } else if (index == R.id.getMtpStatus) {
                flag = systemExtApi.getMtpStatus();
            } else if (index == R.id.setLanguage) {
                flag = systemExtApi.setLanguage(Locale.CHINESE.getLanguage(), Locale.CHINESE.getCountry(), Locale.CHINESE.getVariant());
            } else if (index == R.id.resetLanguage) {
                flag = systemExtApi.setLanguage(Locale.ENGLISH.getLanguage(), Locale.ENGLISH.getCountry(), Locale.ENGLISH.getVariant());
            } else if (index == R.id.enableAirplaneMode) {
                systemExtApi.enableAirplaneMode(true);
                flag = true;
            } else if (index == R.id.simInfoButton) {
                handleSimInfo();
                return;
            } else if (index == R.id.settings) {
                writerInLog("", R.id.log_clear);
            }
            if (flag) {
                writerInSuccessLog("result is true!");
            } else if (index != R.id.settings) {
                writerInFailedLog("result is false!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            writerInFailedLog("test failed!");
        }
    }

    private void handleSimInfo() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null && telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY) {
            StringBuilder simInfo = new StringBuilder("SIM Status: true\n");
            simInfo.append("SIM Country: ").append(telephonyManager.getSimCountryIso()).append("\n");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            simInfo.append("SIM Serial Number: ").append(telephonyManager.getSimSerialNumber()).append("\n");
            simInfo.append("Subscriber ID: ").append(telephonyManager.getSubscriberId()).append("\n");
            simInfo.append("Network Operator Name: ").append(telephonyManager.getNetworkOperatorName()).append("\n");
            simInfo.append("Network Country ISO: ").append(telephonyManager.getNetworkCountryIso()).append("\n");
            simInfo.append("Phone Type: ").append(getPhoneTypeString(telephonyManager.getPhoneType())).append("\n");
            simInfo.append("Network Type: ").append(getNetworkTypeString(telephonyManager.getNetworkType())).append("\n");
            simInfo.append("Data Activity: ").append(getDataActivityString(telephonyManager.getDataActivity())).append("\n");
            simInfo.append("Data State: ").append(getDataStateString(telephonyManager.getDataState())).append("\n");
            simInfo.append("Signal Strength: ").append(signalStrengthLevel != -1 ? signalStrengthLevel + "/4" : "Unknown").append("\n");

            writerInSuccessLog(simInfo.toString());
        } else {
            writerInFailedLog("SIM Status: false");
        }

    }



    private String getNetworkTypeString(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO revision 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO revision A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO revision B";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "eHRPD";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDEN";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            case TelephonyManager.NETWORK_TYPE_GSM:
                return "GSM";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "TD-SCDMA";
            case TelephonyManager.NETWORK_TYPE_IWLAN:
                return "IWLAN";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "Unknown";
            default:
                return getNetworkTypeStringUsingReflection(networkType);
        }
    }

    private String getNetworkTypeStringUsingReflection(int networkType) {
        try {
            Class<?> telephonyManagerClass = TelephonyManager.class;
            Field networkTypeNrField = telephonyManagerClass.getField("NETWORK_TYPE_NR");
            int networkTypeNrValue = ((Field) networkTypeNrField).getInt(null);
            if (networkType == networkTypeNrValue) {
                return "NR (5G)";
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Field not available or inaccessible, ignore it
        }
        return "Unknown";
    }



    private String getPhoneTypeString(int phoneType) {
        switch (phoneType) {
            case TelephonyManager.PHONE_TYPE_GSM:
                return "GSM";
            case TelephonyManager.PHONE_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.PHONE_TYPE_SIP:
                return "SIP";
            case TelephonyManager.PHONE_TYPE_NONE:
                return "None";
            default:
                return "Unknown";
        }
    }

    private String getDataActivityString(int dataActivity) {
        switch (dataActivity) {
            case TelephonyManager.DATA_ACTIVITY_NONE:
                return "None";
            case TelephonyManager.DATA_ACTIVITY_IN:
                return "In";
            case TelephonyManager.DATA_ACTIVITY_OUT:
                return "Out";
            case TelephonyManager.DATA_ACTIVITY_INOUT:
                return "In/Out";
            case TelephonyManager.DATA_ACTIVITY_DORMANT:
                return "Dormant";
            default:
                return "Unknown";
        }
    }

    private String getDataStateString(int dataState) {
        switch (dataState) {
            case TelephonyManager.DATA_DISCONNECTED:
                return "Disconnected";
            case TelephonyManager.DATA_CONNECTING:
                return "Connecting";
            case TelephonyManager.DATA_CONNECTED:
                return "Connected";
            case TelephonyManager.DATA_SUSPENDED:
                return "Suspended";
            default:
                return "Unknown";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        this.unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            Logger.debug("onServiceConnected:" + service.getInterfaceDescriptor());
            systemExtApi = ISystemExtApi.Stub.asInterface(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }

    public static void writerInLog(String message, int what) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = message;
        mHandler.sendMessage(msg);
    }

    public static void writerInSuccessLog(String message) {
        writerInLog(message, R.id.log_success);
    }

    public static void writerInFailedLog(String message) {
        writerInLog(message, R.id.log_failed);
    }
}