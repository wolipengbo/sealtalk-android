package cn.rongcloud.im.model;

/**
 * Created by zhjchen on 8/12/15.
 */
public class RongEvent {

    /**
     * 有人加入实时位置共享
     */
    public static class RealTimeLocationMySelfJoinEvent {
        String userId;

        RealTimeLocationMySelfJoinEvent(String userId) {
            this.userId = userId;
        }

        public static RealTimeLocationMySelfJoinEvent obtain(String userId) {

            return new RealTimeLocationMySelfJoinEvent(userId);
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }


    /**
     * 有人加入实时位置共享
     */
    public static class RealTimeLocationJoinEvent {
        String userId;

        RealTimeLocationJoinEvent(String userId) {
            this.userId = userId;
        }

        public static RealTimeLocationJoinEvent obtain(String userId) {

            return new RealTimeLocationJoinEvent(userId);
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    /**
     * 有人退出实时位置共享
     */
    public static class RealTimeLocationQuitEvent {
        String userId;

        RealTimeLocationQuitEvent(String userId) {
            this.userId = userId;
        }

        public static RealTimeLocationQuitEvent obtain(String userId) {
            return new RealTimeLocationQuitEvent(userId);
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }

    /**
     * 接收某人的实时位置共享
     */
    public static class RealTimeLocationReceiveEvent {
        String userId;
        double latitude;
        double longitude;

        RealTimeLocationReceiveEvent(String userId, double latitude, double longitude) {
            this.userId = userId;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public static RealTimeLocationReceiveEvent obtain(String userId, double latitude, double longitude) {
            return new RealTimeLocationReceiveEvent(userId, latitude, longitude);
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }


    public static class RealTimeLocationStatusChangeEvent {


        public RealTimeLocationStatusChangeEvent obtain(double latitude, double longitude, String userId) {


            return null;
        }
    }
}
