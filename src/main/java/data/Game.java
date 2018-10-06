package data;

import java.util.Date;

public class Game {

    private int id;
    private Date date;
    private String location;

    private Game(Builder builder) {
        this.id = builder.id;
        this.date = builder.date;
        this.location = builder.location;
    }

    public static Builder newGame() {
        return new Builder();
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public static final class Builder {
        private int id;
        private Date date;
        private String location;

        private Builder() {
        }

        public Game build() {
            return new Game(this);
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder date(Date date) {
            this.date = date;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }
    }

    @Override
    public String toString() {
        return date + " @ " + location;
    }
}
