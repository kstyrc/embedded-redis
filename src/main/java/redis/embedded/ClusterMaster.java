package redis.embedded;

public class ClusterMaster {

    private Redis masterRedis = null;
    private String masterRedisIp;
    private Integer masterRedisPort;
    private Integer clusterNodesCount;
    private Integer indexOfCluster;

    public ClusterMaster(String masterRedisIp, Integer masterRedisPort,
                         Integer clusterNodesCount, Integer indexOfCluster) {
        this.masterRedisIp = masterRedisIp;
        this.masterRedisPort = masterRedisPort;
        this.clusterNodesCount = clusterNodesCount;
        this.indexOfCluster = indexOfCluster;
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

    public Redis getMasterRedis() {
        return masterRedis;
    }

    public void setMasterRedis(Redis masterRedis) {
        this.masterRedis = masterRedis;
    }

    public Integer getClusterNodesCount() {
        return clusterNodesCount;
    }

    public void setClusterNodesCount(Integer clusterNodesCount) {
        this.clusterNodesCount = clusterNodesCount;
    }

    public Integer getIndexOfCluster() {
        return indexOfCluster;
    }

    public void setIndexOfCluster(Integer indexOfCluster) {
        this.indexOfCluster = indexOfCluster;
    }
}
