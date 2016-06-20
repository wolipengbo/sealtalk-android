package cn.rongcloud.im.server.response;

/**
 * Created by AMing on 16/4/8.
 * Company RongCloud
 */
public class CreateGroupData {

    /**
     * timestamp : 1460095009370
     * targetGroupName : 虫兔家
     * operatorNickname : 虫子
     */

    private DataEntity data;

    public void setData(DataEntity data) {
        this.data = data;
    }

    public DataEntity getData() {
        return data;
    }

    public static class DataEntity {
        private long timestamp;
        private String targetGroupName;
        private String operatorNickname;

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setTargetGroupName(String targetGroupName) {
            this.targetGroupName = targetGroupName;
        }

        public void setOperatorNickname(String operatorNickname) {
            this.operatorNickname = operatorNickname;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getTargetGroupName() {
            return targetGroupName;
        }

        public String getOperatorNickname() {
            return operatorNickname;
        }
    }
}
