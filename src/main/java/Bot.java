import com.google.gson.Gson;
import data.Configuration;
import data.Game;
import data.GameDetail;
import data.Player;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import network.FutsalService;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import pt.migafgarcia.futsalbot.FutsalGrpc;
import pt.migafgarcia.futsalbot.GameNotification;
import pt.migafgarcia.futsalbot.None;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Bot {

    private static Configuration configuration;

    static {
        try {
            configuration = new Gson().fromJson(new FileReader("futsal.conf"), Configuration.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private static final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC);

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .cache(new Cache(new File("."), 10 * 1024 * 1024))
            .addInterceptor(interceptor)
            .build();

    private static final Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(configuration.getBaseUrl())
            .build();

    private static final FutsalService futsalService = retrofit.create(FutsalService.class);

    private static DiscordApi api = new DiscordApiBuilder()
            .setToken(configuration.getToken())
            .login()
            .join();

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    static {
        sdf.setTimeZone(TimeZone.getTimeZone(ZonedDateTime.now().getZone()));
    }

    private static final String COMMAND_GAMES = "!games";
    private static final String COMMAND_HELP = "!help";
    private static final String COMMAND_NOTIFY = "!notify";

    private static String cookie;

    private static final String help = "Supported commands:\n\t!help --> Prints this message\n\t!games --> Lists all available games\n\nI am a bot, beep boop...";

    public static void main(String[] args) throws InterruptedException, IOException {

        Futsal futsal = new Futsal();

        Server server = ServerBuilder.forPort(10101).addService(futsal).build();

        server.start();

        api.addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase(COMMAND_GAMES)) {
                getGamesDetail(new Observer<GameDetail>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        if(event.getMessage().getUserAuthor().isPresent())
                            event.getChannel().sendMessage("Howdy " + event.getMessage().getUserAuthor().get().getNicknameMentionTag());
                        else
                            event.getChannel().sendMessage("Hey stranger...");
                    }

                    @Override
                    public void onNext(GameDetail gameDetail) {
                        System.out.println(gameDetail.toString());
                        event.getChannel().sendMessage(gameDetail.toString());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        event.getChannel().sendMessage("I am a bot, beep boop...");
                    }
                });
            }
        });

        api.addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase(COMMAND_HELP)) {
                if(event.getMessage().getUserAuthor().isPresent())
                    event.getChannel().sendMessage("Howdy " + event.getMessage().getUserAuthor().get().getNicknameMentionTag());
                else
                    event.getChannel().sendMessage("Hey stranger...");

                event.getChannel().sendMessage(help);
            }
        });

        api.addMessageCreateListener(event -> {
            if (event.getMessage().getContent().equalsIgnoreCase(COMMAND_NOTIFY)) {
                futsal.addTextChannel(event.getChannel());
            }
        });

        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());

        server.awaitTermination();
    }

    private static void getGames(Observer<String> observer) {

        futsalService
                .login(System.currentTimeMillis() / 1000, configuration.getUsername(), configuration.getPw())
                .flatMap(response -> {
                    String cookie = "";
                    if (response.isSuccessful()) {
                        String body = response.body();
                        if (!body.startsWith("-1")) {
                            cookie = response.headers().get("Set-Cookie");
                        }
                        else {
                            throw new RuntimeException("Login failed");
                        }
                    }
                    return futsalService.getGames(cookie, System.currentTimeMillis() / 1000);
                })
                .map(stringResponse -> stringResponse.body())
                .subscribe(observer);

    }

    private static void getGamesList(Observer<List<Game>> observer) {

        futsalService
                .login(System.currentTimeMillis() / 1000, configuration.getUsername(), configuration.getPw())
                .flatMap(response -> {
                    String cookie;
                    if (response.isSuccessful()) {
                        String body = response.body();
                        if (!body.startsWith("-1")) {
                            cookie = response.headers().get("Set-Cookie");
                        }
                        else {
                            throw new RuntimeException("Login failed");
                        }
                    }
                    else {
                        throw new RuntimeException("Response not successful");
                    }
                    return futsalService.getGames(cookie, System.currentTimeMillis() / 1000);
                })
                .map(response -> {

                    List<Game> games;
                    System.out.println("Body:" + response.body());

                    if(response.isSuccessful()) {
                        String body = response.body();
                        if (body.startsWith("-1"))
                            throw new RuntimeException("Request failed");
                        else
                            games = parseGames(body);
                    }
                    else
                        throw new RuntimeException("Response not successful");

                    System.out.println(games);
                    return games;
                })
                .subscribe(observer);

    }

    private static void getGamesDetail(Observer<GameDetail> observer) {

        Observable<Game> gamesObservable = futsalService
                .login(System.currentTimeMillis() / 1000, configuration.getUsername(), configuration.getPw())
                .flatMap(response -> {

                    if (response.isSuccessful()) {
                        String body = response.body();
                        if (!body.startsWith("-1")) {
                            cookie = response.headers().get("Set-Cookie");
                        }
                        else {
                            throw new RuntimeException("Login failed");
                        }
                    }
                    else {
                        throw new RuntimeException("Response not successful");
                    }
                    return futsalService.getGames(cookie, System.currentTimeMillis() / 1000);
                })
                .flatMap(response -> {

                    List<Game> games;

                    if(response.isSuccessful()) {
                        String body = response.body();
                        if (body.startsWith("-1"))
                            throw new RuntimeException("Request failed");
                        else
                            games = parseGames(body);
                    }
                    else
                        throw new RuntimeException("Response not successful");

                    System.out.println(games);
                    return Observable.fromIterable(games);
                });


        Observable<List<Player>> playersObservable = gamesObservable
                .flatMap(game -> futsalService.getGameStatus(cookie, System.currentTimeMillis() / 1000, game.getId()))
                .map(response -> {

                    List<Player> players;

                    if(response.isSuccessful()) {
                        String body = response.body();
                        if (body.startsWith("-1"))
                            throw new RuntimeException("Request failed");
                        else
                            players = parsePlayers(body);
                    }
                    else
                        throw new RuntimeException("Response not successful");


                    System.out.println(players);
                    return players;
                });


        gamesObservable
                .zipWith(playersObservable, (game, players) -> GameDetail.newGameDetail().game(game).players(players).build())
                .subscribe(observer);


    }

    private static List<Player> parsePlayers(String body) throws ParseException {
        List<Player> players = new ArrayList<>();
        Scanner scanner = new Scanner(body);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] split = line.split(",");

            if(split.length == 6) {
                Player p = Player.newPlayer()
                        .id(Integer.valueOf(split[5].trim()))
                        .username(split[0].trim())
                        .status(split[1].trim())
                        .registered(Integer.valueOf(split[2].trim()) == 1)
                        .late(Integer.valueOf(split[3].trim()) == 1)
                        .date(sdf.parse(split[4].trim()))
                        .build();
                if(p.isRegistered()) {
                    players.add(p);
                }
            }
        }

        return players;
    }

    private static List<Game> parseGames(String body) throws ParseException {
        List<Game> games = new ArrayList<>();
        Scanner scanner = new Scanner(body);
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] split = line.split(",");

            if(split.length == 4) {
                games.add(
                        Game.newGame()
                        .id(Integer.valueOf(split[0].trim()))
                        .date(sdf.parse(split[1].trim()))
                        .location(split[3].trim())
                        .build()
                );
            }
        }

        return games;
    }

    private static class Futsal extends FutsalGrpc.FutsalImplBase {

        private List<TextChannel> textChannels = new ArrayList<>();

        void addTextChannel(TextChannel textChannel) {
            textChannels.add(textChannel);
        }


        @Override
        public void notify(GameNotification request, StreamObserver<None> responseObserver) {
            System.out.println(request);

            for (TextChannel textChannel : textChannels) {
                textChannel.sendMessage("Yo! " + request.getText());
            }


            responseObserver.onNext(None.newBuilder().build());
            responseObserver.onCompleted();
        }
    }

}
