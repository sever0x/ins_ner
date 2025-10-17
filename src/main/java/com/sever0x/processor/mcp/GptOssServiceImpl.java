package com.sever0x.processor.mcp;

import com.sever0x.processor.mcp.dto.GptOssChatRequest;
import com.sever0x.processor.mcp.dto.GptOssChatResponse;
import com.sever0x.processor.mcp.dto.GptOssMessage;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GptOssServiceImpl implements GptOssService {

    @Override
    public GptOssChatResponse continueConversation(GptOssChatRequest request) {
        GptOssChatResponse response = new GptOssChatResponse();
        List<GptOssMessage> messages = request != null && request.getMessages() != null
                ? new ArrayList<>(request.getMessages())
                : new ArrayList<>();
        response.setMessages(messages);
        return response;
    }
}
