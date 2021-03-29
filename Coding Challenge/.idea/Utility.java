import com.google.common.collect.Streams;
import org.apache.commons.math3.util.Pair;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.time.Duration;

public class Utility{
    /** Max of 5 simulatneous calls */
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .executor(executorService)
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args){
        /** Assumed a list of ids would be provided by client */

        Set<String> ids_Set = uniqueIds(ids);
        List<URI> targets = new List<URI>;

        /** creates the call for each id */
        for(String y : ids_Set) {
            URI temp = new URI("https://eluv.io/items/" + y + "-H \"Authorization:" + base(y) + "\"");
            targets.add(temp);
        }

        /** gets information about ids */
        List<CompletableFuture<String>> result = targets.stream()
                .map(url -> httpClient.sendAsync(
                        HttpRequest.newBuilder(url)
                                .GET()
                                // .setHeader()
                                .build(),
                        HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> response.body()))
                .collect(Collectors.toList());

        /**pairs ids and information*/
        List<Pair> pairs = Streams.zip(targets.stream(), result.stream(), Pair::new)
                .collect(Collectors.toList());

        /**prints out each unique id and information about id for client*/
        for(Pair pair : pairs){
            System.out.println(pair.key + ": " + pair.value);
        }
    }
    /** Prevents unnecessary queries for item IDs that have already been seen */
    private Set<String> uniqueIds(List<String> list){
        Set<String> hash_Set = new HashSet<String>();
        for(String x : list){
            hash_Set.add(x);
        }
        return hash_Set;
    }
    /** Converts ID to base64 for authorization */
    private String base(String x){
        return Base64.getEncoder().encodeToString(x.getBytes());
    }
}
