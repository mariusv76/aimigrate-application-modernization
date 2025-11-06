package org.pwte.example.jaxrs.test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.ws.rs.core.MediaType;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import junit.framework.TestCase;

public class CustomerOrderRESTTest extends TestCase {

	private String urlPrefix; 
	private String urlTestPrefix;
	private ObjectMapper mapper = new ObjectMapper();
	
	private ClientConfig clientConfig = new ClientConfig();
	private ClientConfig clientConfig2 = new ClientConfig();
		
	public void setUp() throws Exception 
	{
		try {
			Context envEntryContext = (Context) new InitialContext().lookup("java:comp/env");
			urlPrefix = (String) envEntryContext.lookup("CUSTOMER_ORDER_SERVICES_WEB_ENDPOINT");
			urlTestPrefix = (String) envEntryContext.lookup("CUSTOMER_ORDER_SERVICES_TEST_ENDPOINT");
		} catch (NamingException e) {
			e.printStackTrace();
			urlPrefix = "https://localhost:9443/CustomerOrderServicesWeb/";
			urlTestPrefix = "http://localhost:9080/CustomerOrderServicesTest/";
		}
		
		javax.ws.rs.core.Application app = new javax.ws.rs.core.Application() {
	        public Set<Class<?>> getClasses() {
	            Set<Class<?>> classes = new HashSet<Class<?>>();
	    		classes.add(com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider.class);
	    		
	            return classes;
	        }
	    };
	    
	    clientConfig.applications(app);
	    clientConfig2.applications(app);
	    
		clientConfig.setLoadWinkApplications(false);
		clientConfig2.setLoadWinkApplications(false);
		
		BasicAuthSecurityHandler basicAuth = new BasicAuthSecurityHandler();
		basicAuth.setUserName("rbarcia");
		basicAuth.setPassword("bl0wfish");
		clientConfig.handlers(basicAuth);
		
		BasicAuthSecurityHandler basicAuth2 = new BasicAuthSecurityHandler();
		basicAuth2.setUserName("kbrown");
		basicAuth2.setPassword("bl0wfish");
		clientConfig2.handlers(basicAuth2);
	}
	
	public void testLoadCustomer() throws Exception
	{
		RestClient client = new RestClient(clientConfig);
		
		Resource resource = client.resource(urlPrefix + "jaxrs/Customer");
		ClientResponse resourceResponse = resource.accept("application/json").get();
		String customerJson = resourceResponse.getEntity(String.class);
		JsonNode customer = mapper.readTree(customerJson);
		
		RestClient clientTest = new RestClient();
		Resource resourceTest = clientTest.resource(urlTestPrefix+"sampleJSON/customer.json");
		ClientResponse clientTestResponse = resourceTest.accept("application/json").get();
		
		assertEquals(200, resourceResponse.getStatusCode());
		assertEquals(MediaType.APPLICATION_JSON, resourceResponse.getHeaders().get("Content-Type").get(0));
		
		String customerTestJson = clientTestResponse.getEntity(String.class);
		JsonNode customerTest = mapper.readTree(customerTestJson);
		assertEquals(customer.get("name").asText(), customerTest.get("name").asText());
		assertEquals(customer.get("householdSize").asInt(), customerTest.get("householdSize").asInt());
		assertEquals(customer.get("RESIDENTIAL").asText(), customerTest.get("RESIDENTIAL").asText());
		assertEquals(customer.get("frequentCustomer").asText(), customerTest.get("frequentCustomer").asText());
		
	}
	
	public void testUpdateAddress() throws IOException
	{
		
		RestClient clientTest = new RestClient();

		Resource resourceTest = clientTest.resource(urlTestPrefix+"sampleJSON/newAddress1.json");
		String newAddress1Json = resourceTest.accept("application/json").get(String.class);
		JsonNode newAddress1 = mapper.readTree(newAddress1Json);
		
		RestClient client = new RestClient(clientConfig);

		Resource customerAddress = client.resource(urlPrefix + "jaxrs/Customer/Address");
		ClientResponse clientResponse = customerAddress.contentType(MediaType.APPLICATION_JSON).put(newAddress1Json);
		
		assertEquals(204, clientResponse.getStatusCode());
		
		Resource resource = client.resource(urlPrefix + "jaxrs/Customer");
		String customerJson = resource.accept("application/json").get(String.class);
		JsonNode customer = mapper.readTree(customerJson);
		
		assertEquals(newAddress1.toString(), customer.get("address").toString());
		
		Resource resourceTest2 = clientTest.resource(urlTestPrefix+"sampleJSON/newAddress2.json");
		String newAddress2Json = resourceTest2.accept("application/json").get(String.class);
		JsonNode newAddress2 = mapper.readTree(newAddress2Json);
		
		Resource customerAddress2 = client.resource(urlPrefix + "jaxrs/Customer/Address");
		ClientResponse clientResponse2 = customerAddress2.contentType(MediaType.APPLICATION_JSON).put(newAddress2Json);
		
		assertEquals(204, clientResponse2.getStatusCode());
		
		Resource resource2 = client.resource(urlPrefix + "jaxrs/Customer");
		String customer2Json = resource2.accept("application/json").get(String.class);
		JsonNode customer2 = mapper.readTree(customer2Json);
		
		assertEquals(newAddress2.toString(), customer2.get("address").toString());
	}
	
	public void testOrderProcess() throws IOException
	{
		RestClient client = new RestClient(clientConfig);
		RestClient clientTest = new RestClient();
		
		Resource liTest = clientTest.resource(urlTestPrefix+"sampleJSON/LineItem1.json");
		String li1Json = liTest.accept("application/json").get(String.class);
		JsonNode li1 = mapper.readTree(li1Json);
		
		Resource addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		ClientResponse clientResponse = addTest.accept("application/json").contentType("application/json").post(li1Json);
		var headers = clientResponse.getHeaders();
		List<String> etag = headers.get("ETag");
		System.out.println("ETag -> " + etag);
		String version = "-1";
		if(etag != null) version = etag.get(0);
		
		assertEquals(200, clientResponse.getStatusCode());
		String openOrderJson = clientResponse.getEntity(String.class);
		JsonNode openOrder = mapper.readTree(openOrderJson);
		
		assertEquals(1, openOrder.get("lineitems").size());
		
		Resource custOrder = client.resource(urlPrefix + "jaxrs/Customer");
		String customerJson = custOrder.accept("application/json").get(String.class);
		JsonNode customer = mapper.readTree(customerJson);
		
		JsonNode openOrder2 = customer.get("openOrder");
		
		assertEquals(openOrder2.get("total").asDouble(), openOrder.get("total").asDouble());
		assertEquals(openOrder2.get("status").asText(), openOrder.get("status").asText());
		assertEquals(openOrder2.get("orderId").asLong(), openOrder.get("orderId").asLong());
		assertEquals(openOrder2.get("lineitems").size(), openOrder.get("lineitems").size());
		
		liTest = clientTest.resource(urlTestPrefix+"sampleJSON/LineItem2.json");
		String li2Json = liTest.accept("application/json").get(String.class);
		JsonNode li2 = mapper.readTree(li2Json);
		
		addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		clientResponse = addTest.accept("application/json").contentType("application/json").post(li1Json);
		assertEquals(412, clientResponse.getStatusCode());
		
		addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		clientResponse = addTest.header("If-Match", version).accept("application/json").contentType("application/json").post(li2Json);
		
		assertEquals(200, clientResponse.getStatusCode());
		openOrderJson = clientResponse.getEntity(String.class);
		openOrder = mapper.readTree(openOrderJson);
		version = clientResponse.getHeaders().get("ETag").get(0);
		assertEquals(2, openOrder.get("lineitems").size());
		
		liTest = clientTest.resource(urlTestPrefix+"sampleJSON/LineItem3.json");
		String li3Json = liTest.accept("application/json").get(String.class);
		JsonNode li3 = mapper.readTree(li3Json);
		
		addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		clientResponse = addTest.header("If-Match", version).accept("application/json").contentType("application/json").post(li3Json);
		
		version = clientResponse.getHeaders().get("ETag").get(0);
		
		assertEquals(200, clientResponse.getStatusCode());
		openOrderJson = clientResponse.getEntity(String.class);
		openOrder = mapper.readTree(openOrderJson);
		assertEquals(2, openOrder.get("lineitems").size());
		
		long newQuan = li2.get("quantity").asLong() + li3.get("quantity").asLong();
		
		JsonNode lis = openOrder.get("lineitems");
		for (JsonNode liCheck : lis) {
			if(liCheck.get("productId").asLong() == li2.get("productId").asLong())
			{
				assertEquals(newQuan, liCheck.get("quantity").asLong());
				break;
			}
		}
		
		liTest = clientTest.resource(urlTestPrefix+"sampleJSON/LineItem4.json");
		String li4Json = liTest.accept("application/json").get(String.class);
		JsonNode li4 = mapper.readTree(li4Json);
		
		addTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem");
		clientResponse = addTest.header("If-Match", version).accept("application/json").contentType("application/json").post(li4Json);
		
		version = clientResponse.getHeaders().get("ETag").get(0);
		
		assertEquals(200, clientResponse.getStatusCode());
		openOrderJson = clientResponse.getEntity(String.class);
		openOrder = mapper.readTree(openOrderJson);
		assertEquals(3, openOrder.get("lineitems").size());
		
		
		Resource removeTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder/LineItem/"+li4.get("productId").asLong());
		clientResponse = removeTest.accept("application/json").delete();
		assertEquals(412, clientResponse.getStatusCode());
		
		clientResponse = removeTest.header("If-Match",version).accept("application/json").delete();
		assertEquals(200, clientResponse.getStatusCode());
		version = clientResponse.getHeaders().get("ETag").get(0);
		
		version = clientResponse.getHeaders().get("ETag").get(0);
		
		Resource submitTest = client.resource(urlPrefix + "jaxrs/Customer/OpenOrder");
		clientResponse = submitTest.post(null);
		assertEquals(412, clientResponse.getStatusCode());
		
		clientResponse = submitTest.header("If-Match",version).post(null);
		assertEquals(204, clientResponse.getStatusCode());
		
		customerJson = custOrder.accept("application/json").get(String.class);
		customer = mapper.readTree(customerJson);
		assertTrue(customer.get("openOrder").isNull());
		
	}
	
	public void testOrderHistory() throws IOException
	{
		RestClient client = new RestClient(clientConfig);
		Resource orderHistoryTest = client.resource(urlPrefix + "jaxrs/Customer/Orders");
		ClientResponse clientResponse = orderHistoryTest.accept("application/json").get();
		String orderHistoryJson = clientResponse.getEntity(String.class);
		JsonNode orderHistory = mapper.readTree(orderHistoryJson);
		assertEquals(200, clientResponse.getStatusCode());
		int size = orderHistory.size();
		String lastModified = clientResponse.getHeaders().get("Last-Modified").get(0);
		
		
		clientResponse = orderHistoryTest.accept("application/json").header("If-Modified-Since", lastModified).get();
		assertEquals(304, clientResponse.getStatusCode());
		
		testOrderProcess();
		clientResponse = orderHistoryTest.accept("application/json").header("If-Modified-Since", lastModified).get();
		orderHistoryJson = clientResponse.getEntity(String.class);
		orderHistory = mapper.readTree(orderHistoryJson);
		int newSize = orderHistory.size();
		assertEquals(newSize,size+1);
		assertEquals(200, clientResponse.getStatusCode());
	}
	
	public void testFormMetaData () throws Exception
	{
		//Residential User
		RestClient client = new RestClient(clientConfig);
		Resource info = client.resource(urlPrefix + "jaxrs/Customer/TypeForm");
		ClientResponse clientResponse = info.accept("application/json").get();
		String formDataJson = clientResponse.getEntity(String.class);
		JsonNode formData = mapper.readTree(formDataJson);
		assertEquals(formData.get("type").asText(),"residential");
		assertEquals(formData.get("label").asText(),"Residential Customer");
		JsonNode groups = formData.get("formData");
		for (int i = 0; i < groups.size();i++)
		{
			JsonNode item = groups.get(i);
			if(item.get("name").asText().equals("frequentCustomer"))
			{
				assertEquals(item.get("name").asText(),"frequentCustomer");
				assertEquals(item.get("label").asText(),"Frequent Customer");
				assertEquals(item.get("type").asText(),"string");
				assertEquals(item.get("readonly").asText(),"true");
			}
			else if(item.get("name").asText().equals("householdSize"))
			{
				assertEquals(item.get("name").asText(),"householdSize");
				assertEquals(item.get("label").asText(),"Household Size");
				assertEquals(item.get("type").asText(),"number");
				assertEquals(item.get("required").asText(),"true");
				assertEquals(item.get("constraints").asText(),"{min:1,max:10,places:0}");
			}
		}
		
		
		//Business User
		RestClient client2 = new RestClient(clientConfig2);
		Resource info2 = client2.resource(urlPrefix + "jaxrs/Customer/TypeForm");
		ClientResponse clientResponse2 = info2.accept("application/json").get();
		formDataJson = clientResponse2.getEntity(String.class);
		formData = mapper.readTree(formDataJson);
		assertEquals(formData.get("type").asText(),"business");
		assertEquals(formData.get("label").asText(),"Business Customer");
		groups = formData.get("formData");
		for (int i = 0; i < groups.size();i++)
		{
			JsonNode item = groups.get(i);
			if(item.get("name").asText().equals("description"))
			{
				assertEquals(item.get("name").asText(),"description");
				assertEquals(item.get("label").asText(),"Description");
				assertEquals(item.get("type").asText(),"text");
			}
			else if(item.get("name").asText().equals("businessPartner"))
			{
				assertEquals(item.get("name").asText(),"businessPartner");
				assertEquals(item.get("label").asText(),"Business Partner");
				assertEquals(item.get("type").asText(),"string");
				assertEquals(item.get("readonly").asText(),"true");
			}
			else if(item.get("name").asText().equals("volumeDiscount"))
			{
				assertEquals(item.get("name").asText(),"volumeDiscount");
				assertEquals(item.get("label").asText(),"Volume Discount");
				assertEquals(item.get("type").asText(),"string");
				assertEquals(item.get("readonly").asText(),"true");
			}
		}
	}
	
	public void testUpdateInfo() throws IOException
	{
		//Residential User
		RestClient client = new RestClient(clientConfig);
		long householdSize = 3;
		ObjectNode data = mapper.createObjectNode();
		data.put("type", "RESIDENTIAL");
		data.put("householdSize",householdSize);
		Resource customerInfo = client.resource(urlPrefix + "jaxrs/Customer/Info");
		ClientResponse clientResponse = customerInfo.contentType(MediaType.APPLICATION_JSON).post(data.toString());
		assertEquals(204, clientResponse.getStatusCode());
		Resource resource = client.resource(urlPrefix + "jaxrs/Customer");
		String customerJson = resource.accept("application/json").get(String.class);
		JsonNode customer = mapper.readTree(customerJson);
		assertEquals(customer.get("householdSize").asLong(),data.get("householdSize").asLong());
		data.put("householdSize",6);
		clientResponse = customerInfo.contentType(MediaType.APPLICATION_JSON).post(data.toString());
		assertEquals(204, clientResponse.getStatusCode());
		
		//Business User
		RestClient client2 = new RestClient(clientConfig2);
		String desc = "High Tech Partner";
		data = mapper.createObjectNode();
		data.put("type", "BUSINESS");
		data.put("description", desc);
		customerInfo = client2.resource(urlPrefix + "jaxrs/Customer/Info");
		clientResponse = customerInfo.contentType(MediaType.APPLICATION_JSON).post(data.toString());
		assertEquals(204, clientResponse.getStatusCode());
		resource = client2.resource(urlPrefix + "jaxrs/Customer");
		customerJson = resource.accept("application/json").get(String.class);
		customer = mapper.readTree(customerJson);
		assertEquals(customer.get("description").asText(),desc);
	}
	

}
