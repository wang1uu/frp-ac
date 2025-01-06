package cc.wang1.frp.service.impl;

import cc.wang1.frp.service.AccessControlService;
import org.springframework.stereotype.Service;

@Service
public class AccessControlServiceImpl implements AccessControlService {


    @Override
    public boolean accessControl(String ip) {
        return false;
    }
}
