package com.News.NewsAPI.news;

import com.News.NewsAPI.finance.AlphaVantageResponse;
import com.News.NewsAPI.websocket.NewsWebSocketHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@Getter
@Slf4j
public class NewsService {


    private final WebClient webClient;
    private final WebClient userClient;
    private final WebClient financeClient;
    private final ObjectMapper objectMapper;
    private final NewsWebSocketHandler newsWebSocketHandler;
    private final List<Article> articles = new ArrayList<>();

    public NewsService(WebClient.Builder webClient, ObjectMapper objectMapper, NewsWebSocketHandler newsWebSocketHandler){
        this.webClient = webClient.baseUrl("https://newsapi.org/v2").build();
        this.userClient = webClient.baseUrl("http://localhost:8082/user").build();
        this.financeClient = webClient.baseUrl("https://www.alphavantage.co").build();
        this.objectMapper = objectMapper;
        this.newsWebSocketHandler= newsWebSocketHandler;
    }

    @Scheduled(fixedRate = 20000) // Fetch and broadcast news every 60 seconds
    public void fetchAndBroadcastNews() {

        getUpdates() // Replace "user-id" with actual user ID or handle multiple users
                .collectList()
                .doOnNext(articles -> {
                    log.info("Broadcasting {} articles", articles.size());
                    articles.forEach(article -> log.info("Article: {}", article));
                    newsWebSocketHandler.broadcastNews(articles);
                })
                .subscribe();
    }


    public Flux<Article> getUpdates() {

        List<String> keywords = Arrays.asList("technology", "science", "health", "business");
        String keyword = keywords.get(new Random().nextInt(keywords.size()));


        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/Everything")
                        .queryParam("q", keyword)
                        .queryParam("from", LocalDate.now().minusDays(1))
                        .queryParam("apikey", "f23c4fe55e434b2b94313c43cdcf44aa")
                        .queryParam("pageSize", 5)
                        .build()
                )
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(this::parseArticles)
                .doOnNext(article -> log.info("parsed article: {}", article));
    }


    public Flux<Article> getNews(int pageSize,String id) {

        return getPreferences(id)
                .flatMapMany(key -> webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/Everything")
                                .queryParam("q", key)
                                .queryParam("apikey", "f23c4fe55e434b2b94313c43cdcf44aa")
                                .queryParam("pageSize", pageSize)
                                .build()
                        )
                        .retrieve()
                        .bodyToFlux(String.class)
                        .flatMap(this::parseArticles)
                );
    }


    public Mono<String> getPreferences(String id) {
        return this.userClient.get()
                .uri("/getPref?id={id}", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .map(keywords-> String.join(" AND ", keywords))
                .doOnNext(k -> log.info(k));
    }


    public Flux<Article> search(int pageSize, String search) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/Everything")
                        .queryParam("q", search)
                        .queryParam("apikey", "f23c4fe55e434b2b94313c43cdcf44aa")
                        .queryParam("pageSize", pageSize)
                        .build()
                )
                .retrieve()
                .bodyToFlux(String.class)
                .flatMap(this::parseArticles)
                .doOnNext(article -> log.info("parsed article: {}", article));
    }




    private Flux<Article> parseArticles(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray articlesArray = jsonResponse.getJSONArray("articles");
            List<Article> articleList = objectMapper.readValue(articlesArray.toString(), new TypeReference<List<Article>>() {});
            return Flux.fromIterable(articleList);
        } catch (Exception e) {
            log.error("Error parsing articles", e);
            return Flux.empty();
        }
    }



    public Mono<AlphaVantageResponse> getIntradayData(String symbol) {
        return financeClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/query")
                        .queryParam("function", "TIME_SERIES_INTRADAY")
                        .queryParam("symbol", symbol)
                        .queryParam("interval", "1min")
                        .queryParam("apikey", "DMN8YVHBK5DBHGHZ")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::parseJson);
    }

    private Mono<AlphaVantageResponse> parseJson(String json) {
        try {
            AlphaVantageResponse response = objectMapper.readValue(json, AlphaVantageResponse.class);
            return Mono.just(response);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }





}















