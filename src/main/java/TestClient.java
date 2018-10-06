import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class TestClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 10101)
                .usePlaintext(true)
                .build();
        FutsalGrpc.FutsalStub stub = FutsalGrpc.newStub(channel);

        GameNotification notification = GameNotification
                .newBuilder()
                .setText("asd")
                .build();

        stub.notify(notification, new StreamObserver<None>() {
            @Override
            public void onNext(None value) {

            }

            @Override
            public void onError(Throwable t) {
                System.out.println(t);
            }

            @Override
            public void onCompleted() {
                System.out.println("DONE");
            }
        });

        Thread.sleep(5000);
    }
}
