package com.sever0x.processor.mcp.service;

import com.sever0x.processor.NERResponse;
import com.sever0x.processor.docs.Preprocessor;
import jakarta.annotation.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class McpNerService {
	private final ChatClient chatClient;
	private final Preprocessor preprocessor;
	@Nullable
	private final VectorStore vectorStore;

	public McpNerService(ChatClient.Builder chatClientBuilder, Preprocessor preprocessor, @Nullable VectorStore vectorStore) {
		this.chatClient = chatClientBuilder.build();
		this.preprocessor = preprocessor;
		this.vectorStore = vectorStore;
	}

	public NERResponse extract(String rawText, @Nullable String filename) {
		String text = preprocessor.normalize(rawText);

		String ragContext = "";
		if (vectorStore != null) {
			var top = vectorStore.similaritySearch(
					SearchRequest.builder().query(text).topK(5).build()
			);
			ragContext = top.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));
		}
		var converter = new BeanOutputConverter<>(NerPayload.class);
		String format = converter.getFormat();

		String system = """
				Ти — досвідчена NER-система, спеціалізована на страхових документах (страхові поліси, листи, звіти, рахунки).
				Твоя задача — витягати іменовані сутності зі вхідного тексту та повертати ТІЛЬКИ валідний JSON (відповідно до RFC8259),
				який строго відповідає поданій JSON Schema.
				
				Мова вхідного тексту — німецька або англійська.
				
				Схема JSON:
				{
				  "entities": {
				    "client_name": string[],
				    "client_number": string[],
				    "contract_number": string[],
				    "client_firma_name": string[],
				    "client_birthdate": string[],
				    "insurer_firma_name": string[]
				  }
				}
				
				Інструкції:
				- Повертай лише ті значення, що явно присутні у вхідному тексті (жодних домислів чи нормалізації).
				- Використовуй точний текстовий фрагмент (як у джерелі).
				- Якщо сутності відсутні — повертай порожні масиви [].
				- Видаляй дублікати (навіть якщо у тексті кілька згадок одного і того ж значення).
				- Під "client_firma_name" розумій компанію-клієнта (страхувальника), а також її дочірні компанії.
				- Під "insurer_firma_name" розумій страхову компанію або сервісну організацію, що обслуговує клієнта.
				- Під "client_name" розумій фізичних осіб, які представляють клієнта (наприклад, фінансового директора, бухгалтера).
				- Не додавай представників страховика у "client_name".
				- Під "contract_number" розпізнавай усі послідовності, що схожі на номери страхових договорів чи полісів.
				- Значення з RAG-контексту — лише допоміжна підказка, але остаточні сутності витягуй лише з основного тексту.
				- Результат повинен бути чистим JSON без коментарів, пояснень або додаткового тексту.
				""";

		String user = """
				 Контекст:
				 ```\s
				 %s
				 ```
				
				 Текст:
				 ```
				 %s
				 ```
				
				 %s
				\s""".formatted(ragContext, text, format);

		try {
			NerPayload payload = chatClient.prompt().system(system).user(user).call().entity(converter);
			Map<String, List<String>> entities = safeEntities(payload);
			return new NERResponse(getFilename(filename), entities);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, List<String>> safeEntities(NerPayload p) {
		if (p == null || p.entities == null) {
			return Map.of();
		}
		Map<String, List<String>> cleaned = new LinkedHashMap<>();
		p.entities.forEach((k, v) -> {
			if (v != null && !v.isEmpty()) {
				List<String> vals = v.stream()
						.map(s -> s == null ? "" : s.trim())
						.filter(s -> !s.isEmpty())
						.distinct()
						.toList();

				if (!vals.isEmpty()) {
					cleaned.put(k, vals);
				}
			}
		});
		return cleaned;
	}

	private String getFilename(@Nullable String filename) {
		return filename == null ? "" : filename;
	}

	public record NerPayload(Map<String, List<String>> entities) {
	}
}
