package cn.rongcloud.im.server.response;

import java.util.List;

/**
 * Created by AMing on 16/2/18.
 * Company RongCloud
 */
public class GroupNotificationMessageData {


    /**
     * timestamp : 1456366634327
     * targetUserDisplayNames : ["android"]
     * targetUserIds : ["Cz3bcYl2K"]
     * operatorNickname : 赵哈哈
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
        private String operatorNickname;
        private List<String> targetUserDisplayNames;
        private List<String> targetUserIds;

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setOperatorNickname(String operatorNickname) {
            this.operatorNickname = operatorNickname;
        }

        public void setTargetUserDisplayNames(List<String> targetUserDisplayNames) {
            this.targetUserDisplayNames = targetUserDisplayNames;
        }

        public void setTargetUserIds(List<String> targetUserIds) {
            this.targetUserIds = targetUserIds;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getOperatorNickname() {
            return operatorNickname;
        }

        public List<String> getTargetUserDisplayNames() {
            return targetUserDisplayNames;
        }

        public List<String> getTargetUserIds() {
            return targetUserIds;
        }
    }
}
