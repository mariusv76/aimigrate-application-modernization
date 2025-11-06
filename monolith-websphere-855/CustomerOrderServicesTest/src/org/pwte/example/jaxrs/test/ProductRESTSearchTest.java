package org.pwte.example.jaxrs.test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class ProductRESTSearchTest extends TestCase {

	private String urlPrefix;
	private ObjectMapper mapper = new ObjectMapper();

	public void setUp() throws Exception {
		try {
			Context envEntryContext = (Context) new InitialContext().lookup("java:comp/env");
			urlPrefix = (String) envEntryContext.lookup("CUSTOMER_ORDER_SERVICES_WEB_ENDPOINT");
		} catch (NamingException e) {
			e.printStackTrace();
			urlPrefix = "https://localhost:9443/CustomerOrderServicesWeb/";
		}
	}

	public void testProductResources() throws Exception {
		RestClient client = new RestClient();
		Resource resource = client.resource(urlPrefix + "jaxrs/Product/1");

		String json = resource.accept("application/json").get(String.class);
		JsonNode product = mapper.readTree(json);
		assertEquals("Return of the Jedi", product.get("name").asText());
		assertEquals(29.99, product.get("price").asDouble());
		assertEquals(1L, product.get("id").asLong());
		assertEquals("images/Return.jpg", product.get("image").asText());
		assertEquals("Episode 6, Luke has the final confrontation with his father!", product.get("description").asText());
	}

	public void testProductListByCategory() throws Exception {
		RestClient client = new RestClient();
		Resource resource = client.resource(urlPrefix + "jaxrs/Product?categoryId=1");

		String json = resource.accept("application/json").get(String.class);
		JsonNode productList = mapper.readTree(json);
		for (int i = 0; i < productList.size(); i++) {
			JsonNode product = productList.get(i);

			switch (product.get("id").asInt()) {
			case 1: {
				assertEquals("Return of the Jedi", product.get("name").asText());
				assertEquals(29.99, product.get("price").asDouble());
				assertEquals(1L, product.get("id").asLong());
				assertEquals("images/Return.jpg", product.get("image").asText());
				assertEquals("Episode 6, Luke has the final confrontation with his father!",
						product.get("description").asText());
				break;
			}
			case 2: {
				assertEquals("Empire Strikes Back", product.get("name").asText());
				assertEquals(29.99, product.get("price").asDouble());
				assertEquals(2L, product.get("id").asLong());
				assertEquals("images/Empire.jpg", product.get("image").asText());
				assertEquals("Episode 5, Luke finds out a secret that will change his destiny",
						product.get("description").asText());
				break;
			}
			case 3: {
				assertEquals("New Hope", product.get("name").asText());
				assertEquals(29.99, product.get("price").asDouble());
				assertEquals(3L, product.get("id").asLong());
				assertEquals("images/NewHope.jpg", product.get("image").asText());
				assertEquals("Episode 4, after years of oppression, a band of rebels fight for freedom",
						product.get("description").asText());
				break;
			}
			default: {
				// fail("Invalid object");
			}
			}
		}
	}

	public void testCategoryResource() throws Exception {
		RestClient client = new RestClient();
		Resource resource = client.resource(urlPrefix + "jaxrs/Category/1");

		String json = resource.accept("application/json").get(String.class);
		JsonNode category = mapper.readTree(json);
		assertEquals("Entertainment", category.get("name").asText());
		assertEquals(1L, category.get("id").asLong());
		JsonNode subCategories = category.get("subCategories");
		for (int i = 0; i < subCategories.size(); i++) {
			JsonNode subCategory = subCategories.get(i);
			switch (subCategory.get("id").asInt()) {
			case 2: {
				assertEquals(2L, subCategory.get("id").asLong());
				assertEquals("Movies", subCategory.get("name").asText());
				break;
			}
			case 3: {
				assertEquals(3L, subCategory.get("id").asLong());
				assertEquals("Music", subCategory.get("name").asText());
				break;
			}
			case 4: {
				assertEquals(4L, subCategory.get("id").asLong());
				assertEquals("Games", subCategory.get("name").asText());
				break;
			}
			default: {
				fail("Invalid Subcategory");
			}
			}
		}
	}

	public void testCategoryListResources() throws Exception {
		RestClient client = new RestClient();
		Resource resource = client.resource(urlPrefix + "jaxrs/Category");

		String json = resource.accept("application/json").get(String.class);
		JsonNode categories = mapper.readTree(json);
		for (int k = 0; k < categories.size(); k++) {
			JsonNode category = categories.get(k);
			switch (category.get("id").asInt()) {
			case 1: {
				assertEquals("Entertainment", category.get("name").asText());
				assertEquals(1L, category.get("id").asLong());
				JsonNode subCategories = category.get("subCategories");
				for (int i = 0; i < subCategories.size(); i++) {
					JsonNode subCategory = subCategories.get(i);
					switch (subCategory.get("id").asInt()) {
					case 2: {
						assertEquals(2L, subCategory.get("id").asLong());
						assertEquals("Movies", subCategory.get("name").asText());
						break;
					}
					case 3: {
						assertEquals(3L, subCategory.get("id").asLong());
						assertEquals("Music", subCategory.get("name").asText());
						break;
					}
					case 4: {
						assertEquals(4L, subCategory.get("id").asLong());
						assertEquals("Games", subCategory.get("name").asText());
						break;
					}
					default: {
						fail("Invalid Subcategory");
					}
					}
				}
				break;
			}
			case 10: {
				assertEquals("Electronics", category.get("name").asText());
				assertEquals(10L, category.get("id").asLong());
				JsonNode subCategories = category.get("subCategories");
				for (int i = 0; i < subCategories.size(); i++) {
					JsonNode subCategory = subCategories.get(i);
					switch (subCategory.get("id").asInt()) {
					case 12: {
						assertEquals(12L, subCategory.get("id").asLong());
						assertEquals("TV", subCategory.get("name").asText());
						break;
					}
					case 13: {
						assertEquals(13L, subCategory.get("id").asLong());
						assertEquals("Cellphones", subCategory.get("name").asText());
						break;
					}
					case 14: {
						assertEquals(14L, subCategory.get("id").asLong());
						assertEquals("DVD Players", subCategory.get("name").asText());
						break;
					}
					default: {
						fail("Invalid Subcategory");
					}
					}
				}
				break;
			}
			default:
				fail("Invalid Category");

			}
		}
	}

}
