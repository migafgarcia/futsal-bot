package data;

import java.util.Date;

public class Player {
    private int id;
    private String username;
    private String status;
    private boolean registered;
    private boolean late;
    private Date date;

    private Player(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.status = builder.status;
        this.registered = builder.registered;
        this.late = builder.late;
        this.date = builder.date;
    }

    public static Builder newPlayer() {
        return new Builder();
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getStatus() {
        return status;
    }

    public boolean isRegistered() {
        return registered;
    }

    public boolean isLate() {
        return late;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return username;
    }

    public static final class Builder {
        private int id;
        private String username;
        private String status;
        private boolean registered;
        private boolean late;
        private Date date;

        private Builder() {
        }

        public Player build() {
            return new Player(this);
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder registered(boolean registered) {
            this.registered = registered;
            return this;
        }

        public Builder late(boolean late) {
            this.late = late;
            return this;
        }

        public Builder date(Date date) {
            this.date = date;
            return this;
        }
    }

}
