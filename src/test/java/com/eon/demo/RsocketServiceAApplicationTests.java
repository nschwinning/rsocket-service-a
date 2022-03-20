package com.eon.demo;

/*
@SpringBootTest("rsocket.port=${test.rsocket.server.port}")
@ExtendWith(RSocketServerExtension.class)
class RsocketServiceAApplicationTests {

	@Autowired
	private RSocketRequester rsocketRequester;
	
	@Autowired
	private ConfigurableApplicationContext application;
	
	@Test
	void testStream(RSocketMessageRegistry catalog) {
		
		RSocketServerBootstrap server = application.getBean(RSocketServerBootstrap.class);

		
		
		MessageMapping response = MessageMapping.stream("quotes")
				.response(new Quote(1, "Blablabla"));
		
		catalog.register(response);
		
		rsocketRequester.route("quotes").data(1)
			.retrieveFlux(Quote.class).subscribe(q -> System.out.println(q));
	}
	
	@Test
	void testReqRes(RSocketMessageRegistry catalog) {
		MessageMapping response = MessageMapping.response("quote")
				.response(new Quote(1, "Blablabla"));
		
		catalog.register(response);
		
		Quote q = rsocketRequester.route("quotes").data(1)
			.retrieveMono(Quote.class)
			.block();
		
		assertThat(q.getMessage()).isEqualTo("Wurst");

	}
	
	@Bean 
	public RSocketMessageRegistry catalog() {
		return new CustomRSocketMessageRegistry();
	}

}

class CustomRSocketMessageRegistry implements RSocketMessageRegistry {
	
	private Map<String, MessageMapping> maps = new HashMap<>();

	@Override
	public Collection<MessageMapping> getMappings() {
		List<MessageMapping> values = new ArrayList<>(maps.values());
		return values;
	}

	@Override
	public MessageMapping getMapping(String name) {
		for (MessageMapping map : getMappings()) {
			if (map.matches(null, name)) {
				return map;
			}
		}
		return null;
	}

	@Override
	public void register(MessageMapping map) {
		maps.put(map.getPattern(), map);		
	}
	
}
*/
