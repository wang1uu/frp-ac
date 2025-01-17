package cc.wang1.frp.service;


public interface AccessControlService {
    boolean accessControl(String ip);

    void refresh();

    void block(String ip);

    void unblock(String ip);
}
