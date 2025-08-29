package com.ridingmate.api_server.infra.terra;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
public class TerraClient {

    private final WebClient terraWebClient;

}
