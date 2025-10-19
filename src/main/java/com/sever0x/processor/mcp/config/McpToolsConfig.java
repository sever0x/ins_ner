package com.sever0x.processor.mcp.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.sever0x.processor.mcp.tools.EnrichmentTools;
import com.sever0x.processor.mcp.tools.NerMcpTools;
import com.sever0x.processor.mcp.tools.ParseTools;
import com.sever0x.processor.mcp.tools.ValidationTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfig {
	@Bean
	public ToolCallbackProvider toolCallbackProvider(NerMcpTools nerMcpTools,
	                                                 EnrichmentTools enrichmentTools,
	                                                 ParseTools parseTools,
	                                                 ValidationTools validationTools) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(nerMcpTools, enrichmentTools, parseTools, validationTools)
				.build();
	}

	@Bean
	ChatClient provideChatClient(ChatModel chatModel) {
		return ChatClient.create(chatModel);
	}

	@Bean
	Jackson2ObjectMapperBuilderCustomizer escapeNonAscii() {
		return builder -> builder.featuresToEnable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
	}

}
