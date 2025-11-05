package org.pwte.example.resources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pwte.example.domain.Category;
import org.pwte.example.exception.CategoryDoesNotExist;
import org.pwte.example.service.ProductSearchService;

import javax.naming.InitialContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CategoryResource REST endpoint.
 * Tests JAX-RS operations without requiring a running server.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryResource REST API Tests")
class CategoryResourceTest {

    @Mock
    private ProductSearchService productSearchService;

    private CategoryResource categoryResource;

    private static final int TEST_CATEGORY_ID = 100;
    private static final int INVALID_CATEGORY_ID = 999;

    private Category testCategory;
    private List<Category> topLevelCategories;

    @BeforeEach
    void setUp() throws Exception {
        // Create test category
        testCategory = new Category();
        testCategory.setCategoryID(TEST_CATEGORY_ID);
        testCategory.setName("Test Category");

        // Create top-level categories
        topLevelCategories = new ArrayList<>();
        Category cat1 = new Category();
        cat1.setCategoryID(1);
        cat1.setName("Electronics");
        topLevelCategories.add(cat1);

        Category cat2 = new Category();
        cat2.setCategoryID(2);
        cat2.setName("Books");
        topLevelCategories.add(cat2);

        // Create resource with mocked service
        categoryResource = new CategoryResource() {
            {
                // Inject mock service directly (bypass JNDI lookup)
                this.productSearch = productSearchService;
            }
        };
    }

    // ===== loadCategory Tests =====

    @Test
    @DisplayName("GET /Category/{id} should return category when it exists")
    void testLoadCategory_Success() throws CategoryDoesNotExist {
        // Arrange
        when(productSearchService.loadCategory(TEST_CATEGORY_ID)).thenReturn(testCategory);

        // Act
        Category result = categoryResource.loadCategory(TEST_CATEGORY_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCategoryID()).isEqualTo(TEST_CATEGORY_ID);
        assertThat(result.getName()).isEqualTo("Test Category");

        verify(productSearchService).loadCategory(TEST_CATEGORY_ID);
    }

    @Test
    @DisplayName("GET /Category/{id} should throw 404 when category does not exist")
    void testLoadCategory_NotFound() throws CategoryDoesNotExist {
        // Arrange
        when(productSearchService.loadCategory(INVALID_CATEGORY_ID))
                .thenThrow(new CategoryDoesNotExist());

        // Act & Assert
        assertThatThrownBy(() -> categoryResource.loadCategory(INVALID_CATEGORY_ID))
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        
        verify(productSearchService).loadCategory(INVALID_CATEGORY_ID);
    }

    @Test
    @DisplayName("GET /Category/{id} should handle multiple different categories")
    void testLoadCategory_MultipleDifferentIds() throws CategoryDoesNotExist {
        // Arrange
        Category category1 = new Category();
        category1.setCategoryID(1);
        category1.setName("Category 1");

        Category category2 = new Category();
        category2.setCategoryID(2);
        category2.setName("Category 2");

        when(productSearchService.loadCategory(1)).thenReturn(category1);
        when(productSearchService.loadCategory(2)).thenReturn(category2);

        // Act
        Category result1 = categoryResource.loadCategory(1);
        Category result2 = categoryResource.loadCategory(2);

        // Assert
        assertThat(result1.getCategoryID()).isEqualTo(1);
        assertThat(result1.getName()).isEqualTo("Category 1");
        assertThat(result2.getCategoryID()).isEqualTo(2);
        assertThat(result2.getName()).isEqualTo("Category 2");
    }

    // ===== loadTopLevelCategories Tests =====

    @Test
    @DisplayName("GET /Category should return all top-level categories")
    void testLoadTopLevelCategories_Success() {
        // Arrange
        when(productSearchService.getTopLevelCategories()).thenReturn(topLevelCategories);

        // Act
        List<Category> result = categoryResource.loadTopLevelCategories();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        assertThat(result.get(1).getName()).isEqualTo("Books");
        verify(productSearchService).getTopLevelCategories();
    }

    @Test
    @DisplayName("GET /Category should return empty list when no top-level categories exist")
    void testLoadTopLevelCategories_Empty() {
        // Arrange
        when(productSearchService.getTopLevelCategories()).thenReturn(new ArrayList<>());

        // Act
        List<Category> result = categoryResource.loadTopLevelCategories();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(productSearchService).getTopLevelCategories();
    }

    @Test
    @DisplayName("GET /Category should return categories with hierarchy information")
    void testLoadTopLevelCategories_WithHierarchy() {
        // Arrange
        Category parent = new Category();
        parent.setCategoryID(1);
        parent.setName("Parent Category");

        Category child = new Category();
        child.setCategoryID(2);
        child.setName("Child Category");
        child.setParent(parent);

        List<Category> categories = new ArrayList<>();
        categories.add(parent);

        when(productSearchService.getTopLevelCategories()).thenReturn(categories);

        // Act
        List<Category> result = categoryResource.loadTopLevelCategories();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Parent Category");
    }

    @Test
    @DisplayName("GET /Category should handle large number of categories")
    void testLoadTopLevelCategories_Large() {
        // Arrange
        List<Category> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Category cat = new Category();
            cat.setCategoryID(i);
            cat.setName("Category " + i);
            largeList.add(cat);
        }
        when(productSearchService.getTopLevelCategories()).thenReturn(largeList);

        // Act
        List<Category> result = categoryResource.loadTopLevelCategories();

        // Assert
        assertThat(result).hasSize(100);
        assertThat(result.get(0).getName()).isEqualTo("Category 0");
        assertThat(result.get(99).getName()).isEqualTo("Category 99");
    }

    @Test
    @DisplayName("loadCategory should verify service interaction")
    void testLoadCategory_VerifyServiceCall() throws CategoryDoesNotExist {
        // Arrange
        when(productSearchService.loadCategory(anyInt())).thenReturn(testCategory);

        // Act
        categoryResource.loadCategory(TEST_CATEGORY_ID);

        // Assert
        verify(productSearchService, times(1)).loadCategory(TEST_CATEGORY_ID);
        verifyNoMoreInteractions(productSearchService);
    }
}
