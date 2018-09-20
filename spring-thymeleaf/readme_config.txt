    /**
     * Komplet mauelle Konfiguration ohne Spring-Properties.
     *
     * @return {@link Jackson2ObjectMapperBuilder}
     */
    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder()
    {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        // @formatter:off
        builder
            .createXmlMapper(false)
            .dateFormat(new SimpleDateFormat("yyyy-MM-dd"))
            // Deserialization
            .featuresToDisable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT,DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToEnable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            // Serialization
            .indentOutput(true)
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
            .locale(Locale.GERMANY)
            // .modules(modules)
            .timeZone("Europe/Berlin");
        // @formatter:on

        return builder;
    }

---------------------------------------------------------------------------------------------------

    /**
     * Globaler JSON-Mapper mit Spring-Properties für Detail-Konfiguration.
     *
     * @param builder {@link Jackson2ObjectMapperBuilder}
     * @return {@link ObjectMapper}
     */
    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper(final Jackson2ObjectMapperBuilder builder)
    {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

		// Diese Module sind bereits in spring-boot-starter-json enthalten (Release 2.X).
        // Some other custom configuration for supporting Java 8 features
        // objectMapper.registerModule(new Jdk8Module());
        // objectMapper.registerModule(new JavaTimeModule());
        
        // AnnotationIntrospector jaxbIntrospector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        // AnnotationIntrospector jacksonIntrospector = new JacksonAnnotationIntrospector();
        //
        // // Annotation-Mix: Verwende primär JaxB-Annotations und sekundär Jackson-Annotations
        // AnnotationIntrospector introspector = new AnnotationIntrospectorPair(jaxbIntrospector, jacksonIntrospector);

        // objectMapper.setAnnotationIntrospector(introspector);
        // objectMapper.getDeserializationConfig().with(introspector);
        // objectMapper.getSerializationConfig().with(introspector)        

        return objectMapper;
    }

---------------------------------------------------------------------------------------------------

    Formattierung der LocaDate*Klassen, siehe JavaTimeModule.

    public static final DateTimeFormatter FORMATTER = ofPattern("dd::MM::yyyy");

    @Bean
    @Primary
    public ObjectMapper serializingObjectMapper()
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer());
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

    public class LocalDateSerializer extends JsonSerializer<LocalDate>
    {
        @Override
        public void serialize(final LocalDate value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException
        {
            gen.writeString(value.format(FORMATTER));
        }
    }

    public class LocalDateDeserializer extends JsonDeserializer<LocalDate>
    {
        @Override
        public LocalDate deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException
        {
            return LocalDate.parse(p.getValueAsString(), FORMATTER);
        }
    }
   