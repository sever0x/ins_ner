package com.sever0x.processor.mcp;

import com.sever0x.processor.mcp.dto.GptOssChatRequest;
import com.sever0x.processor.mcp.dto.GptOssChatResponse;
import com.sever0x.processor.mcp.dto.GptOssMessage;
import com.sever0x.processor.mcp.dto.GptOssToolCall;
import com.sever0x.processor.mcp.dto.GptOssToolFunction;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private final GptOssService gptOssService;

    public McpController(GptOssService gptOssService) {
        this.gptOssService = gptOssService;
    }

    @PostMapping
    public ResponseEntity<GptOssChatResponse> handleMcp(@RequestBody GptOssChatRequest request) {
        validateRequest(request);
        GptOssChatResponse response = gptOssService.continueConversation(request);
        return ResponseEntity.ok(response);
    }

    private void validateRequest(GptOssChatRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getMessages())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one message is required.");
        }

        for (GptOssMessage message : request.getMessages()) {
            if (!StringUtils.hasText(message.getRole())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message role is required.");
            }

            boolean hasContent = StringUtils.hasText(message.getContent());
            List<GptOssToolCall> toolCalls = message.getToolCalls();
            boolean hasToolCalls = !CollectionUtils.isEmpty(toolCalls);

            if (!hasContent && !hasToolCalls) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Each message must contain content or a tool_calls array.");
            }

            if (hasToolCalls) {
                for (GptOssToolCall toolCall : toolCalls) {
                    if (!StringUtils.hasText(toolCall.getId())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tool call id is required.");
                    }
                    if (!StringUtils.hasText(toolCall.getType())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tool call type is required.");
                    }
                    GptOssToolFunction function = toolCall.getFunction();
                    if (function == null
                            || !StringUtils.hasText(function.getName())
                            || !StringUtils.hasText(function.getArguments())) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Tool call function name and arguments are required.");
                    }
                }
            }
        }
    }
}
