package com.sever0x.processor.mcp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GptOssChatRequest {

    @JsonProperty("messages")
    private List<GptOssMessage> messages;

    public List<GptOssMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<GptOssMessage> messages) {
        this.messages = messages;
    }
}
