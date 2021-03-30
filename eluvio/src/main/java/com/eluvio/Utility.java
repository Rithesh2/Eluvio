package com.eluvio;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


public class Utility{
    /** Max of 5 simultaneous calls */
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5);
    /** executes client calls */
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .executor(executorService)
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args){
        /** Assumed a list of ids would be provided by client, temporary ids provided for testing */

        String[] x = {"id1", "id2", "id3"};
        List<String> y = getItemInfo(x);
        y.stream().forEach(System.out::println);

    }
    /** Prevents unnecessary queries for item IDs that have already been seen */
    private static Set<String> uniqueIds(String[] list){
        Set<String> hash_Set = new HashSet<String>();
        for(String x : list){
            hash_Set.add(x);
        }
        return hash_Set;
    }
    /** Converts ID to base64 for authorization */
    private static String base(String x){
        return Base64.getEncoder().encodeToString(x.getBytes());
    }
    /** retrieves item information*/
    private static List<String> getItemInfo(String[] list){
        Set<String> idsSet = uniqueIds(list);

        List<CompletableFuture<String>> result = idsSet.stream()
                .map(id -> {
                    try {
                        return httpClient.sendAsync(
                                HttpRequest.newBuilder( new URI("https://eluv.io/items/" + id))
                                        .GET()
                                        .setHeader("Authorization", base(id))
                                        .build(),
                                HttpResponse.BodyHandlers.ofString())
                                .thenApply(response -> response.body());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .collect(Collectors.toList());

        return getList(result);
    }
    /** converts from completable future list to a regular string list */
    private static List<String> getList(List<CompletableFuture<String>> list){
        List<String> result = new ArrayList<>();
        for(CompletableFuture<String> future : list) {
            try {
                result.add(future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
