// Created: 11.02.2026
package de.ai.jlama;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.github.tjake.jlama.model.AbstractModel;
import com.github.tjake.jlama.model.ModelSupport;
import com.github.tjake.jlama.model.functions.Generator;
import com.github.tjake.jlama.safetensors.DType;
import com.github.tjake.jlama.safetensors.prompt.PromptContext;
import com.github.tjake.jlama.util.Downloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <a href="https://github.com/tjake/Jlama/blob/main/README.md">Jlama Github</a>
 * <a href="https://deepwiki.com/tjake/Jlama">Jlama Wiki</a>
 *
 * @author Thomas Freese
 */
public final class JLamaMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(JLamaMain.class);

    static AbstractModel loadModel(final String workingDir, final String model) throws IOException {
        LOGGER.info("Loading local model from {}", workingDir);

        final File localModelPath = new Downloader(workingDir, model).huggingFaceModel();

        return ModelSupport.loadModel(localModelPath, DType.F32, DType.I8);
    }

    static void main() throws IOException {
        // To pass the Proxy.
        // System.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");

        final String workingDir = System.getProperty("user.home") + "/.jLamaModels";

        // Available models: https://huggingface.co/tjake
        // final String modelName = "tjake/Llama-3.2-1B-Instruct-JQ4";
        final String modelName = "tjake/Llama-3.2-3B-Instruct-JQ4";
        // final String modelName = "tjake/Llama-3.1-8B-Instruct-JQ4";

        // final String prompt = "Why are llamas so cute?";
        final String prompt = "Warum sind Lamas so niedlich?";

        final UUID session = UUID.randomUUID();

        try (AbstractModel model = loadModel(workingDir, modelName)) {
            final PromptContext promptContext = model.promptSupport()
                    .map(promptSupport -> promptSupport
                            .builder()
                            .addSystemMessage("You are a helpful chatbot who writes short responses.")
                            .addUserMessage(prompt)
                            .build()
                    )
                    .orElseGet(() -> {
                        LOGGER.warn("JLama-Model does not have PromptSupport, ignore SystemPrompt.");
                        return PromptContext.of(prompt);
                    });

            LOGGER.info("Question: {}", prompt);

            final Generator.Response response = model.generate(session, promptContext, 0.75F, 512);

            // final Generator.Response response = model.generateBuilder()
            //         .session(session)
            //         .promptContext(promptContext)
            //         .ntokens(512)
            //         .temperature(0.75F)
            //         .generate();

            LOGGER.info("generatedTokens={}, generateTimeMs={}, promptTimeMs={}, promptTokens={}",
                    response.generatedTokens,
                    response.generateTimeMs,
                    response.promptTimeMs,
                    response.promptTokens);
            // LOGGER.info(response.responseText);
            LOGGER.info(new String(response.responseText.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));

            final float[] embeddings = model.embed("I am some Text", Generator.PoolingType.AVG);
            LOGGER.info("Embeddings length: {}", embeddings.length);
            LOGGER.info("Embeddings: {}", embeddings);
        }
    }

    private JLamaMain() {
        super();
    }
}
