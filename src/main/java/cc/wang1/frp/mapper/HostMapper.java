package cc.wang1.frp.mapper;

import cc.wang1.frp.entity.Host;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HostMapper extends BaseMapper<Host> {
    Integer upsert(@Param("host") Host host);
}
