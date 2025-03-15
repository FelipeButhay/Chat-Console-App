package ChatApp;

import java.io.Serializable;

public class Message implements Serializable {
    private int flag = 0;
    private String data;

    private String transmitterUserName;
    private String receiverUserName;

    public void setFlag(int newFlag) {
        flag = newFlag;
    }

    public int getFlag() {
        return flag;
    }
    
    public void setData(String newData) {
        data = newData;
    }

    public String getData() {
        return data;
    }

    public void setTransmitter(String userName) {
        transmitterUserName = userName;
    }

    public String getTransmitter() {
        return transmitterUserName;
    }

    public void setReceiver(String userName) {
        receiverUserName = userName;
    }

    public String getReceiver() {
        return receiverUserName;
    }
}
