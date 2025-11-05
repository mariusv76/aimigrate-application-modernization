package org.pwte.example.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pwte.example.domain.Product;
import org.pwte.example.exception.ProductDoesNotExistException;
import org.pwte.example.service.ProductSearchService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductResource REST endpoint.
 * Tests JAX-RS operations for product catalog management.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductResource REST API Tests")
class ProductResourceTest {

    @Mock
    private ProductSearchService productSearchService;

    private ProductResource productResource;

    private static final int TEST_PRODUCT_ID = 200;
    private static final int INVALID_PRODUCT_ID = 999;
    private static final int TEST_CATEGORY_ID = 100;

    private Product testProduct;
    private List<Product> categoryProducts;

    @BeforeEach
    void setUp() throws Exception {
        // Create test product
        testProduct = new Product();
        testProduct.setProductId(TEST_PRODUCT_ID);
        testProduct.setName("Test Product");
        testProduct.setDescription("Test Description");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setImagePath("images/test.jpg");

        // Create category products
        categoryProducts = new ArrayList<>();
        Product product1 = new Product();
        product1.setProductId(1);
        product1.setName("Product 1");
        product1.setPrice(new BigDecimal("10.00"));
        categoryProducts.add(product1);

        Product product2 = new Product();
        product2.setProductId(2);
        product2.setName("Product 2");
        product2.setPrice(new BigDecimal("20.00"));
        categoryProducts.add(product2);

        // Create resource with mocked service
        productResource = new ProductResource() {
            {
                // Inject mock service directly (bypass JNDI lookup)
                this.productSearch = productSearchService;
            }
        };
    }

    // ===== getProduct Tests =====

    @Test
    @DisplayName("GET /Product/{id} should return product when it exists")
    void testGetProduct_Success() throws ProductDoesNotExistException {
        // Arrange
        when(productSearchService.loadProduct(TEST_PRODUCT_ID)).thenReturn(testProduct);

        // Act
        Response response = productResource.getProduct(TEST_PRODUCT_ID);

        // Assert
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isInstanceOf(Product.class);
        
        Product result = (Product) response.getEntity();
        assertThat(result.getProductId()).isEqualTo(TEST_PRODUCT_ID);
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        
        // Verify Expires header is set
        assertThat(response.getMetadata().getFirst("Expires")).isNotNull();
        
        verify(productSearchService).loadProduct(TEST_PRODUCT_ID);
    }

    @Test
    @DisplayName("GET /Product/{id} should throw 404 when product does not exist")
    void testGetProduct_NotFound() throws ProductDoesNotExistException {
        // Arrange
        when(productSearchService.loadProduct(INVALID_PRODUCT_ID))
                .thenThrow(new ProductDoesNotExistException());

        // Act & Assert
        assertThatThrownBy(() -> productResource.getProduct(INVALID_PRODUCT_ID))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        
        verify(productSearchService).loadProduct(INVALID_PRODUCT_ID);
    }

    @Test
    @DisplayName("GET /Product/{id} should set Expires header for caching")
    void testGetProduct_ExpiresHeader() throws ProductDoesNotExistException {
        // Arrange
        when(productSearchService.loadProduct(TEST_PRODUCT_ID)).thenReturn(testProduct);

        // Act
        Response response = productResource.getProduct(TEST_PRODUCT_ID);

        // Assert
        assertThat(response.getMetadata().getFirst("Expires")).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("GET /Product/{id} should return product with all fields populated")
    void testGetProduct_AllFields() throws ProductDoesNotExistException {
        // Arrange
        Product fullProduct = new Product();
        fullProduct.setProductId(300);
        fullProduct.setName("Complete Product");
        fullProduct.setDescription("Full Description");
        fullProduct.setPrice(new BigDecimal("149.99"));
        fullProduct.setImagePath("images/complete.jpg");

        when(productSearchService.loadProduct(300)).thenReturn(fullProduct);

        // Act
        Response response = productResource.getProduct(300);
        Product result = (Product) response.getEntity();

        // Assert
        assertThat(result.getProductId()).isEqualTo(300);
        assertThat(result.getName()).isEqualTo("Complete Product");
        assertThat(result.getDescription()).isEqualTo("Full Description");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("149.99"));
        assertThat(result.getImagePath()).isEqualTo("images/complete.jpg");
    }

    // ===== getProductsByCategory Tests =====

    @Test
    @DisplayName("GET /Product?categoryId={id} should return products for valid category")
    void testGetProductsByCategory_Success() {
        // Arrange
        when(productSearchService.loadProductsByCategory(TEST_CATEGORY_ID))
                .thenReturn(categoryProducts);

        // Act
        List<Product> result = productResource.getProductsByCategory(TEST_CATEGORY_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Product 1");
        assertThat(result.get(1).getName()).isEqualTo("Product 2");
        verify(productSearchService).loadProductsByCategory(TEST_CATEGORY_ID);
    }

    @Test
    @DisplayName("GET /Product?categoryId=0 should throw 400 Bad Request")
    void testGetProductsByCategory_ZeroCategory() {
        // Act & Assert
        assertThatThrownBy(() -> productResource.getProductsByCategory(0))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        
        verifyNoInteractions(productSearchService);
    }

    @Test
    @DisplayName("GET /Product?categoryId=-1 should throw 400 Bad Request")
    void testGetProductsByCategory_NegativeCategory() {
        // Act & Assert
        assertThatThrownBy(() -> productResource.getProductsByCategory(-1))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        
        verifyNoInteractions(productSearchService);
    }

    @Test
    @DisplayName("GET /Product?categoryId={id} should return empty list when no products")
    void testGetProductsByCategory_Empty() {
        // Arrange
        when(productSearchService.loadProductsByCategory(TEST_CATEGORY_ID))
                .thenReturn(new ArrayList<>());

        // Act
        List<Product> result = productResource.getProductsByCategory(TEST_CATEGORY_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(productSearchService).loadProductsByCategory(TEST_CATEGORY_ID);
    }

    @Test
    @DisplayName("GET /Product?categoryId={id} should handle large product lists")
    void testGetProductsByCategory_Large() {
        // Arrange
        List<Product> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Product p = new Product();
            p.setProductId(i);
            p.setName("Product " + i);
            p.setPrice(new BigDecimal(String.valueOf(i * 10)));
            largeList.add(p);
        }
        when(productSearchService.loadProductsByCategory(TEST_CATEGORY_ID))
                .thenReturn(largeList);

        // Act
        List<Product> result = productResource.getProductsByCategory(TEST_CATEGORY_ID);

        // Assert
        assertThat(result).hasSize(100);
        assertThat(result.get(0).getName()).isEqualTo("Product 0");
        assertThat(result.get(99).getName()).isEqualTo("Product 99");
    }

    @Test
    @DisplayName("getProductsByCategory should validate positive category ID only")
    void testGetProductsByCategory_ValidationBoundary() {
        // Arrange
        when(productSearchService.loadProductsByCategory(1)).thenReturn(categoryProducts);

        // Act - Valid: categoryId = 1
        List<Product> result = productResource.getProductsByCategory(1);
        assertThat(result).isNotNull();

        // Act & Assert - Invalid: categoryId = 0
        assertThatThrownBy(() -> productResource.getProductsByCategory(0))
                .isInstanceOf(WebApplicationException.class);

        // Act & Assert - Invalid: categoryId = -1
        assertThatThrownBy(() -> productResource.getProductsByCategory(-1))
                .isInstanceOf(WebApplicationException.class);
    }

    @Test
    @DisplayName("getProductsByCategory should verify service interaction")
    void testGetProductsByCategory_VerifyServiceCall() {
        // Arrange
        when(productSearchService.loadProductsByCategory(anyInt()))
                .thenReturn(categoryProducts);

        // Act
        productResource.getProductsByCategory(TEST_CATEGORY_ID);

        // Assert
        verify(productSearchService, times(1)).loadProductsByCategory(TEST_CATEGORY_ID);
        verifyNoMoreInteractions(productSearchService);
    }

    @Test
    @DisplayName("getProduct should handle products with zero price")
    void testGetProduct_ZeroPrice() throws ProductDoesNotExistException {
        // Arrange
        Product freeProduct = new Product();
        freeProduct.setProductId(400);
        freeProduct.setName("Free Product");
        freeProduct.setPrice(BigDecimal.ZERO);

        when(productSearchService.loadProduct(400)).thenReturn(freeProduct);

        // Act
        Response response = productResource.getProduct(400);
        Product result = (Product) response.getEntity();

        // Assert
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getProduct should handle products with high precision prices")
    void testGetProduct_HighPrecisionPrice() throws ProductDoesNotExistException {
        // Arrange
        Product preciseProduct = new Product();
        preciseProduct.setProductId(500);
        preciseProduct.setName("Precise Product");
        preciseProduct.setPrice(new BigDecimal("99.999999"));

        when(productSearchService.loadProduct(500)).thenReturn(preciseProduct);

        // Act
        Response response = productResource.getProduct(500);
        Product result = (Product) response.getEntity();

        // Assert
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("99.999999"));
    }
}
