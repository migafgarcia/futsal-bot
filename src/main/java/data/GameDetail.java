package data;

import java.util.List;

public class GameDetail {

    private Game game;
    private List<Player> players;

    private GameDetail(Builder builder) {
        this.game = builder.game;
        this.players = builder.players;
    }

    public static Builder newGameDetail() {
        return new Builder();
    }

    @Override
    public String toString() {
        return game + " --> " + players;
    }

    public static final class Builder {
        private Game game;
        private List<Player> players;

        private Builder() {
        }

        public GameDetail build() {
            return new GameDetail(this);
        }

        public Builder game(Game game) {
            this.game = game;
            return this;
        }

        public Builder players(List<Player> players) {
            this.players = players;
            return this;
        }
    }
}
