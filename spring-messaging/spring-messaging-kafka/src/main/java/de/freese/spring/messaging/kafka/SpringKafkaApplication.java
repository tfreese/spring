// Created: 18.12.22
package de.freese.spring.messaging.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class SpringKafkaApplication
{
    public static void main(String[] args)
    {
        // EmbeddedKafkaContextCustomizer

        SpringApplication.run(SpringKafkaApplication.class, args);
    }

    //    @Bean
    //    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory)
    //    {
    //        return new KafkaTemplate<>(producerFactory);
    //    }
    //
    //    @Bean
    //    public ProducerFactory<String, String> producerFactory()
    //    {
    //        Map<String, Object> config = new HashMap<>();
    //
    //        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    //        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    //        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    //
    //        return new DefaultKafkaProducerFactory<>(config);
    //    }
}
