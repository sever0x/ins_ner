package com.sever0x.processor.mcp;

import com.sever0x.processor.mcp.dto.GptOssChatRequest;
import com.sever0x.processor.mcp.dto.GptOssChatResponse;

public interface GptOssService {

    GptOssChatResponse continueConversation(GptOssChatRequest request);
}
