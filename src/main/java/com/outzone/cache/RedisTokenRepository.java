package com.outzone.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Component
public class RedisTokenRepository implements PersistentTokenRepository {

    private final static String REMEMBER_ME_KEY = "spring:security:";

    @Resource
    RedisTemplate<Object, Object> template;

    private void setToken(PersistentRememberMeToken token){
        Map<String,String>  map =  new HashMap<>();
        map.put("username",token.getUsername());
        map.put("series", token.getSeries());
        map.put("tokenValue", token.getTokenValue());
        map.put("date",""+token.getDate().getTime());
        template.opsForHash().putAll(REMEMBER_ME_KEY+token.getSeries(),map);
        template.expire(REMEMBER_ME_KEY+token.getSeries(),1, TimeUnit.DAYS);

    }

    private PersistentRememberMeToken getToken(String series){
        Map<Object,Object> map = template.opsForHash().entries(REMEMBER_ME_KEY+series);
        if(map.isEmpty()) return null;
        else return new PersistentRememberMeToken((String) map.get("username"),
                series,
                (String) map.get("tokenValue"),
                new Date(Long.parseLong((String) map.get("date"))));

    }

    @Override
    public void createNewToken(PersistentRememberMeToken token) {
        template.opsForValue().set(REMEMBER_ME_KEY+"username:"+token.getUsername(),token.getSeries());
        template.expire(REMEMBER_ME_KEY+"username:"+token.getUsername(),1,TimeUnit.DAYS);
        this.setToken(token);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        PersistentRememberMeToken token = this.getToken(series);
        if(token != null)
            this.setToken(new PersistentRememberMeToken(token.getUsername(),series,tokenValue,lastUsed));
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String seriesId) {

        return this.getToken(seriesId);
    }

    @Override
    public void removeUserTokens(String username) {
        String series = (String) template.opsForValue().get(username);
        template.delete(REMEMBER_ME_KEY+series);
        template.delete(REMEMBER_ME_KEY+"username:"+username);

    }
}
