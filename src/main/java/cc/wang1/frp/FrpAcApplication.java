package cc.wang1.frp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("cc.wang1.frp.mapper")
@SpringBootApplication
public class FrpAcApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(FrpAcApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

    }
}
