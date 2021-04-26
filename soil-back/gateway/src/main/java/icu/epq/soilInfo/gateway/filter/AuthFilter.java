package icu.epq.soilInfo.gateway.filter;

import com.google.gson.Gson;
import icu.epq.soilInfo.common.tool.R;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * token认证
 *
 * @author EPQ
 */
@Component
@AllArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {

    private StringRedisTemplate redisTemplate;

    @SneakyThrows
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authorization == null) {
            return exchange.getResponse().writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(new Gson().toJson(R.fail("非法登录")).getBytes(StandardCharsets.UTF_8))));
        }
        String[] msg = authorization.split(":");
        if (msg.length != 2) {
            return exchange.getResponse().writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(new Gson().toJson(R.fail("非法登录")).getBytes(StandardCharsets.UTF_8))));
        }
        String username = msg[0];
        String token = msg[1];
        Object value = redisTemplate.opsForHash().get("token_cache", username);
        if (value != null && value.toString().equals(token)) {
            return chain.filter(exchange);
        }

        return exchange.getResponse().writeWith(Flux.just(exchange.getResponse().bufferFactory().wrap(new Gson().toJson(R.fail("非法登录")).getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * 设置最高优先级
     *
     * @return
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
