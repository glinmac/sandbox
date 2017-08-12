package ltd.cylleneworks.sandbox.springboot.hbase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties
@Component
public class HbaseProperties {
    @Value("${hbase.zookeeper.quorum}")
    private String quorum;
    public String getQuorum() {return quorum;}
    public void setQuorum(String quorum) {
        this.quorum = quorum;
    }
}
