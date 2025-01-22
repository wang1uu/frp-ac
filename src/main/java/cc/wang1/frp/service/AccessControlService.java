package cc.wang1.frp.service;


import cc.wang1.frp.dto.frp.ValidatedResultDTO;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface AccessControlService {
    boolean accessControl(String ip);

    void refresh();

    void block(String ip);

    void unblock(String ip);

    ValidatedResultDTO<?> frpAc(HttpServletRequest request) throws IOException;
}
