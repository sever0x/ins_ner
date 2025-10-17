package com.sever0x.processor.mcp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GptOssMessage {

    @JsonProperty("role")
    private String role;

    @JsonProperty("content")
    private String content;

    @JsonProperty("tool_calls")
    private List<GptOssToolCall> toolCalls;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<GptOssToolCall> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<GptOssToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }
}
