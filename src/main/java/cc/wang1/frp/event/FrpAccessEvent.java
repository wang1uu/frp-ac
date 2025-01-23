package cc.wang1.frp.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FrpAccessEvent extends ApplicationEvent {

    private final String ip;

    public FrpAccessEvent(Object source, String ip) {
        super(source);
        this.ip = ip;
    }
}
