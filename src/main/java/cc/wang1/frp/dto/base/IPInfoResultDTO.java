package cc.wang1.frp.dto.base;

import lombok.Data;

@Data
public class IPInfoResultDTO {
    private String ip;
    private String city;
    private String region;
    private String country;
    private String loc;
    private String org;
    private String postal;
    private String timezone;
}
