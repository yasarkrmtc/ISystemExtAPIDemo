package com.example.terminaldemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.example.util.Logger;
import com.example.util.TextViewUtil;
import com.wizarpos.wizarviewagentassistant.aidl.ISystemExtApi;
import com.wizarpos.wizarviewagentassistant.aidl.NetworkType;

import java.util.Locale;

public class MainActivity extends AbstractActivity implements View.OnClickListener,ServiceConnection {
    ISystemExtApi systemExtApi;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        log_text = (TextView) this.findViewById(R.id.text_result);
        log_text.setMovementMethod(ScrollingMovementMethod.getInstance());
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
        findViewById(R.id.enableAirplaneMode).setOnClickListener(this);
        findViewById(R.id.settings).setOnClickListener(this);
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
                }
                if(log_text.getText().length() > 256){
                    log_text.post(new Runnable() {
                        @Override
                        public void run() {
                            log_text.scrollTo(0, log_text.getLayout().getLineTop(log_text.getLineCount()) - log_text.getHeight());
                        }
                    });
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
        Logger.debug("bind service result (%s)" ,isSuccess);
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
        try{
            boolean flag = false;
            writerInLog(btnText, R.id.log_default);
            if (index == R.id.btnSetScreenOff) {
                flag = systemExtApi.setScreenOffTimeout(30000);
            } else if (index == R.id.getPreferredNetworkType) {
                int type = systemExtApi.getPreferredNetworkType(NetworkType.PARCELABLE_WRITE_RETURN_VALUE);
                writerInSuccessLog("result is : "+type);
                return;
            } else if (index == R.id.setPreferredNetworkType) {
                flag = systemExtApi.setPreferredNetworkType(0,9);
            } else if (index == R.id.startLockTaskMode) {
                flag = systemExtApi.startLockTaskMode(0);
            } else if (index == R.id.setMobileDataEnabled) {
                flag = systemExtApi.setMobileDataEnabled(0,true);
            } else if (index == R.id.setMobileDataRoamingEnabled) {
                flag = systemExtApi.setMobileDataRoamingEnabled(0,1);
            } else if (index == R.id.getSupportedNetworkTypes) {
                NetworkType [] networkTypes = systemExtApi.getSupportedNetworkTypes();
                writerInSuccessLog("result is : "+JSON.toJSONString(networkTypes));
                return;
            } else if (index == R.id.setTouchScreenWakeupValue) {
                flag = systemExtApi.setTouchScreenWakeupValue("touch");
            } else if (index == R.id.getTouchScreenWakeupValue) {
                String value = systemExtApi.getTouchScreenWakeupValue();
                writerInSuccessLog("result is : "+value);
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
                flag = systemExtApi.setDeviceOwner(this.getPackageName(),LockReceiver.class.getName());
            } else if (index == R.id.setUsrProp) {
                flag = systemExtApi.setUsrProp("fingerprint" + 0, "1");;
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
            } else if (index == R.id.enableAirplaneMode) {
                systemExtApi.enableAirplaneMode(true);
                flag = true;
            }else if (index == R.id.settings) {
                log_text.setText("");
            }
            if(flag){
                writerInSuccessLog("result is true!");
            }else if (index!=R.id.settings){
                writerInFailedLog("result is false!");
            }
        }catch (Exception e){
            e.printStackTrace();
            writerInFailedLog("test failed!");
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}