package redis.embedded;

public class ClusterSlave {

    private Redis slaveRedis = null;
    private String slaveIp;
    private Integer slavePort;
    private String masterRedisIp;
    private Integer masterRedisPort;

    public ClusterSlave(String slaveIp, Integer slavePort,
                        String masterRedisIp, Integer masterRedisPort) {
        this.slaveIp = slaveIp;
        this.slavePort = slavePort;
        this.masterRedisIp = masterRedisIp;
        this.masterRedisPort = masterRedisPort;
    }

    public String getSlaveIp() {
        return slaveIp;
    }

    public void setSlaveIp(String slaveIp) {
        this.slaveIp = slaveIp;
    }

    public Integer getSlavePort() {
        return slavePort;
    }

    public void setSlavePort(Integer slavePort) {
        this.slavePort = slavePort;
    }

    public String getMasterRedisIp() {
        return masterRedisIp;
    }

    public void setMasterRedisIp(String masterRedisIp) {
        this.masterRedisIp = masterRedisIp;
    }

    public Integer getMasterRedisPort() {
        return masterRedisPort;
    }

    public void setMasterRedisPort(Integer masterRedisPort) {
        this.masterRedisPort = masterRedisPort;
    }

    public Redis getSlaveRedis() {
        return slaveRedis;
    }

    public void setSlaveRedis(Redis slaveRedis) {
        this.slaveRedis = slaveRedis;
    }
}
