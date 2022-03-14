package com.openocean.arb.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openocean.arb.common.exception.BizException;
import lombok.experimental.UtilityClass;

import java.io.IOException;

@UtilityClass
public class JacksonUtil {
    private static ObjectMapper mapper = new ObjectMapper();

    public String toJSONStr(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BizException(e.getMessage());
        }
    }

    public <T> T parseObject(String jsonStr, Class<T> type) {
        try {
            return mapper.readValue(jsonStr, type);
        } catch (IOException e) {
            throw new BizException(e.getMessage());
        }
    }

}
