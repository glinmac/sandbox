package ltd.cylleneworks.sandbox.springboot.hbase;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.hadoop.hbase.HbaseTemplate;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    @Autowired
    private HbaseTemplate hbaseTemplate;

    private String tableName = "sandbox:users";

    private static byte[] cfData = Bytes.toBytes("d");
    private static byte[] qFullname = Bytes.toBytes("fullname");
    private static byte[] qEmail = Bytes.toBytes("email");
    private static byte[] qDob = Bytes.toBytes("dob");

    public User find(String username) {
        return hbaseTemplate.get(tableName, username, new RowMapper<User>() {
            @Override
            public User mapRow(Result result, int rowNum) throws Exception {
                return new User(Bytes.toString(result.getRow()),
                        Bytes.toString(result.getValue(cfData, qFullname)),
                        Bytes.toString(result.getValue(cfData, qEmail)),
                        Bytes.toString(result.getValue(cfData, qDob)));
            }
        });
    }


}
